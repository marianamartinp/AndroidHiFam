package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mariana.androidhifam.databinding.FragmentPublicacionesListaBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;

public class PublicacionesListaFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout, ModalFragment.CustomModalInterface {
    private PublicacionesListaFragmentArgs publicacionesListaFragmentArgs;
    private @NonNull FragmentPublicacionesListaBinding binding;
    private ArrayList<File> imagenesPublicaciones;
    private ArrayList<Publicacion> publicaciones;
    private NavController navController;
    private GridAdapter<Publicacion> adapter;
    private Integer idAlbum, idGrupo, tokenUsuario;
    private Album album;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicacionesListaFragmentArgs = PublicacionesListaFragmentArgs.fromBundle(getArguments());
            idAlbum = publicacionesListaFragmentArgs.getIdAlbum();
            idGrupo = publicacionesListaFragmentArgs.getIdGrupo();
        }
        publicaciones = new ArrayList<>();
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPublicacionesListaBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
        tokenUsuario = Integer.parseInt(activity.getToken());
        cliente = activity.getCliente();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setRefreshLayout(this);
        binding.botonVistaGrid.setOnClickListener(this);
        binding.botonNuevaPublicacion.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
        binding.gridView.setOnItemClickListener(this);
        SwipeRefreshLayout refreshLayout = activity.findViewById(R.id.refreshLayout);
        binding.gridView.setOnScrollListener(new GridView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Disable refreshing when scrolling
                if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    refreshLayout.setEnabled(false);
                } else {
                    refreshLayout.setEnabled(true);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Empty method body, not needed for this purpose
            }
        });
        resumirVistaPublicaciones(idAlbum);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void cargarAlbum(Integer idAlbum, CountDownLatch latch) {
        try {
            album = cliente.leerAlbum(idAlbum);
            mainHandler.post(this::cargarTituloAlbum);
        }
        catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(() -> Toast.makeText(getContext(), "Error al el título del álbum.", Toast.LENGTH_SHORT).show());
        }
        finally {
            latch.countDown();
        }
    }

    public void cargarTituloAlbum() {
        if (null != album) {
            binding.tituloAlbum.setText(album.getTitulo());
        }
    }

    public void cargarPublicaciones(Integer idAlbum, CountDownLatch latch) {
        try {
            LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
            filtros.put("pea.COD_ALBUM", "="+idAlbum);
            filtros.put("p.FECHA_ELIMINACION", "is null");
            LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
            ordenacion.put("p.COD_PUBLICACION", "asc");
            publicaciones = cliente.leerPublicaciones(filtros,ordenacion);
            mainHandler.post(this::actualizarInterfaz);
        }
        catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            latch.countDown();
        }
    }

    public void cargarLista() {
        imagenesPublicaciones = activity.getImagenes();
        adapter = new GridAdapter<>(requireContext(), publicaciones, imagenesPublicaciones, true);
        binding.gridView.setAdapter(adapter);
    }

    public void actualizarInterfaz() {
        cargarTituloAlbum();
        cargarLista();
        mostrarTextoAlternativo();
    }

    public void cargarImagenesDrive(CountDownLatch latch) {
        try {
            activity.cargarImagenesDrive(false);
            mainHandler.post(this::actualizarInterfaz);
        } finally {
            latch.countDown();
        }
    }

    public void cargarVistaPublicaciones(Integer idAlbum, SwipeRefreshLayout refreshLayout) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        CountDownLatch latch = new CountDownLatch(3);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> cargarAlbum(idAlbum, latch));
        executorService.execute(() -> cargarPublicaciones(idAlbum, latch));
        executorService.execute(() -> cargarImagenesDrive(latch));
        executorService.execute(() -> {
            try {
                latch.await();
                mainHandler.post(() -> {
                    binding.gridView.clearAnimation();
                    if (null != refreshLayout) {
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "Se han actualizado las publicaciones.", Toast.LENGTH_SHORT).show();
                    }
                    activity.setHabilitarInteraccion(true);
                });
            } catch (InterruptedException e) {
                mainHandler.post(this::errorAlCargarInterfaz);
            }
        });
    }

    public void resumirVistaPublicaciones(Integer idAlbum) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> {
            cargarPublicaciones(idAlbum, null);
            mainHandler.post(() -> {
                binding.gridView.clearAnimation();
                activity.setHabilitarInteraccion(true);
            });
        });
    }

    public void menuPopUp() {
        PopupMenu popup = new PopupMenu(requireActivity(), binding.botonOpciones);
        popup.getMenuInflater().inflate(R.menu.menu_context_albumes, popup.getMenu());
        if (null != album) {
            if (Objects.equals(tokenUsuario, album.getUsuarioAdminAlbum().getCodUsuario()) ||
                Objects.equals(tokenUsuario, album.getGrupoCreaAlbum().getUsuarioAdminGrupo().getCodUsuario())) {
                popup.getMenu().clear();
                popup.getMenuInflater().inflate(R.menu.menu_opciones_albumes, popup.getMenu());
            }
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(requireActivity(), "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        popup.show();
    }

    @Override
    public void onClick(View v) {
        if (activity.getHabilitarInteraccion()) {
            int id = v.getId();
            if (id == R.id.botonVistaGrid) {
                findNavController(v).navigate(PublicacionesListaFragmentDirections.actionPublicacionesListaFragmentToPublicacionesFragment(idAlbum, idGrupo));
            } else if (id == R.id.botonNuevaPublicacion) {
                findNavController(v).navigate(PublicacionesListaFragmentDirections.actionPublicacionesListaFragmentToNuevaPublicacionFragment(idAlbum, idGrupo));
            } else if (id == R.id.botonOpciones) {
                menuPopUp();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (activity.getHabilitarInteraccion()) {
            findNavController(view).navigate(PublicacionesListaFragmentDirections.actionPublicacionesListaFragmentToPublicacionFragment((int) id, idGrupo, idAlbum));
        }
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh(SwipeRefreshLayout refreshLayout) {
        if (activity.getHabilitarInteraccion()) {
            cargarVistaPublicaciones(idAlbum, refreshLayout);
        }
        else {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onPositiveClick(String idModal, Integer position, Integer id) {
        if (activity.getHabilitarInteraccion()) {
            eliminarAlbum(id);
        }
    }

    public void eliminarAlbum(int idAlbum) {
        executorService.execute(() -> {
            try {
                cliente.eliminarAlbum(idAlbum);
                mainHandler.post(() -> {
                    navController.popBackStack();
                    Toast.makeText(requireContext(), "Álbum eliminado.", Toast.LENGTH_SHORT).show();
                });
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(requireContext(), "Error al eliminar el álbum.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void modalEliminarAlbum(int id) {
        ModalFragment modal = new ModalFragment("eliminarAlbum", null, (int) id, this, "¿Desea eliminar este álbum?", getString(R.string.btnEliminar), getString(R.string.btnCancelar));
        modal.show(activity.getSupportFragmentManager(), "modalEliminarAlbum");
    }

    public void mostrarTextoAlternativo() {
        if (publicaciones.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        }
        else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }

    public void errorAlCargarInterfaz() {
        Toast.makeText(getContext(), "Error al cargar las publicaciones.", Toast.LENGTH_SHORT).show();
    }
}
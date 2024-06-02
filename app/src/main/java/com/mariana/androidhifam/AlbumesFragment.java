package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import com.mariana.androidhifam.databinding.FragmentAlbumesBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.Grupo;

public class AlbumesFragment extends Fragment implements View.OnClickListener, View.OnCreateContextMenuListener, MenuProvider, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout, ModalFragment.CustomModalInterface {
    private AlbumesFragmentArgs albumesFragmentArgs;
    private @NonNull FragmentAlbumesBinding binding;
    private ArrayList<Album> albumes;
    private ArrayList<File> imagenesAlbumes;
    GridAdapter<Album> adapter;
    private CCAlbumFamiliar cliente;
    private Integer idGrupo, tokenUsuario;
    private Grupo grupo;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private NavController navController;
    private boolean vistaCreada = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            albumesFragmentArgs = AlbumesFragmentArgs.fromBundle(getArguments());
            idGrupo = albumesFragmentArgs.getIdGrupo();
        }
        albumes = new ArrayList<>();
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlbumesBinding.inflate(inflater, container, false);
        cliente = activity.getCliente();
        tokenUsuario = Integer.parseInt(activity.getToken());
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setRefreshLayout(this);
        //getActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        binding.botonNuevoAlbum.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
        binding.botonUsuarios.setOnClickListener(this);
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

        if (vistaCreada) {
            resumirVistaAlbumes(idGrupo);
        }
        else {
            cargarVistaAlbumes(idGrupo, null);
        }
        registerForContextMenu(binding.gridView);

        binding.gridView.setOnItemClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        getActivity().removeMenuProvider(this);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_context_albumes, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        // Menú contextual álbumes
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (activity.getHabilitarInteraccion()) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_context_albumes, menu);
        }
    }

    public void cargarGrupo(Integer idGrupo, CountDownLatch latch) {;
        try {
            grupo = cliente.leerGrupo(idGrupo);
        }
        catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            latch.countDown();
        }
    }

    public void cargarTituloGrupo() {
        if (null != grupo) {
            binding.tituloGrupo.setText(grupo.getTitulo());
        }
    }

    public void cargarAlbumes(Integer idGrupo, CountDownLatch latch) {
        try {
            LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
            filtros.put("a.COD_GRUPO_CREA_ALBUM", "="+idGrupo);
            filtros.put("a.FECHA_ELIMINACION", "is null");
            LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
            ordenacion.put("a.titulo", "asc");
            albumes = cliente.leerAlbumes(filtros,ordenacion);
            mainHandler.post(this::actualizarInterfaz);
        }
        catch(Exception e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            latch.countDown();
        }
    }

    public void cargarGrid() {
        imagenesAlbumes = activity.getImagenes();
        adapter = new GridAdapter<>(requireContext(), albumes, imagenesAlbumes, false);
        binding.gridView.setAdapter(adapter);
    }

    public void actualizarInterfaz() {
        cargarGrid();
        mostrarTextoAlternativo();
    }

    public void cargarImagenesDrive(CountDownLatch latch) {
        try {
            activity.cargarImagenesDrive(false);
        } finally {
            latch.countDown();
        }
    }

    public void cargarVistaAlbumes(Integer idGrupo, SwipeRefreshLayout refreshLayout) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        CountDownLatch latch = new CountDownLatch(3);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> {
            cargarGrupo(idGrupo, latch);
            mainHandler.post(this::cargarTituloGrupo);
        });
        executorService.execute(() -> cargarAlbumes(idGrupo, latch));
        executorService.execute(() -> {
            cargarImagenesDrive(latch);
            mainHandler.post(this::actualizarInterfaz);
        });
        executorService.execute(() -> {
            try {
                latch.await();
                mainHandler.post(() -> {
                    binding.gridView.clearAnimation();
                    if (null != refreshLayout) {
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "Se han actualizado los álbumes.", Toast.LENGTH_SHORT).show();
                    }
                    activity.setHabilitarInteraccion(true);
                    vistaCreada = true;
                });
            } catch (InterruptedException e) {
                mainHandler.post(this::errorAlCargarInterfaz);
            }
        });
    }

    public void resumirVistaAlbumes(Integer idGrupo) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        CountDownLatch latch = new CountDownLatch(1);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> cargarAlbumes(idGrupo, latch));
        executorService.execute(() -> {
            try {
                latch.await();
                mainHandler.post(() -> {
                    binding.gridView.clearAnimation();
                    activity.setHabilitarInteraccion(true);
                    vistaCreada = true;
                });
            } catch (InterruptedException e) {
                mainHandler.post(this::errorAlCargarInterfaz);
            }
        });
    }

    public void menuPopUp() {
        PopupMenu popup = new PopupMenu(requireActivity(), binding.botonOpciones);
        if (Objects.equals(tokenUsuario, grupo.getUsuarioAdminGrupo().getCodUsuario())) {
            popup.getMenuInflater().inflate(R.menu.menu_opciones_grupos_admin, popup.getMenu());
        }
        else {
            popup.getMenuInflater().inflate(R.menu.menu_opciones_grupos, popup.getMenu());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int idMenuItem = item.getItemId();

                if(idMenuItem == R.id.eliminarGrupo) {
                    modalEliminarGrupo(idGrupo);
                    return true;
                }
                else {
                    return true;
                }
            }
        });
        popup.show();
    }

    @Override
    public void onClick(View v) {
        if (activity.getHabilitarInteraccion()) {
            int id = v.getId();
            if (id == R.id.botonNuevoAlbum) {
                navController.navigate(AlbumesFragmentDirections.actionAlbumesFragmentToNuevoAlbumFragment(idGrupo));
            } else if (id == R.id.botonOpciones) {
                menuPopUp();
            } else if (id == R.id.botonUsuarios) {
                navController.navigate(AlbumesFragmentDirections.actionAlbumesFragmentToSolicitudesEntradaGrupoFragment(idGrupo));
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (activity.getHabilitarInteraccion()) {
            findNavController(view).navigate(AlbumesFragmentDirections.actionAlbumesFragmentToPublicacionesFragment((int) id, idGrupo));
        }
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh(SwipeRefreshLayout refreshLayout) {
        if (activity.getHabilitarInteraccion()) {
            cargarVistaAlbumes(idGrupo, refreshLayout);
        }
        else {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onPositiveClick(Integer position, Integer id) {
        if (activity.getHabilitarInteraccion()) {
            eliminarGrupo(id);
        }
    }

    public void eliminarGrupo(int idGrupo) {
        executorService.execute(() -> {
            try {
                cliente.eliminarGrupo(idGrupo);
                mainHandler.post(() -> {
                    navController.popBackStack();
                    Toast.makeText(requireContext(), "Grupo eliminado.", Toast.LENGTH_SHORT).show();
                });
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(this::errorAlCargarInterfaz);
            }
        });
    }

    public void modalEliminarGrupo(int id) {
        ModalFragment modal = new ModalFragment(null, (int) id, this, "¿Desea eliminar este grupo?", getString(R.string.btnEliminar), getString(R.string.btnCancelar));
        modal.show(getActivity().getSupportFragmentManager(), "modalEliminarGrupo");
    }

    public void mostrarTextoAlternativo() {
        if (albumes.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        }
        else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }

    public void errorAlCargarInterfaz() {
        Toast.makeText(getContext(), "Error al cargar los álbumes.", Toast.LENGTH_SHORT).show();
    }
}
package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import com.mariana.androidhifam.databinding.FragmentPublicacionesBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pojosalbumfamiliar.Publicacion;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;

public class PublicacionesFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout {
    private PublicacionesFragmentArgs publicacionesFragmentArgs;
    private @NonNull FragmentPublicacionesBinding binding;
    private ArrayList<File> imagenesPublicaciones;
    private ArrayList<Publicacion> publicaciones;
    private GridAdapter<Publicacion> adapter;
    private Integer idAlbum, idGrupo;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private ServicioPublicacion servicioPublicacion;
    private boolean vistaCreada = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicacionesFragmentArgs = PublicacionesFragmentArgs.fromBundle(getArguments());
            idAlbum = publicacionesFragmentArgs.getIdAlbum();
            idGrupo = publicacionesFragmentArgs.getIdGrupo();
        }
        publicaciones = new ArrayList<>();
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        servicioPublicacion = new ServicioPublicacion();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPublicacionesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setRefreshLayout(this);
        binding.botonVistaIndividual.setOnClickListener(this);
        binding.botonNuevaPublicacion.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
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
        binding.gridView.setOnItemClickListener(this);
        if (vistaCreada) {
            actualizarInterfaz();
        }
        else {
            cargarVistaPublicaciones(idAlbum, false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void cargarTituloAlbum(Integer idAlbum, CountDownLatch latch) {
        try {
            String tituloAlbum = servicioPublicacion.cargarTituloAlbum(idAlbum);
            mainHandler.post(() -> binding.tituloAlbum.setText(tituloAlbum));
        }
        catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(() -> Toast.makeText(getContext(), "Error al el título del álbum.", Toast.LENGTH_SHORT).show());
        }
        finally {
            latch.countDown();
        }
    }

    public void cargarPublicaciones(Integer idAlbum, CountDownLatch latch) {
        try {
            publicaciones = servicioPublicacion.cargarPublicaciones(idAlbum);
            mainHandler.post(this::actualizarInterfaz);
        }
        catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            latch.countDown();
        }
    }

    public void cargarGrid() {
        imagenesPublicaciones = activity.getImagenes();
        adapter = new GridAdapter<>(requireContext(), publicaciones, imagenesPublicaciones, false);
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

    public void cargarVistaPublicaciones(Integer idAlbum, boolean refrescar) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        CountDownLatch latch = new CountDownLatch(3);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> cargarTituloAlbum(idAlbum, latch));
        executorService.execute(() -> cargarPublicaciones(idAlbum, latch));
        executorService.execute(() -> {
            cargarImagenesDrive(latch);
            mainHandler.post(this::actualizarInterfaz);
        });
        executorService.execute(() -> {
            try {
                latch.await();
                mainHandler.post(() -> {
                    binding.gridView.clearAnimation();
                    if (refrescar) {
                        Toast.makeText(getContext(), "Se han actualizado las publicaciones.", Toast.LENGTH_SHORT).show();
                    }
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
        popup.getMenuInflater()
                .inflate(R.menu.menu_context_grupos, popup.getMenu());
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
            if (id == R.id.botonVistaIndividual) {
                findNavController(v).navigate(PublicacionesFragmentDirections.actionPublicacionesFragmentToPublicacionesListaFragment(idAlbum, idGrupo));
            } else if (id == R.id.botonNuevaPublicacion) {
                findNavController(v).navigate(PublicacionesFragmentDirections.actionPublicacionesFragmentToNuevaPublicacionFragment(idAlbum, idGrupo));
            } else if (id == R.id.botonOpciones) {
                menuPopUp();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (activity.getHabilitarInteraccion()) {
            findNavController(view).navigate(PublicacionesFragmentDirections.actionPublicacionesFragmentToPublicacionFragment((int) id));
        }
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh() {
        if (activity.getHabilitarInteraccion()) {
            cargarVistaPublicaciones(idAlbum, true);
        }
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
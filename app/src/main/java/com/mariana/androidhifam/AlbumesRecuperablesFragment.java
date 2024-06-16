package com.mariana.androidhifam;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mariana.androidhifam.databinding.FragmentAlbumesRecuperablesBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import utils.GridAdapter;

public class AlbumesRecuperablesFragment extends Fragment implements View.OnCreateContextMenuListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout, ModalFragment.CustomModalInterface {

    private @NonNull AlbumesFragmentArgs albumesFragmentArgs;
    private @NonNull FragmentAlbumesRecuperablesBinding binding;
    private ArrayList<Album> albumes;
    private ArrayList<File> imagenesAlbumes;
    private GridAdapter<Album> adapter;
    private CCAlbumFamiliar cliente;
    private Integer tokenUsuario, idGrupo;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;

    // Método llamado al crear la instancia del Fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recupera los argumentos pasados al Fragment
        if (getArguments() != null) {
            albumesFragmentArgs = AlbumesFragmentArgs.fromBundle(getArguments());
            idGrupo = albumesFragmentArgs.getIdGrupo();
        }
        // Inicializa el cliente para interactuar con el backend
        cliente = new CCAlbumFamiliar();
        albumes = new ArrayList<>();
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Método llamado para crear la vista del Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity.findViewById(R.id.refreshLayout).setEnabled(true);
        // Infla el layout del Fragment y obtiene una instancia del binding
        binding = FragmentAlbumesRecuperablesBinding.inflate(inflater, container, false);
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    // Método llamado cuando la vista ha sido creada
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        activity.setRefreshLayout(this);
        registerForContextMenu(binding.gridView);
        SwipeRefreshLayout refreshLayout = activity.findViewById(R.id.refreshLayout);

        // Listener para deshabilitar el refresh cuando se está desplazando la vista
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
                // No precisado.
            }
        });

        // Listener para manejar el clic en los ítems del GridView
        binding.gridView.setOnItemClickListener(this);
        // Cargo los datos en la vista.
        cargarVistaAlbumes(tokenUsuario, idGrupo, null);
    }

    // Método llamado al destruir la vista del Fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Método llamado al seleccionar un ítem del menú contextual
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        int itemId = (int) binding.gridView.getAdapter().getItemId(position);
        int idMenuItem = item.getItemId();

        if (idMenuItem == R.id.recuperarAlbum) {
            modalRecuperarAlbum(position, itemId);
            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }
    }

    // Método para crear el menú contextual
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (activity.getHabilitarInteraccion()) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_albumes_recuperables, menu);
        }
    }

    // Método llamado al hacer clic en un ítem del GridView
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (activity.getHabilitarInteraccion()) {
            modalRecuperarAlbum(position, (int) id);
        }
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh(SwipeRefreshLayout refreshLayout) {
        if (activity.getHabilitarInteraccion()) {
            cargarVistaAlbumes(tokenUsuario, idGrupo, refreshLayout);
        }
        else {
            refreshLayout.setRefreshing(false);
        }
    }

    // Implementación del método para manejar el clic positivo en el modal
    @Override
    public void onPositiveClick(String idModal, Integer position, Integer id) {
        if (activity.getHabilitarInteraccion()) {
            recuperarAlbum(position, id);
        }
    }

    // Método para cargar los álbumes eliminados
    public void cargarAlbumes(Integer tokenUsuario, Integer idGrupo) {
        try {
            LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
            filtros.put("a.COD_USUARIO_ADMIN_ALBUM", "=" + tokenUsuario);
            filtros.put("a.COD_GRUPO_CREA_ALBUM", "=" + idGrupo);
            filtros.put("a.FECHA_ELIMINACION", "is not null");
            LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
            ordenacion.put("a.titulo", "asc");
            albumes = cliente.leerAlbumes(filtros,ordenacion);
            mainHandler.post(this::actualizarInterfaz);
        }
        catch (ExcepcionAlbumFamiliar e) {
            errorAlCargarInterfaz();
        }
    }

    // Método para cargar el GridView para los álbumes e imágenes
    public void cargarGrid() {
        imagenesAlbumes = activity.getImagenes();
        adapter = new GridAdapter<>(requireContext(), albumes, imagenesAlbumes, false);
        binding.gridView.setAdapter(adapter);
    }

    // Método para actualizar la interfaz
    public void actualizarInterfaz() {
        cargarGrid();
        mostrarTextoAlternativo();
    }

    // Método para cargar la vista de álbumes llamando a métodos secundarios
    public void cargarVistaAlbumes(Integer tokenUsuario, Integer idGrupo, SwipeRefreshLayout refreshLayout) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> {
            cargarAlbumes(tokenUsuario, idGrupo);
            mainHandler.post(() -> {
                binding.gridView.clearAnimation();
                if (null != refreshLayout) {
                    refreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Se han actualizado los álbumes eliminados.", Toast.LENGTH_SHORT).show();
                }
                activity.setHabilitarInteraccion(true);
            });
        });
    }

    // Método para mostrar el modal de recuperación de álbum
    public void modalRecuperarAlbum(int position, int id) {
        ModalFragment modal = new ModalFragment("recuperarAlbum", position, (int) id, this, "¿Desea recuperar este álbum?", getString(R.string.btnRecuperar), getString(R.string.btnCancelar));
        modal.show(activity.getSupportFragmentManager(), "modalRecuperarAlbum");
    }

    // Método para recuperar un álbum
    public void recuperarAlbum(int position, int idAlbum) {
        executorService.execute(() -> {
            try {
                cliente.restaurarAlbum(idAlbum);
                mainHandler.post(() -> {
                    albumes.remove(position);
                    adapter.notifyDataSetChanged();
                    mostrarTextoAlternativo();
                    Toast.makeText(requireContext(), "Álbum recuperado.", Toast.LENGTH_SHORT).show();
                });
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(getContext(), "Error al recuperar el álbum.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Método para mostrar un mensaje alternativo si no hay álbumes
    public void mostrarTextoAlternativo() {
        if (albumes.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        } else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }

    // Método para mostrar un mensaje de error genérico al cargar la interfaz
    public void errorAlCargarInterfaz() {
        Toast.makeText(getContext(), "Error al cargar los álbumes eliminados.", Toast.LENGTH_SHORT).show();
    }
}
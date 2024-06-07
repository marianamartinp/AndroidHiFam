package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

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

import com.mariana.androidhifam.databinding.FragmentGruposRecuperablesBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import utils.GridAdapter;

public class GruposRecuperablesFragment extends Fragment implements View.OnCreateContextMenuListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout, ModalFragment.CustomModalInterface {

    private @NonNull FragmentGruposRecuperablesBinding binding;
    private ArrayList<Grupo> grupos;
    private ArrayList<File> imagenesGrupos;
    private GridAdapter<Grupo> adapter;
    private CCAlbumFamiliar cliente;
    private Integer tokenUsuario;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;

    // Método llamado cuando se crea el Fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cliente = new CCAlbumFamiliar();
        grupos = new ArrayList<>();
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Método llamado cuando se crea la interfaz de usuario del Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Habilita el SwipeRefreshLayout y obtiene el token del usuario
        activity.findViewById(R.id.refreshLayout).setEnabled(true);
        // Obtención del binding.
        binding = FragmentGruposRecuperablesBinding.inflate(inflater, container, false);
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    // Método llamado después de que se haya creado la interfaz de usuario del Fragment
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Configura el SwipeRefreshLayout y el menú contextual
        MainActivity activity = (MainActivity) getActivity();
        activity.setRefreshLayout(this);
        registerForContextMenu(binding.gridView);
        SwipeRefreshLayout refreshLayout = activity.findViewById(R.id.refreshLayout);
        // Desactiva la opción de refrescar al hacer scroll en el grid.
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
        // Configura el listener para los ítems del GridView
        binding.gridView.setOnItemClickListener(this);
        cargarVistaGrupos(tokenUsuario, null);
    }

    // Método llamado al destruir la vista del Fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Método para manejar la selección de opciones en el menú contextual
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        int itemId = (int) binding.gridView.getAdapter().getItemId(position);
        int idMenuItem = item.getItemId();

        if (idMenuItem == R.id.recuperarGrupo) {
            modalRecuperarGrupo(position, itemId);
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
            inflater.inflate(R.menu.menu_grupos_recuperables, menu);
        }
    }

    // Método para manejar clics en las vistas
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (activity.getHabilitarInteraccion()) {
            modalRecuperarGrupo(position, (int) id);
        }
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh(SwipeRefreshLayout refreshLayout) {
        if (activity.getHabilitarInteraccion()) {
            cargarVistaGrupos(tokenUsuario, refreshLayout);
        }
        else {
            refreshLayout.setRefreshing(false);
        }
    }

    // Implementación del clic positivo en el modal personalizado
    @Override
    public void onPositiveClick(String idModal, Integer position, Integer id) {
        if (activity.getHabilitarInteraccion()) {
            recuperarGrupo(position, id);
        }
    }

    // Carga los grupos del usuario que han sido eliminados
    public void cargarGrupos(Integer tokenUsuario) {
        try {
            // Realiza la consulta de grupos y actualiza la interfaz
            LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
            filtros.put("g.COD_USUARIO_ADMIN_GRUPO", "=" + tokenUsuario);
            filtros.put("g.FECHA_ELIMINACION", "is not null");
            LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
            ordenacion.put("g.titulo", "asc");
            grupos = cliente.leerGrupos(filtros, ordenacion);
            mainHandler.post(this::actualizarInterfaz);
        }
        catch (ExcepcionAlbumFamiliar e) {
            errorAlCargarInterfaz();
        }
    }

    // Carga las imágenes en el GridView
    public void cargarGrid() {
        imagenesGrupos = activity.getImagenes();
        adapter = new GridAdapter<>(requireContext(), grupos, imagenesGrupos, false);
        binding.gridView.setAdapter(adapter);
    }

    // Actualiza la interfaz
    public void actualizarInterfaz() {
        cargarGrid();
        mostrarTextoAlternativo();
    }

    // Método para cargar la vista de los grupos llamando a métodos secundarios
    public void cargarVistaGrupos(Integer tokenUsuario, SwipeRefreshLayout refreshLayout) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> {
            cargarGrupos(tokenUsuario);
            mainHandler.post(() -> {
                binding.gridView.clearAnimation();
                if (null != refreshLayout) {
                    refreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Se han actualizado las familias eliminadas.", Toast.LENGTH_SHORT).show();
                }
                activity.setHabilitarInteraccion(true);
            });
        });
    }

    // Muestra un modal para confirmar la recuperación de un grupo
    public void modalRecuperarGrupo(int position, int id) {
        ModalFragment modal = new ModalFragment("recuperarGrupo", position, (int) id, this, "¿Desea recuperar esta familia?", getString(R.string.btnRecuperar), getString(R.string.btnCancelar));
        modal.show(activity.getSupportFragmentManager(), "modalRecuperarGrupo");
    }

    // Recupera un grupo eliminado
    public void recuperarGrupo(int position, int idGrupo) {
        executorService.execute(() -> {
            try {
                // Intenta restaurar el grupo y actualiza la interfaz
                cliente.restaurarGrupo(idGrupo);
                mainHandler.post(() -> {
                    grupos.remove(position);
                    adapter.notifyDataSetChanged();
                    mostrarTextoAlternativo();
                    Toast.makeText(requireContext(), "Familia recuperada.", Toast.LENGTH_SHORT).show();
                });
            } catch (ExcepcionAlbumFamiliar e) {
                Toast.makeText(getContext(), "Error al recuperar la familia.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Muestra un texto alternativo si no hay grupos
    public void mostrarTextoAlternativo() {
        if (grupos.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        } else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }

    // Método genérico para manejar errores al cargar la interfaz
    public void errorAlCargarInterfaz() {
        Toast.makeText(getContext(), "Error al cargar las familias eliminadas.", Toast.LENGTH_SHORT).show();
    }
}
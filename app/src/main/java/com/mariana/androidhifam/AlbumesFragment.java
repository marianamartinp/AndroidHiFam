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
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mariana.androidhifam.databinding.FragmentAlbumesBinding;

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
import pojosalbumfamiliar.Grupo;
import utils.GridAdapter;

public class AlbumesFragment extends Fragment implements View.OnClickListener, View.OnCreateContextMenuListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout, ModalFragment.CustomModalInterface {
    private @NonNull AlbumesFragmentArgs albumesFragmentArgs;
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

    // Método que se ejecuta al crear el fragmento
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recupera los argumentos pasados al Fragment
        if (getArguments() != null) {
            albumesFragmentArgs = AlbumesFragmentArgs.fromBundle(getArguments());
            idGrupo = albumesFragmentArgs.getIdGrupo();
        }
        // Inicializa el cliente para interactuar con el backend
        albumes = new ArrayList<>();
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Método que se ejecuta al crear la vista
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity.findViewById(R.id.refreshLayout).setEnabled(true);
        // Infla el layout del Fragment y obtiene una instancia del binding
        binding = FragmentAlbumesBinding.inflate(inflater, container, false);
        cliente = activity.getCliente();
        tokenUsuario = Integer.parseInt(activity.getToken());
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    // Método que se ejecuta una vez la vista ha sido creada
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Configuración de botones y eventos de la vista
        activity.setRefreshLayout(this);
        binding.botonNuevoAlbum.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
        binding.botonUsuarios.setOnClickListener(this);
        SwipeRefreshLayout refreshLayout = activity.findViewById(R.id.refreshLayout);
        binding.gridView.setOnScrollListener(new GridView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Desabilitar el refrescar al hacer scroll.
                if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    refreshLayout.setEnabled(false);
                } else {
                    refreshLayout.setEnabled(true);
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // No precisado
            }
        });

        // Cargar la vista de álbumes si ya se ha creado o si no (se cargarán de cero los datos
        // o solo se refrescará la interfaz
        if (vistaCreada) {
            resumirVistaAlbumes(idGrupo);
        }
        else {
            cargarVistaAlbumes(idGrupo, null);
        }
        registerForContextMenu(binding.gridView);

        // Listener para manejar el clic en los ítems del GridView
        binding.gridView.setOnItemClickListener(this);
    }

    // Método que se ejecuta al destruir la vista del fragmento
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

    }

    // Método para crear el menú contextual
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (activity.getHabilitarInteraccion()) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_context_albumes, menu);
        }
    }

    // Método para manejar la selección de opciones en el menú contextual
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (activity.getHabilitarInteraccion()) {
            int position = info.position;
            int itemId = (int) binding.gridView.getAdapter().getItemId(position);
            int idMenuItem = item.getItemId();

            if (idMenuItem == R.id.verAlbum) {
                navController.navigate(AlbumesFragmentDirections.actionAlbumesFragmentToDetallesAlbumFragment(itemId));
            }
        }
        return true;
    }

    // Método para manejar los eventos de clic en los botones
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

    // Método para manejar los eventos de clic en los ítems del GridView
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

    // Implementación del clic positivo en el modal personalizado
    @Override
    public void onPositiveClick(String idModal, Integer position, Integer id) {
        if (activity.getHabilitarInteraccion()) {
            eliminarGrupo(id);
        }
    }

    // Método para cargar la información del grupo padre
    public void cargarGrupo(Integer idGrupo, CountDownLatch latch) {
        try {
            grupo = cliente.leerGrupo(idGrupo);
            mainHandler.post(() -> {
                cargarTituloGrupo();
                visibilizarSolicitudes();
            });
        }catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            latch.countDown();
        }
    }

    // Método para aprobar la visibilidad de las solicitudes del grupo
    public void visibilizarSolicitudes() {
        if (Objects.equals(tokenUsuario, grupo.getUsuarioAdminGrupo().getCodUsuario())) {
            binding.botonUsuarios.setVisibility(View.VISIBLE);
        }
        else {
            binding.botonUsuarios.setVisibility(View.INVISIBLE);
        }
    }

    // Método para cargar el título del grupo en la interfaz
    public void cargarTituloGrupo() {
        if (null != grupo) {
            binding.tituloGrupo.setText(grupo.getTitulo());
        }
    }

    // Método para cargar los álbumes del grupo
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
            if (null != latch) {
                latch.countDown();
            }
        }
    }

    // Método para cargar las imágenes en el GridView
    public void cargarGrid() {
        imagenesAlbumes = activity.getImagenes();
        adapter = new GridAdapter<>(requireContext(), albumes, imagenesAlbumes, false);
        binding.gridView.setAdapter(adapter);
    }

    // Método para actualizar la interfaz con la información cargada
    public void actualizarInterfaz() {
        cargarTituloGrupo();
        cargarGrid();
        mostrarTextoAlternativo();
    }

    // Método para cargar las imágenes desde Drive
    public void cargarImagenesDrive(CountDownLatch latch) {
        try {
            activity.cargarImagenesDrive(false);
            mainHandler.post(this::actualizarInterfaz);
        } finally {
            latch.countDown();
        }
    }

    // Método para cargar la vista de álbumes llamando a métodos secundarios
    public void cargarVistaAlbumes(Integer idGrupo, SwipeRefreshLayout refreshLayout) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        CountDownLatch latch = new CountDownLatch(3);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> cargarGrupo(idGrupo, latch));
        executorService.execute(() -> cargarAlbumes(idGrupo, latch));
        executorService.execute(() -> cargarImagenesDrive(latch));
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

    // Método para resumir la vista de álbumes
    public void resumirVistaAlbumes(Integer idGrupo) {
        visibilizarSolicitudes();
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> {
            cargarAlbumes(idGrupo, null);
            mainHandler.post(() -> {
                binding.gridView.clearAnimation();
                activity.setHabilitarInteraccion(true);
                vistaCreada = true;
            });
        });
    }

    // Método para mostrar el menú emergente de opciones
    public void menuPopUp() {
        PopupMenu popup = new PopupMenu(requireActivity(), binding.botonOpciones);
        popup.getMenuInflater().inflate(R.menu.menu_opciones_grupos, popup.getMenu());
        if (null != grupo) {
            if (Objects.equals(tokenUsuario, grupo.getUsuarioAdminGrupo().getCodUsuario())) {
                popup.getMenu().clear();
                popup.getMenuInflater().inflate(R.menu.menu_opciones_grupos_admin, popup.getMenu());
            }
        }
        // Gestión de sus eventos
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (activity.getHabilitarInteraccion()) {
                    int idMenuItem = item.getItemId();

                    if (idMenuItem == R.id.eliminarGrupo) {
                        modalEliminarGrupo(idGrupo);
                    }
                    else if (idMenuItem == R.id.verGrupo) {
                        navController.navigate(AlbumesFragmentDirections.actionAlbumesFragmentToDetallesGrupoFragment(idGrupo));
                    }
                    else if (idMenuItem == R.id.verAlbumesEliminados) {
                        navController.navigate(AlbumesFragmentDirections.actionAlbumesFragmentToAlbumesRecuperablesFragment(idGrupo));
                    }
                    else if (idMenuItem == R.id.verIntegrantesGrupo) {
                        navController.navigate(AlbumesFragmentDirections.actionAlbumesFragmentToDetallesGrupoFragment2(idGrupo));
                    }
                }
                return true;
            }
        });
        popup.show();
    }

    // Método para eliminar un grupo
    public void eliminarGrupo(int idGrupo) {
        executorService.execute(() -> {
            try {
                cliente.eliminarGrupo(idGrupo);
                mainHandler.post(() -> {
                    navController.popBackStack();
                    Toast.makeText(requireContext(), "Familia eliminada.", Toast.LENGTH_SHORT).show();
                });
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(requireContext(), "Error al eliminar la familia.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Método para mostrar el modal de confirmación de eliminación de grupo
    public void modalEliminarGrupo(int id) {
        ModalFragment modal = new ModalFragment("eliminarGrupo", null, (int) id, this, "¿Desea eliminar esta familia?", getString(R.string.btnEliminar), getString(R.string.btnCancelar));
        modal.show(activity.getSupportFragmentManager(), "modalEliminarGrupo");
    }

    // Método para mostrar el texto alternativo si no hay álbumes
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

    // Método para mostrar un mensaje de error genérico al cargar la interfaz
    public void errorAlCargarInterfaz() {
        Toast.makeText(getContext(), "Error al cargar los álbumes.", Toast.LENGTH_SHORT).show();
    }
}
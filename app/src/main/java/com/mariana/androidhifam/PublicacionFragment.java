package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentPublicacionBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.Comentario;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;
import utils.ItemsListAdapter;
import utils.ListAdapter;
import utils.Utils;


public class PublicacionFragment extends Fragment implements View.OnClickListener, MainActivity.SwipeToRefreshLayout, ModalFragment.CustomModalInterface, ListAdapter.OnItemClickListener {
    private @NonNull PublicacionFragmentArgs publicacionFragmentArgs;
    private @NonNull FragmentPublicacionBinding binding;
    private NavController navController;
    private Publicacion publicacion;
    private ArrayList<Comentario> comentarios;
    private Integer idPublicacion, idAlbum, idGrupo, tokenUsuario;
    private Album album;
    private CCAlbumFamiliar cliente;
    private ListAdapter<Comentario> adapter;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private boolean vistaCreada = false;

    // Método onCreate para la inicialización del fragmento
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recuperar argumentos
        if (getArguments() != null) {
            publicacionFragmentArgs = PublicacionFragmentArgs.fromBundle(getArguments());
            idPublicacion = publicacionFragmentArgs.getIdPublicacion();
            idAlbum = publicacionFragmentArgs.getIdAlbum();
            idGrupo = publicacionFragmentArgs.getIdGrupo();
        }
        // Obtener actividad y configurar variables
        activity = (MainActivity) getActivity();
        comentarios = new ArrayList<>();
        publicacion = new Publicacion();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Método onCreateView para inflar el diseño de la vista del fragmento
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        binding = FragmentPublicacionBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
        tokenUsuario = Integer.parseInt(activity.getToken());
        cliente = activity.getCliente();
        return binding.getRoot();
    }

    // Método onViewCreated para configurar la vista después de que se haya creado
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Configurar el SwipeRefreshLayout
        activity.setRefreshLayout(this);
        binding.botonNuevoComentario.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
        // Verificar si la vista ya ha sido creada
        if (vistaCreada) {
            executorService.execute(() -> cargarPublicacion(idPublicacion, null));
            cargarListaComentarios();
        }
        else {
            cargarAlbum(idAlbum);
            cargarVistaPublicacion(idPublicacion, idAlbum, null);
        }
    }

    // Método onDestroyView para limpiar la vista cuando el fragmento está destruido
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Liberar la referencia al binding
        binding = null;
    }

    // Método onClick para manejar los clics en los elementos de la vista
    @Override
    public void onClick(View v) {
        if (activity.getHabilitarInteraccion()) {
            int id = v.getId();
            if (id == R.id.botonNuevoComentario) {
                navController.navigate(PublicacionFragmentDirections.actionPublicacionFragmentToNuevoComentarioFragment(idPublicacion));
            } else if (id == R.id.botonOpciones) {
                menuPopUp();
            }
        }
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh(SwipeRefreshLayout refreshLayout) {
        if (activity.getHabilitarInteraccion()) {
            cargarVistaPublicacion(idPublicacion, idAlbum, refreshLayout);
        }
        else {
            refreshLayout.setRefreshing(false);
        }
    }

    // Implementación de CustomModalInterface: define las acciones al interactuar con un diálogo modal personalizado
    @Override
    public void onPositiveClick(String idModal, Integer position, Integer id) {
        if (activity.getHabilitarInteraccion()) {
            switch (idModal) {
                case "eliminarPublicacion":
                    if (null != publicacion) {
                        eliminarPublicacion(publicacion);
                    }
                    break;
                case "eliminarComentario":
                    eliminarComentario(id, position);
                    break;
            }
        }
    }

    // Implementación de OnItemClickListener: maneja los clics en los elementos del RecyclerView de los comentarios
    @Override
    public void onItemClick(Object item, int position, int idButton) {
        if (activity.getHabilitarInteraccion()) {
            if (idButton == R.id.iconoEliminar) {
                modalEliminarComentario(position, ((Comentario) item).getCodComentario());
            }
        }
    }

    // Método para cargar el título del álbum
    public void cargarTituloAlbum(Integer idAlbum, CountDownLatch latch) {
        try {
            String tituloAlbum = cliente.leerAlbum(idAlbum).getTitulo();
            mainHandler.post(() -> binding.tituloAlbum.setText(tituloAlbum));
        }
        catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            latch.countDown();
        }
    }

    // Método para cargar la información del álbum
    public void cargarAlbum(Integer idAlbum) {
        executorService.execute(() -> {
            try {
                album = cliente.leerAlbum(idAlbum);
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(getContext(), "Se ha producido un error.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Método para cargar la publicación
    public void cargarPublicacion(Integer idPublicacion, CountDownLatch latch) {
        try {
            publicacion = cliente.leerPublicacion(idPublicacion);
            mainHandler.post(this::cargarFramePublicacion);
        }
        catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            if (null != latch) {
                latch.countDown();
            }
        }
    }

    // Método para cargar los comentarios de la publicación
    public void cargarComentarios(Integer idPublicacion, CountDownLatch latch) {
        // Filtros y ordenación para obtener los comentarios
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("c.COD_PUBL_TIENE_COMENTARIO", "="+idPublicacion);
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("c.FECHA_CREACION", "desc");
        try {
            comentarios = cliente.leerComentarios(filtros,ordenacion);
            mainHandler.post(this::cargarListaComentarios);
        } catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            if (null != latch) {
                latch.countDown();
            }
        }
    }

    // Método para cargar la lista de comentarios en el RecyclerView
    public void cargarListaComentarios() {
        if (!comentarios.isEmpty()) {
            // Crear un adaptador para la lista de comentarios
            adapter = new ListAdapter<>(requireContext(), comentarios, ItemsListAdapter.ITEM_COMENTARIO, this,
            album.getGrupoCreaAlbum().getUsuarioAdminGrupo().getCodUsuario(), album.getUsuarioAdminAlbum().getCodUsuario(), tokenUsuario);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerView.setAdapter(adapter);
        }
        else {
            // Mostrar un texto alternativo si no hay comentarios
            mostrarTextoAlternativo();
        }
    }

    // Método para cargar la información de la publicación en la interfaz
    public void cargarFramePublicacion() {
        String usuario = publicacion.getUsuarioCreaPublicacion().getUsuario();
        String fecha = Utils.parsearDateAString(publicacion.getFechaCreacion());
        binding.infoAdicional.setText("@" + usuario + ", " + fecha);
        binding.tituloDescripcion.setText(publicacion.getTitulo() + ": " + publicacion.getTexto());
        // Establecer la imagen de la publicación
        establecerImagen();
    }

    // Método para establecer la imagen de la publicación
    public void establecerImagen() {
        // Obtener la lista de imágenes de la actividad
        ArrayList<File> imagenes = activity.getImagenes();
        if (!imagenes.isEmpty()) {
            for (File imagenLista : imagenes) {
                // Buscar la imagen correspondiente a la publicación
                if (imagenLista.getName().equals(publicacion.getArchivo().getTitulo())) {
                    // Decodificar el archivo de imagen y establecerlo en ImageView
                    Bitmap bitmap = BitmapFactory.decodeFile(imagenLista.getAbsolutePath());
                    if (bitmap != null) {
                        binding.imagen.setImageBitmap(bitmap);
                        break;
                    } else {
                        Log.e("ImageView", "Failed to decode the image file: " + imagenLista.getAbsolutePath());
                    }
                }
            }
        }
    }

    // Método para cargar la vista de la publicación llamando a métodos secundarios
    public void cargarVistaPublicacion(Integer idPublicacion, Integer idAlbum, SwipeRefreshLayout refreshLayout) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        CountDownLatch latch = new CountDownLatch(3);
        binding.cardView.startAnimation(parpadeo);
        executorService.execute(() -> cargarPublicacion(idPublicacion, latch));
        executorService.execute(() -> cargarTituloAlbum(idAlbum, latch));
        executorService.execute(() -> cargarComentarios(idPublicacion, latch));
        executorService.execute(() -> {
            try {
                latch.await();
                mainHandler.post(() -> {
                    binding.cardView.clearAnimation();
                    if (null != refreshLayout) {
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "Se ha actualizado la publicación.", Toast.LENGTH_SHORT).show();
                    }
                    activity.setHabilitarInteraccion(true);
                    vistaCreada = true;
                });
            } catch (InterruptedException e) {
                mainHandler.post(this::errorAlCargarInterfaz);
            }
        });
    }

    // Método para mostrar el menú emergente de opciones
    public void menuPopUp() {
        PopupMenu popup = new PopupMenu(requireActivity(), binding.botonOpciones);
        popup.getMenuInflater().inflate(R.menu.menu_opciones_publicacion, popup.getMenu());
        if (null != publicacion && null != album) {
            if (Objects.equals(tokenUsuario, publicacion.getUsuarioCreaPublicacion().getCodUsuario())) {
                popup.getMenu().clear();
                popup.getMenuInflater().inflate(R.menu.menu_opciones_publicacion_creador, popup.getMenu());
            }
            else if (Objects.equals(tokenUsuario, album.getGrupoCreaAlbum().getUsuarioAdminGrupo().getCodUsuario()) ||
                     Objects.equals(tokenUsuario, album.getUsuarioAdminAlbum().getCodUsuario())) {
                popup.getMenu().clear();
                popup.getMenuInflater().inflate(R.menu.menu_opciones_publicacion_admin, popup.getMenu());
            }
        }
        // Establecer el listener de clic en los elementos del menú
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (activity.getHabilitarInteraccion()) {
                    int idMenuItem = item.getItemId();
                    if (idMenuItem == R.id.eliminarPublicacion) {
                        modalEliminarPublicacion(idPublicacion);
                    }
                    else if (idMenuItem == R.id.verGrupo) {
                        navController.navigate(PublicacionFragmentDirections.actionPublicacionFragmentToDetallesGrupoFragment(idGrupo));
                        return true;
                    }
                    else if (idMenuItem == R.id.modificarPublicacion) {
                        navController.navigate(PublicacionFragmentDirections.actionPublicacionFragmentToModificarPublicacionFragment(idPublicacion, idGrupo, idAlbum));
                    }
                    else if (idMenuItem == R.id.verAlbum) {
                        navController.navigate(PublicacionFragmentDirections.actionPublicacionFragmentToDetallesAlbumFragment(idAlbum));
                        return true;
                    }
                }
                return true;
            }
        });
        popup.show();
    }

    // Método para mostrar un modal de confirmación para eliminar una publicación
    public void modalEliminarPublicacion(int id) {
        ModalFragment modal = new ModalFragment("eliminarPublicacion", 1, (int) id, this, "¿Desea eliminar esta publicación?", getString(R.string.btnEliminar), getString(R.string.btnCancelar));
        modal.show(activity.getSupportFragmentManager(), "modalEliminarPublicacion");
    }

    // Método para mostrar un modal de confirmación para eliminar un comentario
    public void modalEliminarComentario(int position, int id) {
        ModalFragment modal = new ModalFragment("eliminarComentario", position, (int) id, this, "¿Desea eliminar este comentario?", getString(R.string.btnEliminar), getString(R.string.btnCancelar));
        modal.show(activity.getSupportFragmentManager(), "modalEliminarComentario");
    }

    // Método para eliminar una publicación
    public void eliminarPublicacion(Publicacion publicacion) {
        CountDownLatch latch = new CountDownLatch(2);
        executorService.execute(() -> {
            try {
                // Eliminar el archivo asociado a la publicación en Google Drive
                activity.getDriveServiceHelper().deleteFile(publicacion.getArchivo().getRuta());
            }
            catch (Exception e) {
                Toast.makeText(getContext(), "Error al conectar con Google Drive.", Toast.LENGTH_SHORT).show();
            }
            finally {
                latch.countDown();
            }
        });
        executorService.execute(() -> {
            try {
                // Eliminar la publicación del servidor
                cliente.eliminarPublicacion(publicacion.getCodPublicacion());
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(requireContext(), "Error al eliminar la publicación.", Toast.LENGTH_SHORT).show());
            }
            finally {
                latch.countDown();
            }
        });
        executorService.execute(() -> {
            try {
                latch.await();
                mainHandler.post(() -> {
                    navController.popBackStack();
                    Toast.makeText(requireContext(), "Publicación eliminada.", Toast.LENGTH_SHORT).show();
                });
            } catch (InterruptedException e) {
                mainHandler.post(() -> Toast.makeText(requireContext(), "Error al eliminar la publicación.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Método para manejar errores al cargar la interfaz
    public void errorAlCargarInterfaz() {
        Toast.makeText(getContext(), "Error al cargar la publicación.", Toast.LENGTH_SHORT).show();
    }

    // Método para mostrar un texto alternativo si no hay comentarios
    public void mostrarTextoAlternativo() {
        if (comentarios.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        }
        else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }

    // Método para eliminar un comentario
    public void eliminarComentario(int idComentario, int position) {
        executorService.execute(() -> {
            try {
                // Eliminar el comentario
                int resultado = cliente.eliminarComentario(idComentario);
                mainHandler.post(() -> {
                    if (resultado > 0) {
                        // Actualizar la lista de comentarios si se eliminó con éxito
                        comentarios.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, comentarios.size() - position);
                        mostrarTextoAlternativo();
                    }
                });
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(getContext(), "Error al eliminar el comentario.", Toast.LENGTH_SHORT).show());
            }
        });

    }
}
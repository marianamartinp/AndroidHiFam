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
import java.text.SimpleDateFormat;
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


public class PublicacionFragment extends Fragment implements View.OnClickListener, MainActivity.SwipeToRefreshLayout, ModalFragment.CustomModalInterface, ListAdapter.OnItemClickListener {
    private PublicacionFragmentArgs publicacionFragmentArgs;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicacionFragmentArgs = PublicacionFragmentArgs.fromBundle(getArguments());
            idPublicacion = publicacionFragmentArgs.getIdPublicacion();
            idAlbum = publicacionFragmentArgs.getIdAlbum();
            idGrupo = publicacionFragmentArgs.getIdGrupo();
        }
        activity = (MainActivity) getActivity();
        comentarios = new ArrayList<>();
        publicacion = new Publicacion();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPublicacionBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
        tokenUsuario = Integer.parseInt(activity.getToken());
        cliente = activity.getCliente();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setRefreshLayout(this);
        binding.botonNuevoComentario.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
        if (vistaCreada) {
            cargarFramePublicacion();
            cargarListaComentarios();
        }
        else {
            cargarAlbum(idAlbum);
            cargarVistaPublicacion(idPublicacion, null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

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

    public void cargarAlbum(Integer idAlbum) {
        executorService.execute(() -> {
            try {
                album = cliente.leerAlbum(idAlbum);
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(getContext(), "Se ha producido un error.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void cargarPublicacion(Integer idPublicacion, CountDownLatch latch) {
        try {
            publicacion = cliente.leerPublicacion(idPublicacion);
            mainHandler.post(this::cargarFramePublicacion);
        }
        catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            latch.countDown();
        }
    }

    public void cargarComentarios(Integer idPublicacion, CountDownLatch latch) {
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

    public void cargarListaComentarios() {
        if (!comentarios.isEmpty()) {
            adapter = new ListAdapter<>(requireContext(), comentarios, ItemsListAdapter.ITEM_COMENTARIO, this,
            album.getGrupoCreaAlbum().getUsuarioAdminGrupo().getCodUsuario(), album.getUsuarioAdminAlbum().getCodUsuario(), tokenUsuario);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerView.setAdapter(adapter);
        }
        else {
            mostrarTextoAlternativo();
        }
    }
    public void cargarFramePublicacion() {
        String usuario = publicacion.getUsuarioCreaPublicacion().getUsuario();
        String fecha = Utils.parsearDateAString(publicacion.getFechaCreacion());
        binding.infoAdicional.setText("@" + usuario + ", " + fecha);
        binding.tituloDescripcion.setText(publicacion.getTitulo() + ": " + publicacion.getTexto());
        establecerImagen();
    }

    public void establecerImagen() {
        ArrayList<File> imagenes = activity.getImagenes();
        if (!imagenes.isEmpty()) {
            for (File imagenLista : imagenes) {
                if (imagenLista.getName().equals(publicacion.getArchivo().getTitulo())) {
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

    public void cargarVistaPublicacion(Integer idPublicacion, SwipeRefreshLayout refreshLayout) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        CountDownLatch latch = new CountDownLatch(3);
        binding.cardView.startAnimation(parpadeo);
        executorService.execute(() -> cargarPublicacion(idPublicacion, latch));
        executorService.execute(() -> cargarTituloAlbum(publicacion.getPublicacionEnAlbum(), latch));
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

    public void menuPopUp() {
        PopupMenu popup = new PopupMenu(requireActivity(), binding.botonOpciones);
        popup.getMenuInflater().inflate(R.menu.menu_opciones_publicacion, popup.getMenu());
        if (null != publicacion && null != album) {
            if (Objects.equals(tokenUsuario, publicacion.getUsuarioCreaPublicacion().getCodUsuario()) ||
                Objects.equals(tokenUsuario, album.getGrupoCreaAlbum().getUsuarioAdminGrupo().getCodUsuario()) ||
                Objects.equals(tokenUsuario, album.getUsuarioAdminAlbum().getCodUsuario())) {
                popup.getMenu().clear();
                popup.getMenuInflater().inflate(R.menu.menu_opciones_publicacion_admin, popup.getMenu());
            }
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (activity.getHabilitarInteraccion()) {
                    if (activity.getHabilitarInteraccion()) {
                        int idMenuItem = item.getItemId();
                        if (idMenuItem == R.id.eliminarPublicacion) {
                            modalEliminarPublicacion(idPublicacion);
                        }
                        else if (idMenuItem == R.id.verGrupo) {
                            navController.navigate(PublicacionFragmentDirections.actionPublicacionFragmentToDetallesGrupoFragment(idGrupo));
                            return true;
                        }
                    }
                }
                return true;
            }
        });
        popup.show();
    }
//
//    public void actualizarAdapter(){
//        adapter.notifyDataSetChanged();
//    }

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
            cargarVistaPublicacion(idPublicacion, refreshLayout);
        }
        else {
            refreshLayout.setRefreshing(false);
        }
    }

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

    public void modalEliminarPublicacion(int id) {
        ModalFragment modal = new ModalFragment("eliminarPublicacion", 1, (int) id, this, "¿Desea eliminar esta publicación?", getString(R.string.btnEliminar), getString(R.string.btnCancelar));
        modal.show(activity.getSupportFragmentManager(), "modalEliminarPublicacion");
    }

    public void modalEliminarComentario(int position, int id) {
        ModalFragment modal = new ModalFragment("eliminarComentario", position, (int) id, this, "¿Desea eliminar este comentario?", getString(R.string.btnEliminar), getString(R.string.btnCancelar));
        modal.show(activity.getSupportFragmentManager(), "modalEliminarComentario");
    }

    public void eliminarPublicacion(Publicacion publicacion) {
        CountDownLatch latch = new CountDownLatch(2);
        executorService.execute(() -> {
            try {
                activity.getDriveServiceHelper().deleteFile(publicacion.getArchivo().getRuta());
            }
            finally {
                latch.countDown();
            }
        });
        executorService.execute(() -> {
            try {
                cliente.eliminarPublicacion(publicacion.getCodPublicacion());
                mainHandler.post(() -> {
                    navController.popBackStack();
                    Toast.makeText(requireContext(), "Publicación eliminada.", Toast.LENGTH_SHORT).show();
                });
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(requireContext(), "Error al eliminar la publicación.", Toast.LENGTH_SHORT).show());
            }
            finally {
                latch.countDown();
            }
        });
    }


    public void errorAlCargarInterfaz() {
        Toast.makeText(getContext(), "Error al cargar la publicación.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(Object item, int position, int idButton) {
        if (activity.getHabilitarInteraccion()) {
            if (idButton == R.id.iconoEliminar) {
                modalEliminarComentario(position, ((Comentario) item).getCodComentario());
            }
        }
    }

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

    public void eliminarComentario(int idComentario, int position) {
        executorService.execute(() -> {
            try {
                int resultado = cliente.eliminarComentario(idComentario);
                mainHandler.post(() -> {
                    if (resultado > 0) {
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
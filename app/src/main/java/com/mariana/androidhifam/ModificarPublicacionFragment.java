package com.mariana.androidhifam;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mariana.androidhifam.databinding.FragmentModificarPublicacionBinding;
import com.mariana.androidhifam.databinding.FragmentNuevaPublicacionBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import driveapi.DatosArchivo;
import pojosalbumfamiliar.Archivo;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;
import pojosalbumfamiliar.Usuario;

public class ModificarPublicacionFragment extends Fragment implements View.OnClickListener, MainActivity.SwipeToRefreshLayout {

    private @NonNull ModificarPublicacionFragmentArgs nuevaPublicacionFragmentArgs;
    private @NonNull FragmentModificarPublicacionBinding binding;
    private CCAlbumFamiliar cliente;
    private Integer idAlbum, idGrupo, tokenUsuario, idPublicacion;
    private Publicacion publicacion;
    private MainActivity activity;
    private NavController navController;
    private Uri uriImagen;
    private ExecutorService executorService;
    private Handler mainHandler;

    // Método onCreate para inicializar la actividad
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtener los argumentos pasados al fragmento
        if (getArguments() != null) {
            nuevaPublicacionFragmentArgs = ModificarPublicacionFragmentArgs.fromBundle(getArguments());
            idAlbum = nuevaPublicacionFragmentArgs.getIdAlbum();
            idGrupo = nuevaPublicacionFragmentArgs.getIdGrupo();
            idPublicacion = nuevaPublicacionFragmentArgs.getIdPublicacion();
        }
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Método onCreateView para inflar el diseño de la vista del fragmento
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        navController = NavHostFragment.findNavController(this);
        // Inflar el diseño del fragmento
        binding = FragmentModificarPublicacionBinding.inflate(inflater, container, false);
        cliente = activity.getCliente();
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    // Método onViewCreated para configurar la vista después de que se haya creado
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setRefreshLayout(this);
        // Cargar la vista de la publicación con el ID específico
        cargarVistaPublicacion(idPublicacion, idAlbum,null);
        // Configurar los clics en los elementos de la vista
        binding.botonModificarPublicacion.setOnClickListener(this);
        binding.cardView.setOnClickListener(this);
        binding.botonAtras.setOnClickListener(this);
    }

    // Método onDestroyView para limpiar la vista cuando el fragmento está destruido
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    // Método onClick para manejar los clics en los elementos de la vista
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonModificarPublicacion) {
            modificarPublicacion(idPublicacion, idAlbum, tokenUsuario);
        }
        else if (id == R.id.cardView) {
            seleccionarImagen();
        }
        else if (id == R.id.botonAtras) {
            navController.popBackStack();
        }
    }

    // Método para cargar el título del álbum
    public void cargarTituloAlbum(int idAlbum, CountDownLatch latch) {
        try {
            String titulo = getString(R.string.tituloAlbumFormularioPublicacion, cliente.leerAlbum(idAlbum).getTitulo());
            mainHandler.post(() -> binding.tituloAlbum.setText(titulo));
        } catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(() -> Toast.makeText(getContext(), "Error al cargar el título del álbum.", Toast.LENGTH_SHORT).show());
        }
        finally {
            latch.countDown();
        }
    }

    // Método para cargar la publicación
    public void cargarPublicacion(int idPublicacion, CountDownLatch latch) {
        try {
            publicacion = cliente.leerPublicacion(idPublicacion);
            mainHandler.post(this::cargarFramePublicacion);
        } catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            latch.countDown();
        }
    }

    // Método para modificar la publicación
    public void modificarPublicacion(Integer idPublicacion, Integer idAlbum, Integer tokenUsuario) {
        String tituloPublicacion = binding.tituloPublicacion.getText().toString().trim();
        String textoPublicacion = binding.textoPublicacion.getText().toString().trim();
        if (!tituloPublicacion.isEmpty() && !textoPublicacion.isEmpty()) {
            // Eliminar la imagen anterior de Google Drive de forma asíncrona
            if (null != uriImagen) {
                executorService.execute(this::eliminarImagenDrive);
            }
            executorService.execute(() -> {
                try {
                    // Subir la nueva imagen a Google Drive de forma asíncrona, si está presente
                    DatosArchivo datosArchivo = null;
                    if (null != uriImagen) {
                        datosArchivo = activity.getDriveServiceHelper().uploadImageFile(uriImagen, idGrupo, idAlbum);
                    }
                    // Construir la nueva publicación con los datos actualizados
                    Publicacion publicacion = construirPublicacion(datosArchivo, tokenUsuario);
                    cliente.modificarPublicacion(idPublicacion, publicacion);
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "La publicación se ha modificado correctamente.", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                    });
                } catch (ExcepcionAlbumFamiliar e) {
                    mainHandler.post(() -> Toast.makeText(getContext(), e.getMensajeUsuario(), Toast.LENGTH_SHORT).show());
                }
                catch (Exception e) {
                    mainHandler.post(() -> Toast.makeText(getContext(), "Error al conectar con Google Drive.", Toast.LENGTH_SHORT).show());
                }
            });
        }
        else {
            Toast.makeText(requireContext(), "Añade un título, un texto y una imagen.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para construir una nueva publicación con los datos actualizados
    public Publicacion construirPublicacion(DatosArchivo datosArchivo, int tokenUsuario) throws ExcepcionAlbumFamiliar {
        String titulo = binding.tituloPublicacion.getText().toString();
        String texto = binding.textoPublicacion.getText().toString();
        Publicacion publicacionModificada = new Publicacion();
        publicacionModificada.setPublicacionEnAlbum(idAlbum);
        publicacionModificada.setTitulo(titulo);
        publicacionModificada.setTexto(texto);
        publicacionModificada.setFechaCreacion(publicacion.getFechaCreacion());
        Usuario usuario = new Usuario();
        usuario.setCodUsuario(tokenUsuario);
        publicacionModificada.setUsuarioCreaPublicacion(usuario);
        if (null != datosArchivo) {
            Archivo archivo = new Archivo(null, datosArchivo.getNombre(), datosArchivo.getArchivoId());
            publicacionModificada.setArchivo(archivo);
        }
        else {
            publicacionModificada.setArchivo(publicacion.getArchivo());
        }
        return publicacionModificada;
    }

    // Método para seleccionar una imagen de la galería
    public void seleccionarImagen() {
        // Crear un intent para abrir la galería de imágenes
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        lanzadorSelectorArchivo.launch(intent);
    }

    // Lanzador para la selección de archivos de la galería
    ActivityResultLauncher<Intent> lanzadorSelectorArchivo = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    uriImagen = data.getData();
                    if (uriImagen != null) {
                        // Ocultar el texto de "Más" y mostrar la imagen seleccionada
                        binding.textoMas.setVisibility(View.INVISIBLE);
                        binding.imagen.setImageURI(uriImagen);
                    }
                }
            }
        }
    );

    // Método para eliminar la imagen anterior de Google Drive
    public void eliminarImagenDrive() {
        try {
            activity.getDriveServiceHelper().deleteFile(publicacion.getArchivo().getRuta());
        }
        catch (Exception e) {
            Toast.makeText(getContext(), "Error al conectar con Google Drive.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para cargar la vista de la publicación
    public void cargarFramePublicacion() {
        binding.tituloPublicacion.setText(publicacion.getTitulo());
        binding.textoPublicacion.setText(publicacion.getTexto());
        establecerImagen();
    }

    // Método para establecer la imagen de la publicación
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

    // Método para cargar la vista de la publicación llamando a métodos secundarios
    public void cargarVistaPublicacion(Integer idPublicacion, Integer idAlbum, SwipeRefreshLayout refreshLayout) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        CountDownLatch latch = new CountDownLatch(2);
        binding.cardView.startAnimation(parpadeo);
        executorService.execute(() -> cargarPublicacion(idPublicacion, latch));
        executorService.execute(() -> cargarTituloAlbum(idAlbum, latch));
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
                });
            } catch (InterruptedException e) {
                mainHandler.post(this::errorAlCargarInterfaz);
            }
        });
    }

    // Método para mostrar un mensaje de error genérico al cargar la interfaz
    public void errorAlCargarInterfaz() {
        Toast.makeText(getContext(), "Error al cargar la publicación.", Toast.LENGTH_SHORT).show();
    }
}
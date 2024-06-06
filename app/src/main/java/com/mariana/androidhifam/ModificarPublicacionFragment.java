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

    private ModificarPublicacionFragmentArgs nuevaPublicacionFragmentArgs;
    private @NonNull FragmentModificarPublicacionBinding binding;
    private CCAlbumFamiliar cliente;
    private Integer idAlbum, idGrupo, tokenUsuario, idPublicacion;
    private Publicacion publicacion;
    private MainActivity activity;
    private NavController navController;
    private Uri uriImagen;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        navController = NavHostFragment.findNavController(this);
        binding = FragmentModificarPublicacionBinding.inflate(inflater, container, false);
        cliente = activity.getCliente();
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarVistaPublicacion(idPublicacion, idAlbum,null);
        binding.botonModificarPublicacion.setOnClickListener(this);
        binding.cardView.setOnClickListener(this);
        binding.botonAtras.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

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

    public void modificarPublicacion(Integer idPublicacion, Integer idAlbum, Integer tokenUsuario) {
        String tituloPublicacion = binding.tituloPublicacion.getText().toString().trim();
        String textoPublicacion = binding.textoPublicacion.getText().toString().trim();
        if (!tituloPublicacion.isEmpty() && !textoPublicacion.isEmpty()) {
            if (null != uriImagen) {
                executorService.execute(this::eliminarImagenDrive);
            }
            executorService.execute(() -> {
                try {
                    DatosArchivo datosArchivo = null;
                    if (null != uriImagen) {
                        datosArchivo = activity.getDriveServiceHelper().uploadImageFile(uriImagen, idGrupo, idAlbum);
                    }
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

    public void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        lanzadorSelectorArchivo.launch(intent);
    }

    ActivityResultLauncher<Intent> lanzadorSelectorArchivo = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    uriImagen = data.getData();
                    if (uriImagen != null) {
                        binding.textoMas.setVisibility(View.INVISIBLE);
                        binding.imagen.setImageURI(uriImagen);
                    }
                }
            }
        }
    );

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

    public void eliminarImagenDrive() {
        try {
            activity.getDriveServiceHelper().deleteFile(publicacion.getArchivo().getRuta());
        }
        catch (Exception e) {
            Toast.makeText(getContext(), "Error al conectar con Google Drive.", Toast.LENGTH_SHORT).show();
        }
    }

    public void cargarFramePublicacion() {
        binding.tituloPublicacion.setText(publicacion.getTitulo());
        binding.textoPublicacion.setText(publicacion.getTexto());
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

    public void errorAlCargarInterfaz() {
        Toast.makeText(getContext(), "Error al cargar la publicación.", Toast.LENGTH_SHORT).show();
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
}
package com.mariana.androidhifam;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.mariana.androidhifam.databinding.FragmentNuevaPublicacionBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import driveapi.DatosArchivo;
import pojosalbumfamiliar.Archivo;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;
import pojosalbumfamiliar.Usuario;

public class NuevaPublicacionFragment extends Fragment implements View.OnClickListener {

    private @NonNull NuevaPublicacionFragmentArgs nuevaPublicacionFragmentArgs;
    private @NonNull FragmentNuevaPublicacionBinding binding;
    private CCAlbumFamiliar cliente;
    private Integer idAlbum, idGrupo, tokenUsuario;
    private MainActivity activity;
    private NavController navController;
    private Uri uriImagen;
    private ExecutorService executorService;
    private Handler mainHandler;

    // Método onCreate para la inicialización del fragmento
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtener los argumentos pasados al fragmento
        if (getArguments() != null) {
            nuevaPublicacionFragmentArgs = NuevaPublicacionFragmentArgs.fromBundle(getArguments());
            idAlbum = nuevaPublicacionFragmentArgs.getIdAlbum();
            idGrupo = nuevaPublicacionFragmentArgs.getIdGrupo();
        }
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Método onCreateView para inflar el diseño de la vista del fragmento
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity.findViewById(R.id.refreshLayout).setEnabled(false);
        navController = NavHostFragment.findNavController(this);
        // Inflar el diseño del fragmento
        binding = FragmentNuevaPublicacionBinding.inflate(inflater, container, false);
        cliente = activity.getCliente();
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    // Método onViewCreated para configurar la vista después de que se haya creado
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.botonCrearPublicacion.setOnClickListener(this);
        binding.cardView.setOnClickListener(this);
        binding.botonAtras.setOnClickListener(this);
        // Cargar el título del álbum
        cargarTituloAlbum(idAlbum);
    }

    // Método onDestroyView para limpiar la vista cuando el fragmento está destruido
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Método onClick para manejar los clics en los elementos de la vista
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonCrearPublicacion) {
            crearNuevaPublicacion(idAlbum, tokenUsuario);
        }
        else if (id == R.id.cardView) {
            seleccionarImagen();
        }
        else if (id == R.id.botonAtras) {
            navController.popBackStack();
        }
    }

    // Método para cargar el título del álbum
    public void cargarTituloAlbum(int idAlbum) {
        executorService.execute(() ->  {
            try {
                String titulo = getString(R.string.tituloAlbumFormularioPublicacion, cliente.leerAlbum(idAlbum).getTitulo());
                mainHandler.post(() -> binding.tituloAlbum.setText(titulo));
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(getContext(), "Error al cargar el título del álbum.", Toast.LENGTH_SHORT).show());
            }
        });

    }

    // Método para crear una nueva publicación llamando a métodos secundarios
    public void crearNuevaPublicacion(Integer idAlbum, Integer tokenUsuario) {
        String tituloPublicacion = binding.tituloPublicacion.getText().toString().trim();
        String textoPublicacion = binding.textoPublicacion.getText().toString().trim();
        if (!tituloPublicacion.isEmpty() && !textoPublicacion.isEmpty() & null != uriImagen) {
            executorService.execute(() -> {
                try {
                    // Subir la imagen a Google Drive de forma asíncrona
                    DatosArchivo datosArchivo = activity.getDriveServiceHelper().uploadImageFile(uriImagen, idGrupo, idAlbum);
                    // Insertar la nueva publicación en el álbum
                    Publicacion publicacion = construirPublicacion(datosArchivo, tokenUsuario);
                    cliente.insertarPublicacion(publicacion);
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "La publicación se ha creado correctamente.", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                    });
                } catch (ExcepcionAlbumFamiliar e) {
                    mainHandler.post(() -> Toast.makeText(getContext(), e.getMensajeUsuario(), Toast.LENGTH_SHORT).show());
                }
                catch (Exception e) {
                    Toast.makeText(getContext(), "Error al conectar con Google Drive.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(requireContext(), "Añade un título, un texto y una imagen.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para construir una nueva publicación con los datos de la imagen
    public Publicacion construirPublicacion(DatosArchivo datosArchivo, int tokenUsuario) throws ExcepcionAlbumFamiliar {
        String titulo = binding.tituloPublicacion.getText().toString();
        String texto = binding.textoPublicacion.getText().toString();
        if (null != datosArchivo.getArchivoId()) {
            Publicacion publicacion = new Publicacion();
            publicacion.setPublicacionEnAlbum(idAlbum);
            publicacion.setTitulo(titulo);
            publicacion.setTexto(texto);
            Usuario usuario = new Usuario();
            usuario.setCodUsuario(tokenUsuario);
            publicacion.setUsuarioCreaPublicacion(usuario);
            Archivo archivo = new Archivo(null, datosArchivo.getNombre(), datosArchivo.getArchivoId());
            publicacion.setArchivo(archivo);
            return publicacion;
        }
        return null;
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
}
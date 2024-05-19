package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.mariana.androidhifam.databinding.FragmentNuevaPublicacionBinding;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ccalbumfamiliar.CCAlbumFamiliar;
import driveapi.DatosArchivo;
import pojosalbumfamiliar.Archivo;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;
import pojosalbumfamiliar.Usuario;
import androidx.activity.result.ActivityResultLauncher;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class NuevaPublicacionFragment extends Fragment implements View.OnClickListener {

    private NuevaPublicacionFragmentArgs nuevaPublicacionFragmentArgs;
    private @NonNull FragmentNuevaPublicacionBinding binding;
    private CCAlbumFamiliar cliente;
    private Integer idAlbum, idGrupo;
    private MainActivity activity;
    private NavController navController;
    private Uri uriImagen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nuevaPublicacionFragmentArgs = NuevaPublicacionFragmentArgs.fromBundle(getArguments());
            idAlbum = nuevaPublicacionFragmentArgs.getIdAlbum();
            idGrupo = nuevaPublicacionFragmentArgs.getIdGrupo();
        }
        cliente = new CCAlbumFamiliar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        navController = NavHostFragment.findNavController(this);
        binding = FragmentNuevaPublicacionBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.botonCrearPublicacion.setOnClickListener(this);
        binding.cardView.setOnClickListener(this);
        binding.botonAtras.setOnClickListener(this);
        cargarTituloAlbum(idAlbum);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void cargarTituloAlbum(Integer idAlbum) {
        Thread tarea = new Thread(() -> {
            try {
                binding.tituloAlbum.setText("Álbum: "+cliente.leerAlbum(idAlbum).getTitulo());
            } catch (ExcepcionAlbumFamiliar e) {
                //throw new RuntimeException(e);
            }
        });
        tarea.start();
        try {
            tarea.join(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Task<Boolean> crearNuevaPublicacion(Integer idAlbum, Integer idUsuario) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        String titulo = binding.tituloPublicacion.getText().toString();
        String texto = binding.textoPublicacion.getText().toString();
        activity.getDriveServiceHelper().uploadImageFile(uriImagen, idGrupo, idAlbum)
                .addOnSuccessListener(datosArchivo -> {
                    Thread tarea = new Thread(() -> {
                        boolean creadoCorrectamente = false;
                        if (null != datosArchivo.getArchivoId()) {
                            try {
                                Publicacion publicacion = new Publicacion();
                                publicacion.setPublicacionEnAlbum(idAlbum);
                                publicacion.setTitulo(titulo);
                                publicacion.setTexto(texto);
                                Usuario usuario = new Usuario();
                                usuario.setCodUsuario(idUsuario);
                                publicacion.setUsuarioCreaPublicacion(usuario);
                                Archivo archivo = new Archivo(null, datosArchivo.getNombre(), datosArchivo.getArchivoId());
                                publicacion.setArchivo(archivo);
                                cliente.insertarPublicacion(publicacion);
                                creadoCorrectamente = true;
                            } catch (ExcepcionAlbumFamiliar e) {
                                throw new RuntimeException(e);
                            }
                        }
                        taskCompletionSource.setResult(creadoCorrectamente);
                    });
                    tarea.start();
                    try {
                        tarea.join(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    e.printStackTrace();
                    taskCompletionSource.setException(e);
                });;
        return taskCompletionSource.getTask();
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
        if (id == R.id.botonCrearPublicacion) {
            crearNuevaPublicacion(idAlbum, activity.getIdUsuario())
                    .addOnSuccessListener(creadoCorrectamente -> {
                        if (creadoCorrectamente) {
                            Toast.makeText(requireContext(), "La publicación se ha creado correctamente.", Toast.LENGTH_SHORT).show();
                            navController.popBackStack();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                    });
        }
        else if (id == R.id.cardView) {
            seleccionarImagen();
        }
        else if (id == R.id.botonAtras) {
            navController.popBackStack();
        }
    }


}
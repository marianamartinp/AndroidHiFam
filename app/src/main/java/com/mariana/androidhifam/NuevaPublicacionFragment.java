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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mariana.androidhifam.databinding.FragmentNuevaPublicacionBinding;
import com.mariana.androidhifam.databinding.FragmentPublicacionesBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
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
    private Integer idAlbum;
    private MainActivity activity;
    private NavController navController;
    private Uri uriImagen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nuevaPublicacionFragmentArgs = NuevaPublicacionFragmentArgs.fromBundle(getArguments());
            idAlbum = nuevaPublicacionFragmentArgs.getIdAlbum();
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

    public void crearNuevaPublicacion(Integer idAlbum, Integer idUsuario) {
        Thread tarea = new Thread(() -> {
            try {
                Publicacion publicacion = new Publicacion();
                publicacion.setPublicacionEnAlbum(idAlbum);
                publicacion.setTitulo(binding.tituloPublicacion.getText().toString());
                publicacion.setTexto(binding.textoPublicacion.getText().toString());
                Usuario usuario = new Usuario();
                usuario.setCodUsuario(idUsuario);
                publicacion.setUsuarioCreaPublicacion(usuario);
//                Archivo archivo = new Archivo(null, null, "ruta");
//                publicacion.setArchivo(archivo);
                cliente.insertarPublicacion(publicacion);
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
            crearNuevaPublicacion(idAlbum, activity.getIdUsuario());
            findNavController(v).navigate(NuevaPublicacionFragmentDirections.actionNuevaPublicacionFragmentToPublicacionesFragment(idAlbum));
        }
        else if (id == R.id.cardView) {
            seleccionarImagen();
        }
        else if (id == R.id.botonAtras) {
            navController.popBackStack();
        }
    }
}
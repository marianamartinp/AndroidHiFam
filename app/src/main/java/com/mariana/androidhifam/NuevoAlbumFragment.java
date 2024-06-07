package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentNuevoAlbumBinding;
import com.mariana.androidhifam.databinding.FragmentNuevoGrupoBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.Usuario;
import pojosalbumfamiliar.UsuarioIntegraGrupo;

public class NuevoAlbumFragment extends Fragment implements View.OnClickListener {
    private @NonNull NuevoAlbumFragmentArgs nuevoAlbumFragmentArgs;
    private @NonNull FragmentNuevoAlbumBinding binding;
    private CCAlbumFamiliar cliente;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private NavController navController;
    private Integer idGrupo, tokenUsuario;

    // Método onCreate para la inicialización del fragmento
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtener los argumentos pasados al fragmento
        if (getArguments() != null) {
            nuevoAlbumFragmentArgs = NuevoAlbumFragmentArgs.fromBundle(getArguments());
            idGrupo = nuevoAlbumFragmentArgs.getIdGrupo();
        }
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Método onCreateView para inflar el diseño de la vista del fragmento
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity.findViewById(R.id.refreshLayout).setEnabled(false);
        // Inflar el diseño del fragmento
        binding = FragmentNuevoAlbumBinding.inflate(inflater, container, false);
        // Obtener el controlador de navegación del fragmento
        navController = NavHostFragment.findNavController(this);
        cliente = activity.getCliente();
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    // Método onViewCreated para configurar la vista después de que se haya creado
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Cargar el título del grupo en la interfaz de usuario
        cargarTituloGrupo(idGrupo);
        binding.botonNuevoAlbum.setOnClickListener(this);
        binding.botonAtras.setOnClickListener(this);
    }

    // Método onDestroyView para limpiar la vista cuando el fragmento está destruido
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Liberar la referencia al diseño del fragmento para evitar fugas de memoria
        binding = null;
    }

    // Método onClick para manejar los clics en los elementos de la vista
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonNuevoAlbum) {
            crearAlbum(idGrupo);
        }
        else if (id == R.id.botonAtras) {
            findNavController(v).popBackStack();
        }
    }

    // Cargar el título del grupo
    public void cargarTituloGrupo(Integer idGrupo) {
        executorService.execute(() -> {
            try {
                String tituloGrupo = cliente.leerGrupo(idGrupo).getTitulo();
                String titulo = getString(R.string.tituloGrupoFormularioAlbum, tituloGrupo);
                mainHandler.post(() -> binding.tituloGrupoAlbum.setText(titulo));
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(requireContext(), "Error al cargar el título del grupo.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Crear un objeto de álbum con los datos proporcionados en la vista
    public void crearAlbum(Integer idGrupo) {
        String tituloAlbum = binding.tituloAlbum.getText().toString().trim();
        String descripcionAlbum = binding.descripcionAlbum.getText().toString().trim();
        if (!tituloAlbum.isEmpty()) {
            // Construcción del álbum
            Album album = new Album();
            album.setTitulo(tituloAlbum);
            if (!descripcionAlbum.isEmpty()) {
                album.setDescripcion(descripcionAlbum);
            }
            Usuario usuarioAdmin = new Usuario();
            usuarioAdmin.setCodUsuario(tokenUsuario);
            album.setUsuarioAdminAlbum(usuarioAdmin);
            Grupo grupoCreador = new Grupo();
            grupoCreador.setCodGrupo(idGrupo);
            album.setGrupoCreaAlbum(grupoCreador);
            if (binding.radioButtonColectivo.isChecked()){
                album.setTipo("C");
            }
            else {
                album.setTipo("I");
            }

            // Inserción del álbum
            executorService.execute(() -> {
                try {
                    cliente.insertarAlbum(album);
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "El álbum se ha creado correctamente.", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                    });
                } catch (ExcepcionAlbumFamiliar e) {
                    String mensaje;
                    mensaje = e.getMensajeUsuario();
                    mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
                }
            });
        }
        else {
            Toast.makeText(requireContext(), "El título del álbum es obligatorio.", Toast.LENGTH_SHORT).show();
        }
    }
}
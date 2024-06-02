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
    private NuevoAlbumFragmentArgs nuevoAlbumFragmentArgs;
    private @NonNull FragmentNuevoAlbumBinding binding;
    private CCAlbumFamiliar cliente;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private NavController navController;
    private Integer idGrupo, tokenUsuario;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nuevoAlbumFragmentArgs = NuevoAlbumFragmentArgs.fromBundle(getArguments());
            idGrupo = nuevoAlbumFragmentArgs.getIdGrupo();
        }
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNuevoAlbumBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
        cliente = activity.getCliente();
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarTituloGrupo(idGrupo);
        binding.botonNuevoAlbum.setOnClickListener(this);
        binding.botonAtras.setOnClickListener(this);
    }

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

    public void crearAlbum(Integer idGrupo) {
        String tituloAlbum = binding.tituloAlbum.getText().toString().trim();
        String descripcionAlbum = binding.descripcionAlbum.getText().toString().trim();
        if (!tituloAlbum.isEmpty()) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

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
}
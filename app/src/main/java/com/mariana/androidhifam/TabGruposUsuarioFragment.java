package com.mariana.androidhifam;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mariana.androidhifam.databinding.FragmentTabGruposUsuarioBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import utils.ItemsListAdapter;
import utils.ListAdapter;

public class TabGruposUsuarioFragment extends Fragment implements ListAdapter.OnItemClickListener, View.OnClickListener, ModalFragment.CustomModalInterface {

    private @NonNull FragmentTabGruposUsuarioBinding binding;
    private NavController navController;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private ArrayList<Grupo> grupos;
    private Integer tokenUsuario;
    private ListAdapter<Grupo> adapter;

    // Método de creación
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicialización de variables y objetos
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        cliente = new CCAlbumFamiliar();
    }

    // Método de creación de la vista
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicialización de vistas y obtención del token de usuario
        navController = NavHostFragment.findNavController(this);
        binding = FragmentTabGruposUsuarioBinding.inflate(inflater, container, false);
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    // Método que se llama cuando la vista se crea completamente
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Carga los grupos del usuario
        cargarGruposUsuario();
    }

    // Método para cargar los grupos del usuario
    public void cargarGruposUsuario() {
        executorService.execute(() -> {
            try {
                LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                filtros.put("uig.COD_USUARIO", "=" + tokenUsuario);
                filtros.put("g.FECHA_ELIMINACION", "is null");
                LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
                ordenacion.put("g.titulo", "asc");
                grupos = cliente.leerGrupos(filtros, ordenacion);
                mainHandler.post(() -> {
                    adapter = new ListAdapter<>(requireContext(), grupos, ItemsListAdapter.ITEM_GRUPO_USUARIO, this);
                    binding.misGrupos.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.misGrupos.setAdapter(adapter);
                    mostrarTextoAlternativo();
                });
            }
            catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    // Método para manejar clics en elementos de la lista
    @Override
    public void onItemClick(Object item, int position, int idButton) {
        if (activity.getHabilitarInteraccion()) {
            if (idButton == R.id.iconoEquis) {
                int idGrupo = ((Grupo) item).getCodGrupo();
                modalAbandonarGrupo(idGrupo, position);
            }
        }
    }

    // Método para manejar clics positivos en el diálogo modal
    @Override
    public void onPositiveClick(String idModal, Integer position, Integer id) {
        if (activity.getHabilitarInteraccion()) {
            abandonarGrupo(id, position);
        }
    }

    // Método para manejar clics en vistas
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonIngresarEnFamilia) {
            navController.navigate(TabGruposUsuarioFragmentDirections.actionTabGruposUsuarioFragmentToIngresoGrupoFragment());
        }
    }

    // Método para mostrar un diálogo modal para abandonar un grupo
    public void modalAbandonarGrupo(int id, int position) {
        ModalFragment modal = new ModalFragment("abandonarGrupo", position, id, this, "¿Desea abandor esta familia?", getString(R.string.btnAbandonar), getString(R.string.btnCancelar));
        modal.show(activity.getSupportFragmentManager(), "modalAbandonarGrupo");
    }

    // Método para mostrar un texto alternativo si la lista de grupos está vacía
    public void mostrarTextoAlternativo() {
        if (grupos.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        }
        else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }

    // Método para abandonar un grupo
    public void abandonarGrupo(int idGrupo, int position) {
        executorService.execute(() -> {
            try {
                cliente.eliminarUsuarioIntegraGrupo(tokenUsuario, idGrupo);
                mainHandler.post(() -> {
                    actualizarGrid(position);
                });
            } catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    // Método para actualizar la lista después de abandonar un grupo
    public void actualizarGrid(int position) {
        grupos.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, grupos.size() - position);
        mostrarTextoAlternativo();
    }

    // Método para manejar excepciones de la clase ExcepcionAlbumFamiliar
    public void manejadorExcepcionAlbumFamiliar(ExcepcionAlbumFamiliar e) {
        String mensaje;
        mensaje = e.getMensajeUsuario();
        mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
    }

}
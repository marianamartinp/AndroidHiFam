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
import com.mariana.androidhifam.databinding.FragmentTabMiembrosGrupoBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.Usuario;
import pojosalbumfamiliar.UsuarioIntegraGrupo;

public class TabGruposUsuarioFragment extends Fragment implements ListAdapter.OnItemClickListener, View.OnClickListener {

    private FragmentTabGruposUsuarioBinding binding;
    private NavController navController;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private ArrayList<Grupo> grupos;
    private Integer tokenUsuario;
    private ListAdapter<Grupo> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        cliente = new CCAlbumFamiliar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        navController = NavHostFragment.findNavController(this);
        binding = FragmentTabGruposUsuarioBinding.inflate(inflater, container, false);
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarGruposUsuario();
    }

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

    @Override
    public void onItemClick(Object item, int position, int idButton) {
        if (activity.getHabilitarInteraccion()) {
            if (idButton == R.id.iconoEquis) {
                abandonarGrupo((Grupo) item, position);
            }
        }
    }

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

    public void abandonarGrupo(Grupo item, int position) {
        executorService.execute(() -> {
            try {
                cliente.eliminarUsuarioIntegraGrupo(tokenUsuario, item.getCodGrupo());
                mainHandler.post(() -> {
                    actualizarGrid(position);
                });
            } catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    public void actualizarGrid(int position) {
        grupos.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, grupos.size() - position);
        mostrarTextoAlternativo();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonIngresarEnFamilia) {
            navController.navigate(TabGruposUsuarioFragmentDirections.actionTabGruposUsuarioFragmentToIngresoGrupoFragment());
        }
    }

    public void manejadorExcepcionAlbumFamiliar(ExcepcionAlbumFamiliar e) {
        String mensaje;
        mensaje = e.getMensajeUsuario();
        mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
    }

}
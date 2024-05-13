package com.mariana.androidhifam;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mariana.androidhifam.databinding.FragmentNuevoGrupoBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicReference;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;

public class NuevoGrupoFragment extends Fragment implements View.OnClickListener {

    private @NonNull FragmentNuevoGrupoBinding binding;
    private String tituloAlbum;
    private ArrayList<String> usuarios;
    private Integer idPublicacion, imagenPublicacion;
    private CCAlbumFamiliar cliente;
    private ListAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cliente = new CCAlbumFamiliar();
        usuarios = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNuevoGrupoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.botonCrearFamilia.setOnClickListener(this);
        binding.botonAnyadirUsuario.setOnClickListener(this);

        adapter = new ListAdapter<>(requireContext(), usuarios, ItemsListAdapter.ITEM_MIEMBRO_GRUPO);
        binding.miembrosGrupo.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.miembrosGrupo.setAdapter(adapter);
    }

    public void anyadirUsuario() {
        AtomicReference<Integer> resultado = new AtomicReference<>();
        String usuario = binding.usuarioFamilia.getText().toString().trim();
        Thread tarea = new Thread(() -> {
            try {
                LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                filtros.put("u.USUARIO", "=" + "'" + usuario + "'");
                resultado.set(cliente.leerUsuarios(filtros, null).size());

            } catch (ExcepcionAlbumFamiliar e) {
                // Error
            }
        });
        tarea.start();
        try {
            tarea.join(5000);
        } catch (InterruptedException e) {
            // Error
        }
        if (resultado.get() > 0) {
            usuarios.add(usuario);
            adapter.notifyItemInserted(usuarios.size());
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
        if (id == R.id.botonAnyadirUsuario) {
            anyadirUsuario();
        }
    }
}
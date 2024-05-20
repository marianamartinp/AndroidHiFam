package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentNuevoGrupoBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.Usuario;
import pojosalbumfamiliar.UsuarioIntegraGrupo;

public class NuevoGrupoFragment extends Fragment implements View.OnClickListener, ListAdapter.OnItemClickListener {

    private @NonNull FragmentNuevoGrupoBinding binding;
    private String tituloAlbum;
    private ArrayList<Usuario> usuarios;
    private Integer idPublicacion, imagenPublicacion;
    private CCAlbumFamiliar cliente;
    private ListAdapter<Usuario> adapter;
    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        usuarios = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNuevoGrupoBinding.inflate(inflater, container, false);
        cliente = activity.getCliente();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.botonNuevaFamilia.setOnClickListener(this);
        binding.botonAnyadirUsuario.setOnClickListener(this);
        binding.botonAtras.setOnClickListener(this);

        adapter = new ListAdapter<>(requireContext(), usuarios, ItemsListAdapter.ITEM_MIEMBRO_GRUPO, this);
        binding.miembrosGrupo.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.miembrosGrupo.setAdapter(adapter);

    }

    public boolean crearGrupo() {
        AtomicBoolean creadoCorrectamente = new AtomicBoolean(false);
        AtomicInteger usuariosInsertados = new AtomicInteger();
        AtomicInteger gruposDuplicados = new AtomicInteger();
        String tituloFamilia = binding.tituloFamilia.getText().toString().trim();
        if (!tituloFamilia.isEmpty()) {
            Grupo grupo = new Grupo();
            grupo.setTitulo(tituloFamilia);
            if (!binding.descripcionFamilia.getText().toString().trim().isEmpty()) {
                grupo.setDescripcion(binding.descripcionFamilia.getText().toString().trim());
            }
            Usuario usuarioAdmin = new Usuario();
            usuarioAdmin.setCodUsuario(activity.getIdUsuario());
            grupo.setUsuarioAdminGrupo(usuarioAdmin);

            Thread tarea = new Thread(() -> {
                LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                filtros.put("g.TITULO", "= '" + tituloFamilia + "'");
                filtros.put("g.COD_USUARIO_ADMIN_GRUPO", "=" + activity.getIdUsuario());
                try {
                    gruposDuplicados.set(cliente.leerGrupos(filtros, null).size());
                    if (gruposDuplicados.get() == 0) {
                        int gruposInsertados = cliente.insertarGrupo(grupo);
                        if (gruposInsertados > 0) {
                            Grupo grupoCreado = cliente.leerGrupos(filtros, null).get(0);
                            for (Usuario usuario : usuarios) {
                                usuariosInsertados.set(usuariosInsertados.get() + cliente.insertarUsuarioIntegraGrupo(new UsuarioIntegraGrupo(usuario, grupoCreado)));
                            }
                            creadoCorrectamente.set(true);
                        }
                    }
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
            if (creadoCorrectamente.get() && usuariosInsertados.get() < usuarios.size()) {
                Toast.makeText(requireContext(), "Algunos usuarios pueden no haberse añadido correctamente.", Toast.LENGTH_SHORT).show();
            }
            else if (gruposDuplicados.get() > 0) {
                Toast.makeText(requireContext(), "Ya existe una familia con el título elegido.", Toast.LENGTH_SHORT).show();
            }
            else if (!creadoCorrectamente.get()) {
                Toast.makeText(requireContext(), "No se ha podido crear la familia.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(requireContext(), "El título de la familia es obligatorio.", Toast.LENGTH_SHORT).show();
        }
        return creadoCorrectamente.get();
    }

    public void anyadirUsuario() {
        if (!binding.usuarioFamilia.getText().toString().trim().isEmpty()) {
            AtomicReference<ArrayList<Usuario>> resultado = new AtomicReference<>(new ArrayList<>());
            String usuario = binding.usuarioFamilia.getText().toString().trim();
            Thread tarea = new Thread(() -> {
                try {
                    LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                    filtros.put("u.USUARIO", "=" + "'" + usuario + "'");
                    filtros.put("u.FECHA_ELIMINACION", "is null");
                    resultado.set(cliente.leerUsuarios(filtros, null));

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

            if (!resultado.get().isEmpty() && !usuarioYaInsertado(resultado.get().get(0).getCodUsuario()) && !Objects.equals(activity.getIdUsuario(), resultado.get().get(0).getCodUsuario())) {
                usuarios.add(resultado.get().get(0));
                adapter.notifyItemInserted(usuarios.size());
                binding.usuarioFamilia.setText("");
            } else if (!resultado.get().isEmpty() && Objects.equals(activity.getIdUsuario(), resultado.get().get(0).getCodUsuario())) {
                Toast.makeText(requireContext(), "Tu usuario será añadido por defecto.", Toast.LENGTH_SHORT).show();
            }
            else if (!resultado.get().isEmpty() && usuarioYaInsertado(resultado.get().get(0).getCodUsuario())) {
                Toast.makeText(requireContext(), "El usuario ya pertenece a la familia.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(requireContext(), "El usuario no existe.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(requireContext(), "Escribe un nombre de usuario.", Toast.LENGTH_SHORT).show();
            binding.usuarioFamilia.setText("");
        }
    }

    public boolean usuarioYaInsertado(int id) {
        for (Usuario usuario : usuarios) {
            if (id == usuario.getCodUsuario()) {
                return true;
            }
        }
        return false;
    }

    public void mostrarTextoAlternativo() {
        if (usuarios.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        }
        else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
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
            mostrarTextoAlternativo();
        }
        else if (id == R.id.botonNuevaFamilia) {
            if (crearGrupo()) {
                Toast.makeText(requireContext(), "La familia se ha creado correctamente.", Toast.LENGTH_SHORT).show();
                findNavController(v).popBackStack();
            }
        }
        else if (id == R.id.botonAtras) {
            findNavController(v).popBackStack();
        }
    }


    @Override
    public void onItemClick(Object item, int position) {
        usuarios.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, usuarios.size() - position);
        mostrarTextoAlternativo();
    }
}
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
import utils.ItemsListAdapter;
import utils.ListAdapter;

public class TabMiembrosGrupoFragment extends Fragment implements ListAdapter.OnItemClickListener, View.OnClickListener {

    private @NonNull FragmentTabMiembrosGrupoBinding binding;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private DetallesGrupoFragment parentFragment;
    private Grupo grupo;
    private Integer tokenUsuario, idGrupo;
    private ArrayList<Usuario> usuarios;
    private ListAdapter<Usuario> adapter;

    // Método de creación
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicialización de variables y objetos
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        cliente = new CCAlbumFamiliar();
        parentFragment = (DetallesGrupoFragment) this.getParentFragment();
    }

    // Método de creación de la vista
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicialización de vistas y obtención de datos del grupo
        binding = FragmentTabMiembrosGrupoBinding.inflate(inflater, container, false);
        tokenUsuario = Integer.parseInt(activity.getToken());
        idGrupo = parentFragment.getGrupoId();
        return binding.getRoot();
    }

    // Método que se llama cuando la vista se crea completamente
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Carga el grupo y sus miembros
        cargarGrupo();
        cargarMiembrosGrupo();
    }

    // Método para manejar clics en vistas
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonAnyadirUsuario) {
            anyadirUsuario(grupo);
        }
    }

    // Método para manejar clics en elementos de la lista
    @Override
    public void onItemClick(Object item, int position, int idButton) {
        if (activity.getHabilitarInteraccion()) {
            if (idButton == R.id.iconoEquis) {
                eliminarUsuario((Usuario) item, position);
            }
        }
    }

    // Método para cargar el grupo
    public void cargarGrupo() {
        executorService.execute(() -> {
            try {
                grupo = cliente.leerGrupo(idGrupo);
                mainHandler.post(this::revisarPermisos);
            } catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    // Método para cargar los miembros del grupo
    public void cargarMiembrosGrupo() {
        executorService.execute(() -> {
            try {
                LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                filtros.put("uig.COD_GRUPO", "=" + idGrupo);
                LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
                ordenacion.put("u.USUARIO", "asc");
                usuarios = cliente.leerUsuarios(filtros, ordenacion);
                mainHandler.post(() -> {
                    adapter = new ListAdapter<>(requireContext(), usuarios, ItemsListAdapter.ITEM_MIEMBRO_GRUPO, this, grupo.getUsuarioAdminGrupo().getCodUsuario(), tokenUsuario);
                    binding.miembrosGrupo.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.miembrosGrupo.setAdapter(adapter);
                    mostrarTextoAlternativo();
                });
            }
            catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    // Método para mostrar un texto alternativo si la lista de miembros está vacía
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

    // Método para eliminar un usuario del grupo
    public void eliminarUsuario(Usuario item, int position) {
        executorService.execute(() -> {
            try {
                cliente.eliminarUsuarioIntegraGrupo(item.getCodUsuario(), idGrupo);
                mainHandler.post(() -> {
                    actualizarGrid(position);
                });
            } catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    // Método para actualizar la lista después de eliminar un usuario
    public void actualizarGrid(int position) {
        usuarios.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, usuarios.size() - position);
        mostrarTextoAlternativo();
    }

    // Método para manejar excepciones de la clase ExcepcionAlbumFamiliar
    public void manejadorExcepcionAlbumFamiliar(ExcepcionAlbumFamiliar e) {
        String mensaje;
        mensaje = e.getMensajeUsuario();
        mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
    }

    // Método para añadir un usuario al grupo
    public void anyadirUsuario(Grupo grupo) {
        if (!binding.usuarioFamilia.getText().toString().trim().isEmpty()) {
            String usuario = binding.usuarioFamilia.getText().toString().trim();
            executorService.execute(() -> {
                try {
                    LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                    filtros.put("u.USUARIO", "=" + "'" + usuario + "'");
                    filtros.put("u.FECHA_ELIMINACION", "is null");
                    ArrayList<Usuario> usuariosCoincidentes = cliente.leerUsuarios(filtros, null);
                    int resultado;
                    if (!usuariosCoincidentes.isEmpty() && !usuarioYaInsertado(usuariosCoincidentes.get(0).getCodUsuario()) && !Objects.equals(tokenUsuario, usuariosCoincidentes.get(0).getCodUsuario())) {
                        resultado = cliente.insertarUsuarioIntegraGrupo(new UsuarioIntegraGrupo(usuariosCoincidentes.get(0), grupo));
                    } else {
                        resultado = 0;
                    }
                    mainHandler.post(() -> {
                        if (resultado > 0) {
                            usuarios.add(usuariosCoincidentes.get(0));
                            adapter.notifyItemInserted(usuarios.size());
                            binding.usuarioFamilia.setText("");
                            mostrarTextoAlternativo();
                        } else if (!usuariosCoincidentes.isEmpty() && Objects.equals(tokenUsuario, usuariosCoincidentes.get(0).getCodUsuario())) {
                            Toast.makeText(requireContext(), "Tu usuario ya ha pertenece al grupo.", Toast.LENGTH_SHORT).show();
                        }
                        else if (!usuariosCoincidentes.isEmpty() && usuarioYaInsertado(usuariosCoincidentes.get(0).getCodUsuario())) {
                            Toast.makeText(requireContext(), "El usuario ya pertenece a la familia.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(requireContext(), "El usuario no existe.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (ExcepcionAlbumFamiliar e) {
                    mainHandler.post(() -> Toast.makeText(requireContext(), e.getMensajeUsuario(), Toast.LENGTH_SHORT).show());
                }
            });
        }
        else {
            Toast.makeText(requireContext(), "Escribe un nombre de usuario.", Toast.LENGTH_SHORT).show();
            binding.usuarioFamilia.setText("");
        }
    }

    // Método para verificar si un usuario ya está insertado en la lista de miembros
    public boolean usuarioYaInsertado(int id) {
        // Verificar si un usuario ya está insertado en la lista de miembros
        for (Usuario usuario : usuarios) {
            if (id == usuario.getCodUsuario()) {
                return true;
            }
        }
        return false;
    }

    // Método para verificar los permisos del usuario
    public void revisarPermisos() {
        // Verificar los permisos del usuario y mostrar u ocultar vistas según corresponda
        if (Objects.equals(tokenUsuario, grupo.getUsuarioAdminGrupo().getCodUsuario())) {
            binding.botonAnyadirUsuario.setOnClickListener(this);
            binding.botonAnyadirUsuario.setVisibility(View.VISIBLE);
            binding.usuarioFamilia.setVisibility(View.VISIBLE);
        }
        else {
            binding.botonAnyadirUsuario.setVisibility(View.INVISIBLE);
            binding.usuarioFamilia.setVisibility(View.INVISIBLE);
        }
    }
}
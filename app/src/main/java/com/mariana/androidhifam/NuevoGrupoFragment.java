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

import com.mariana.androidhifam.databinding.FragmentNuevoGrupoBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.Usuario;
import pojosalbumfamiliar.UsuarioIntegraGrupo;

public class NuevoGrupoFragment extends Fragment implements View.OnClickListener, ListAdapter.OnItemClickListener {

    private @NonNull FragmentNuevoGrupoBinding binding;
    private ArrayList<Usuario> usuarios;
    private CCAlbumFamiliar cliente;
    private ListAdapter<Usuario> adapter;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        usuarios = new ArrayList<>();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNuevoGrupoBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
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

    public void crearGrupo() {
        String tituloFamilia = binding.tituloFamilia.getText().toString().trim();
        String descripcionFamilia = binding.descripcionFamilia.getText().toString().trim();
        if (!tituloFamilia.isEmpty()) {
            Grupo grupo = new Grupo();
            grupo.setTitulo(tituloFamilia);
            if (!descripcionFamilia.isEmpty()) {
                grupo.setDescripcion(descripcionFamilia);
            }
            Usuario usuarioAdmin = new Usuario();
            usuarioAdmin.setCodUsuario(activity.getIdUsuario());
            grupo.setUsuarioAdminGrupo(usuarioAdmin);

            executorService.execute(() -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                filtros.put("g.TITULO", "= '" + tituloFamilia + "'");
                filtros.put("g.COD_USUARIO_ADMIN_GRUPO", "=" + activity.getIdUsuario());
                filtros.put("g.FECHA_CREACION", "= '" + sdf.format(new Date()) + "'");
                if (!descripcionFamilia.isEmpty()) {
                    filtros.put("g.DESCRIPCION", "='" + descripcionFamilia + "'");
                }
                try {
                    int gruposInsertados = cliente.insertarGrupo(grupo);
                    if (gruposInsertados > 0) {
                        ArrayList<Grupo> gruposCreados = cliente.leerGrupos(filtros, null);
                        if (null != gruposCreados && !gruposCreados.isEmpty()) {
                            for (Usuario usuario : usuarios) {
                                cliente.insertarUsuarioIntegraGrupo(new UsuarioIntegraGrupo(usuario, gruposCreados.get(0)));
                            }
                        }
                    }
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "La familia se ha creado correctamente.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "El título de la familia es obligatorio.", Toast.LENGTH_SHORT).show();
        }
    }

    public void anyadirUsuario() {
        if (!binding.usuarioFamilia.getText().toString().trim().isEmpty()) {
            AtomicReference<ArrayList<Usuario>> resultado = new AtomicReference<>(new ArrayList<>());
            String usuario = binding.usuarioFamilia.getText().toString().trim();
            executorService.execute(() -> {
                try {
                    LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                    filtros.put("u.USUARIO", "=" + "'" + usuario + "'");
                    filtros.put("u.FECHA_ELIMINACION", "is null");
                    resultado.set(cliente.leerUsuarios(filtros, null));
                    mainHandler.post(() -> {
                        if (!resultado.get().isEmpty() && !usuarioYaInsertado(resultado.get().get(0).getCodUsuario()) && !Objects.equals(activity.getIdUsuario(), resultado.get().get(0).getCodUsuario())) {
                            usuarios.add(resultado.get().get(0));
                            adapter.notifyItemInserted(usuarios.size());
                            binding.usuarioFamilia.setText("");
                            mostrarTextoAlternativo();
                        } else if (!resultado.get().isEmpty() && Objects.equals(activity.getIdUsuario(), resultado.get().get(0).getCodUsuario())) {
                            Toast.makeText(requireContext(), "Tu usuario será añadido por defecto.", Toast.LENGTH_SHORT).show();
                        }
                        else if (!resultado.get().isEmpty() && usuarioYaInsertado(resultado.get().get(0).getCodUsuario())) {
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
        }
        else if (id == R.id.botonNuevaFamilia) {
            crearGrupo();
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
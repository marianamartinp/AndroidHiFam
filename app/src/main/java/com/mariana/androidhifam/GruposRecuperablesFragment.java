package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mariana.androidhifam.databinding.FragmentGruposRecuperablesBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;

public class GruposRecuperablesFragment extends Fragment implements View.OnCreateContextMenuListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout, ModalFragment.CustomModalInterface {

    private GruposRecuperablesFragmentArgs gruposRecuperablesFragmentArgs;
    private @NonNull FragmentGruposRecuperablesBinding binding;
    private ArrayList<Grupo> grupos;
    private ArrayList<Integer> imagenesGrupos;
    private GridAdapter<Grupo> adapter;
    private CCAlbumFamiliar cliente;
    private Integer idUsuario;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gruposRecuperablesFragmentArgs = GruposRecuperablesFragmentArgs.fromBundle(getArguments());
            idUsuario = gruposRecuperablesFragmentArgs.getIdUsuario();
        }
        cliente = new CCAlbumFamiliar();
        grupos = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGruposRecuperablesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        activity.setRefreshLayout(this);
        registerForContextMenu(binding.gridView);
        binding.gridView.setOnItemClickListener(this);
        cargarVistaGrupos(idUsuario);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        int itemId = (int) binding.gridView.getAdapter().getItemId(position);
        int idMenuItem = item.getItemId();

        if (idMenuItem == R.id.verDetallesGrupo) {
            return true;
        }
        else if (idMenuItem == R.id.verIntegrantesGrupo) {
            return true;
        }
        else if (idMenuItem == R.id.recuperarGrupo) {
            modalRecuperarGrupo(position, itemId);
            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_grupos_recuperables, menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        modalRecuperarGrupo(position, (int) id);
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh() {
        cargarVistaGrupos(idUsuario);
        Toast.makeText(getContext(), "Se han actualizado las familias eliminadas.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPositiveClick(int position, int id) {
        if (recuperarGrupo(id) > 0) {
            Toast.makeText(getContext(), "Se ha restaurado el grupo.", Toast.LENGTH_SHORT).show();
            grupos.remove(position);
            adapter.notifyDataSetChanged();
            mostrarTextoAlternativo();
        } else {
            Toast.makeText(getContext(), "Se ha producido un error.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNegativeClick(int position, int id) {
    }

    public void cargarGrupos(Integer idUsuario) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("g.COD_USUARIO_ADMIN_GRUPO", "=" + idUsuario);
        filtros.put("g.FECHA_ELIMINACION", "is not null");
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("g.titulo", "asc");
        grupos = cliente.leerGrupos(filtros, ordenacion);
    }

    public void cargarGrid() {
        if (!grupos.isEmpty()) {
            imagenesGrupos = new ArrayList<>();
            imagenesGrupos.add(R.drawable.imagen2);
            imagenesGrupos.add(R.drawable.imagen3);
            imagenesGrupos.add(R.drawable.imagen1);
            imagenesGrupos.add(R.drawable.imagen4);
            adapter = new GridAdapter<>(requireContext(), grupos, imagenesGrupos, false);
            binding.gridView.setAdapter(adapter);
        }
        mostrarTextoAlternativo();
    }

    public void cargarVistaGrupos(Integer idUsuario) {
        Thread tarea = new Thread(() -> {
            try {
                cargarGrupos(idUsuario);
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
        cargarGrid();
    }

    public void modalRecuperarGrupo(int position, int id) {
        ModalFragment modal = new ModalFragment(position, (int) id, this, "¿Desea recuperar este grupo?", getString(R.string.btnRecuperar), getString(R.string.btnCancelar));
        modal.show(getActivity().getSupportFragmentManager(), "modalRecuperarGrupo");
    }

    public int recuperarGrupo(int idGrupo) {
        AtomicInteger resultado = new AtomicInteger();
        Thread tarea = new Thread(() -> {
            try {
                resultado.set(cliente.restaurarGrupo(idGrupo));
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
        return resultado.get();
    }

    public void mostrarTextoAlternativo() {
        if (grupos.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        } else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }
}
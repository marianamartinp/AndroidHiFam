package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentAlbumesBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Album;

public class AlbumesFragment extends Fragment implements View.OnClickListener, View.OnCreateContextMenuListener, MenuProvider, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout {
    private AlbumesFragmentArgs albumesFragmentArgs;
    private @NonNull FragmentAlbumesBinding binding;
    private ArrayList<Album> albumes;
    private ArrayList<Integer> imagenesAlbumes;
    private CCAlbumFamiliar cliente;
    private Integer idGrupo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            albumesFragmentArgs = AlbumesFragmentArgs.fromBundle(getArguments());
            idGrupo = albumesFragmentArgs.getIdGrupo();
        }
        cliente = new CCAlbumFamiliar();
        albumes = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlbumesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        activity.setRefreshLayout(this);
        //getActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        binding.botonNuevoAlbum.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
        binding.botonUsuarios.setOnClickListener(this);
        cargarVistaAlbumes(idGrupo);

        registerForContextMenu(binding.gridView);

        binding.gridView.setOnItemClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        getActivity().removeMenuProvider(this);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_grupos_admin, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.eliminarGrupo) {
            return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_grupos_admin, menu);

        GridView gv = (GridView) v;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = info.position;

    }

    public void cargarTituloGrupo(Integer idGrupo) throws ExcepcionAlbumFamiliar {
        binding.tituloGrupo.setText(cliente.leerGrupo(idGrupo).getTitulo());
    }

    public void cargarAlbumes(Integer idGrupo) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("a.COD_GRUPO_CREA_ALBUM", "="+idGrupo);
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("a.titulo", "asc");
        albumes = cliente.leerAlbumes(filtros,ordenacion);
    }

    public void cargarGrid() {
        if (!albumes.isEmpty()) {
            imagenesAlbumes = new ArrayList<>();
            imagenesAlbumes.add(R.drawable.imagen2);
            imagenesAlbumes.add(R.drawable.imagen3);
            imagenesAlbumes.add(R.drawable.imagen1);
            imagenesAlbumes.add(R.drawable.imagen4);

            GridAdapter<Album> adapter = new GridAdapter<>(requireContext(), albumes, imagenesAlbumes, false);
            binding.gridView.setAdapter(adapter);
        }
        else {
            binding.textoAlternativo.setText("No hay nada por aquí.");
        }
    }

    public void cargarVistaAlbumes(Integer idGrupo) {
        Thread tarea = new Thread(() -> {
            try {
                cargarTituloGrupo(idGrupo);
                cargarAlbumes(idGrupo);
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
        cargarGrid();
    }

    public void menuPopUp() {
        PopupMenu popup = new PopupMenu(requireActivity(), binding.botonOpciones);
        popup.getMenuInflater()
                .inflate(R.menu.menu_grupos_admin, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(requireActivity(), "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        popup.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonNuevoAlbum) {
            Toast.makeText(requireContext(), "Añadir album", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.botonOpciones) {
            menuPopUp();
        }
        else if (id == R.id.botonUsuarios) {
            Toast.makeText(requireContext(), "Usuarios del album", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        findNavController(view).navigate(AlbumesFragmentDirections.actionAlbumesFragmentToPublicacionesFragment((int)id));
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh() {
        cargarVistaAlbumes(idGrupo);
        Toast.makeText(getContext(), "Se han actualizado los álbumes.", Toast.LENGTH_SHORT).show();
    }
}
package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentAlbumesBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Album;

public class AlbumesFragment extends Fragment implements View.OnClickListener, View.OnCreateContextMenuListener, MenuProvider, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout {
    private AlbumesFragmentArgs albumesFragmentArgs;
    private @NonNull FragmentAlbumesBinding binding;
    private ArrayList<Album> albumes;
    private ArrayList<File> imagenesAlbumes;
    GridAdapter<Album> adapter;
    private CCAlbumFamiliar cliente;
    private Integer idGrupo;
    private String portadaAlbum;
    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            albumesFragmentArgs = AlbumesFragmentArgs.fromBundle(getArguments());
            idGrupo = albumesFragmentArgs.getIdGrupo();
        }
        albumes = new ArrayList<>();
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlbumesBinding.inflate(inflater, container, false);
        cliente = activity.getCliente();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setRefreshLayout(this);
        //getActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        binding.botonNuevoAlbum.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
        binding.botonUsuarios.setOnClickListener(this);
        SwipeRefreshLayout refreshLayout = activity.findViewById(R.id.refreshLayout);
        binding.gridView.setOnScrollListener(new GridView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Disable refreshing when scrolling
                if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    refreshLayout.setEnabled(false);
                } else {
                    refreshLayout.setEnabled(true);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Empty method body, not needed for this purpose
            }
        });
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
    }

    public void cargarTituloGrupo(Integer idGrupo) throws ExcepcionAlbumFamiliar {
        binding.tituloGrupo.setText(cliente.leerGrupo(idGrupo).getTitulo());
    }


    public void cargarAlbumes(Integer idGrupo) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("a.COD_GRUPO_CREA_ALBUM", "="+idGrupo);
        filtros.put("a.FECHA_ELIMINACION", "is null");
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("a.titulo", "asc");
        albumes = cliente.leerAlbumes(filtros,ordenacion);
    }

    public void cargarGrid() {
        imagenesAlbumes = activity.getImagenes();
        adapter = new GridAdapter<>(requireContext(), albumes, imagenesAlbumes, false);
        binding.gridView.setAdapter(adapter);
    }

    public void cargarVistaAlbumes(Integer idGrupo) {
        Thread tarea = new Thread(() -> {
            try {
                cargarTituloGrupo(idGrupo);
                activity.cargarImagenesDrive();
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
        mostrarTextoAlternativo();
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
        findNavController(view).navigate(AlbumesFragmentDirections.actionAlbumesFragmentToPublicacionesFragment((int)id, idGrupo));
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh() {
        cargarVistaAlbumes(idGrupo);
        Toast.makeText(getContext(), "Se han actualizado los álbumes.", Toast.LENGTH_SHORT).show();
    }

    public void mostrarTextoAlternativo() {
        if (albumes.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        }
        else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }
}
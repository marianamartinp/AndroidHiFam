package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentPublicacionesBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;

public class PublicacionesFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout {
    private PublicacionesFragmentArgs publicacionesFragmentArgs;
    private @NonNull FragmentPublicacionesBinding binding;
    private ArrayList<File> imagenesPublicaciones;
    private ArrayList<Publicacion> publicaciones;
    GridAdapter<Publicacion> adapter;
    private CCAlbumFamiliar cliente;
    private Integer idAlbum, idGrupo;
    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicacionesFragmentArgs = PublicacionesFragmentArgs.fromBundle(getArguments());
            idAlbum = publicacionesFragmentArgs.getIdAlbum();
            idGrupo = publicacionesFragmentArgs.getIdGrupo();
        }
        cliente = new CCAlbumFamiliar();
        publicaciones = new ArrayList<>();
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPublicacionesBinding.inflate(inflater, container, false);
        cliente = activity.getCliente();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setRefreshLayout(this);
        binding.botonVistaIndividual.setOnClickListener(this);
        binding.botonNuevaPublicacion.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
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
        binding.gridView.setOnItemClickListener(this);
        cargarVistaPublicaciones(idAlbum);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void cargarTituloAlbum(Integer idAlbum) throws ExcepcionAlbumFamiliar {
        binding.tituloAlbum.setText(cliente.leerAlbum(idAlbum).getTitulo());
    }

    public void cargarPublicaciones(Integer idAlbum) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("pea.COD_ALBUM", "="+idAlbum);
        filtros.put("p.FECHA_ELIMINACION", "is null");
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("p.COD_PUBLICACION", "asc");
        publicaciones = cliente.leerPublicaciones(filtros,ordenacion);
    }

    public void cargarGrid() {
        imagenesPublicaciones = activity.getImagenes();
        adapter = new GridAdapter<Publicacion>(requireContext(), publicaciones, imagenesPublicaciones, false);
        binding.gridView.setAdapter(adapter);
    }

    public void cargarVistaPublicaciones(Integer idAlbum) {
        Thread tarea = new Thread(() -> {
            try {
                cargarTituloAlbum(idAlbum);
                activity.cargarImagenesDrive();
                cargarPublicaciones(idAlbum);
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
        if (id == R.id.botonVistaIndividual) {
            findNavController(v).navigate(PublicacionesFragmentDirections.actionPublicacionesFragmentToPublicacionesListaFragment(idAlbum, idGrupo));
        } else if (id == R.id.botonNuevaPublicacion) {
            findNavController(v).navigate(PublicacionesFragmentDirections.actionPublicacionesFragmentToNuevaPublicacionFragment(idAlbum, idGrupo));
        }
        else if (id == R.id.botonOpciones) {
            menuPopUp();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        findNavController(view).navigate(PublicacionesFragmentDirections.actionPublicacionesFragmentToPublicacionFragment((int)id));
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh() {
        cargarVistaPublicaciones(idAlbum);
        Toast.makeText(getContext(), "Se han actualizado las publicaciones.", Toast.LENGTH_SHORT).show();
    }

    public void mostrarTextoAlternativo() {
        if (publicaciones.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        }
        else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }
}
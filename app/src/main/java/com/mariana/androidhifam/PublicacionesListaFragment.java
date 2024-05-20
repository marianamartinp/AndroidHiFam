package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
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

import com.mariana.androidhifam.databinding.FragmentPublicacionesListaBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;

public class PublicacionesListaFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout {
    private PublicacionesListaFragmentArgs publicacionesListaFragmentArgs;
    private @NonNull FragmentPublicacionesListaBinding binding;
    private ArrayList<File> imagenesPublicaciones;
    private ArrayList<Publicacion> publicaciones;
    private NavController navController;
    private GridAdapter<Publicacion> adapter;
    private CCAlbumFamiliar cliente;
    private  String portadaAlbum;
    private Integer idAlbum, idGrupo;
    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicacionesListaFragmentArgs = PublicacionesListaFragmentArgs.fromBundle(getArguments());
            idAlbum = publicacionesListaFragmentArgs.getIdAlbum();
            idGrupo = publicacionesListaFragmentArgs.getIdGrupo();
        }
        publicaciones = new ArrayList<>();
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPublicacionesListaBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
        cliente = activity.getCliente();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setRefreshLayout(this);
        binding.botonVistaGrid.setOnClickListener(this);
        binding.botonNuevaPublicacion.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
        binding.gridView.setOnItemClickListener(this);
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


    public void cargarGridPublicaciones(Integer idAlbum) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("pea.COD_ALBUM", "="+idAlbum);
        filtros.put("p.FECHA_ELIMINACION", "is null");
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("p.titulo", "asc");
        publicaciones = cliente.leerPublicaciones(filtros,ordenacion);
    }

    public void cargarLista() {
        imagenesPublicaciones = activity.getImagenes();
        adapter = new GridAdapter<Publicacion>(requireContext(), publicaciones, imagenesPublicaciones, true);
        binding.gridView.setAdapter(adapter);
    }

    public void cargarVistaPublicaciones(Integer idAlbum) {
        Thread tarea = new Thread(() -> {
            try {
                cargarTituloAlbum(idAlbum);
                activity.cargarImagenesDrive();
                cargarGridPublicaciones(idAlbum);
            } catch (ExcepcionAlbumFamiliar e) {
                //throw new RuntimeException(e);
            }
        });
        tarea.start();
        try {
            tarea.join(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        cargarLista();
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
        if (id == R.id.botonVistaGrid) {
            findNavController(v).navigate(PublicacionesListaFragmentDirections.actionPublicacionesListaFragmentToPublicacionesFragment(idAlbum, idGrupo));
        } else if (id == R.id.botonNuevaPublicacion) {
            findNavController(v).navigate(PublicacionesListaFragmentDirections.actionPublicacionesListaFragmentToNuevaPublicacionFragment(idAlbum, idGrupo));
        }
        else if (id == R.id.botonOpciones) {
            menuPopUp();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        findNavController(view).navigate(PublicacionesListaFragmentDirections.actionPublicacionesListaFragmentToPublicacionFragment((int)id));
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
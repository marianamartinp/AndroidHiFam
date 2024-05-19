package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentPublicacionesListaBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;

public class PublicacionesListaFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout {
    private PublicacionesListaFragmentArgs publicacionesListaFragmentArgs;
    private @NonNull FragmentPublicacionesListaBinding binding;
    private ArrayList<Integer> imagenesPublicaciones;
    private ArrayList<Publicacion> publicaciones;
    private CCAlbumFamiliar cliente;
    private Integer idAlbum, idGrupo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicacionesListaFragmentArgs = PublicacionesListaFragmentArgs.fromBundle(getArguments());
            idAlbum = publicacionesListaFragmentArgs.getIdAlbum();
            idGrupo = publicacionesListaFragmentArgs.getIdGrupo();
        }
        cliente = new CCAlbumFamiliar();
        publicaciones = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPublicacionesListaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        activity.setRefreshLayout(this);
        binding.botonVistaGrid.setOnClickListener(this);
        binding.botonNuevaPublicacion.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
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

    public void cargarGridPublicaciones(Integer idAlbum) {
        try {
            LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
            filtros.put("pea.COD_ALBUM", "="+idAlbum);
            LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
            ordenacion.put("p.titulo", "asc");
            publicaciones = cliente.leerPublicaciones(filtros,ordenacion);
        } catch (ExcepcionAlbumFamiliar e) {
            throw new RuntimeException(e);
        }
    }

    public void cargarLista() {
        if (!publicaciones.isEmpty()) {
            imagenesPublicaciones = new ArrayList<>();
            imagenesPublicaciones.add(R.drawable.imagen2);
            imagenesPublicaciones.add(R.drawable.imagen3);
            imagenesPublicaciones.add(R.drawable.imagen1);
            imagenesPublicaciones.add(R.drawable.imagen4);

            GridAdapter<Publicacion> adapter = new GridAdapter<Publicacion>(requireContext(), publicaciones, imagenesPublicaciones, true);
            binding.gridView.setAdapter(adapter);
        }
        else {
            binding.textoAlternativo.setText("No hay nada por aquí.");
        }
    }

    public void cargarVistaPublicaciones(Integer idAlbum) {
        Thread tarea = new Thread(() -> {
            try {
                cargarTituloAlbum(idAlbum);
                cargarGridPublicaciones(idAlbum);
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
        cargarLista();
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
}
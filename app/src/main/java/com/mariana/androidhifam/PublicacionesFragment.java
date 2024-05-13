package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentAlbumesBinding;
import com.mariana.androidhifam.databinding.FragmentPublicacionBinding;
import com.mariana.androidhifam.databinding.FragmentPublicacionesBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.Publicacion;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;

public class PublicacionesFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private PublicacionesFragmentArgs publicacionesFragmentArgs;
    private @NonNull FragmentPublicacionesBinding binding;
    private ArrayList<Integer> imagenesPublicaciones;
    private ArrayList<Publicacion> publicaciones;
    private CCAlbumFamiliar cliente;
    private Integer idAlbum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicacionesFragmentArgs = PublicacionesFragmentArgs.fromBundle(getArguments());
            idAlbum = publicacionesFragmentArgs.getIdAlbum();
        }
        cliente = new CCAlbumFamiliar();
        publicaciones = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPublicacionesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.botonVistaIndividual.setOnClickListener(this);
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

    public void cargarPublicaciones(Integer idAlbum) {
        try {
            LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
            filtros.put("pea.COD_ALBUM", "="+idAlbum);
            LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
            ordenacion.put("p.COD_PUBLICACION", "asc");
            publicaciones = cliente.leerPublicaciones(filtros,ordenacion);
        } catch (ExcepcionAlbumFamiliar e) {
            throw new RuntimeException(e);
        }
    }

    public void cargarGrid() {
        if (!publicaciones.isEmpty()) {
            imagenesPublicaciones = new ArrayList<>();
            imagenesPublicaciones.add(R.drawable.imagen2);
            imagenesPublicaciones.add(R.drawable.imagen3);
            imagenesPublicaciones.add(R.drawable.imagen1);
            imagenesPublicaciones.add(R.drawable.imagen4);

            GridAdapter<Publicacion> adapter = new GridAdapter<Publicacion>(requireContext(), publicaciones, imagenesPublicaciones, false);
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
    }

    public void menuPopUp() {
        PopupMenu popup = new PopupMenu(requireActivity(), binding.botonOpciones);
        popup.getMenuInflater()
                .inflate(R.menu.menu_album_admin, popup.getMenu());
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
            findNavController(v).navigate(PublicacionesFragmentDirections.actionPublicacionesFragmentToPublicacionesListaFragment(idAlbum));
        } else if (id == R.id.botonNuevaPublicacion) {
            findNavController(v).navigate(PublicacionesFragmentDirections.actionPublicacionesFragmentToNuevaPublicacionFragment(idAlbum));
        }
        else if (id == R.id.botonOpciones) {
            menuPopUp();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        findNavController(view).navigate(PublicacionesFragmentDirections.actionPublicacionesFragmentToPublicacionFragment((int)id));
    }
}
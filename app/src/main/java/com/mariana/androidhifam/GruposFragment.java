package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.mariana.androidhifam.databinding.FragmentGruposBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;

public class GruposFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private GruposFragmentArgs gruposFragmentArgs;
    private @NonNull FragmentGruposBinding binding;
    private ArrayList<Grupo> grupos;
    private ArrayList<Integer> imagenesGrupos;
    private CCAlbumFamiliar cliente;
    private TextView saludoUsuario;
    private Integer idUsuario;
    private Boolean animar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gruposFragmentArgs = GruposFragmentArgs.fromBundle(getArguments());
            idUsuario = gruposFragmentArgs.getIdUsuario();
        }
        animar = true;
        cliente = new CCAlbumFamiliar();
        grupos = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGruposBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        if (animar) {
            activity.mostrarToolbar(true);
            animar= false;
        }
        saludoUsuario = activity.findViewById(R.id.saludoUsuario);
        binding.botonNuevaFamilia.setOnClickListener(this);
        binding.gridView.setOnItemClickListener(this);
        cargarVistaGrupos(idUsuario);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void cargarNombreUsuario(Integer idUsuario) throws ExcepcionAlbumFamiliar {
        saludoUsuario.setText("¡Hey, " + cliente.leerUsuario(idUsuario).getNombre().split(" ", 2)[0] + "!");
    }

    public void cargarGrupos(Integer idUsuario) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("uig.COD_USUARIO", "="+idUsuario);
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("g.titulo", "asc");
        grupos = cliente.leerGrupos(filtros,ordenacion);
    }

    public void cargarGrid() {
        if (!grupos.isEmpty()) {
            imagenesGrupos = new ArrayList<>();
            imagenesGrupos.add(R.drawable.imagen2);
            imagenesGrupos.add(R.drawable.imagen3);
            imagenesGrupos.add(R.drawable.imagen1);
            imagenesGrupos.add(R.drawable.imagen4);
            GridAdapter<Grupo> adapter = new GridAdapter<>(requireContext(), grupos, imagenesGrupos, false);
            binding.gridView.setAdapter(adapter);
        }
        else {
            binding.textoAlternativo.setText("No hay nada por aquí.");
        }
    }

    public void cargarVistaGrupos(Integer idUsuario) {
        Thread tarea = new Thread(() -> {
            try {
                cargarNombreUsuario(idUsuario);
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonNuevaFamilia) {
            findNavController(v).navigate(GruposFragmentDirections.actionGruposFragmentToMenuAnyadirGrupoFragment());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        findNavController(view).navigate(GruposFragmentDirections.actionGruposFragmentToAlbumesFragment((int)id));
    }
}
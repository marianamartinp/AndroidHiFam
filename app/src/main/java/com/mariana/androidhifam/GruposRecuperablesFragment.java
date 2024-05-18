package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.mariana.androidhifam.databinding.FragmentGruposRecuperablesBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;

public class GruposRecuperablesFragment extends Fragment implements View.OnClickListener, View.OnCreateContextMenuListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout, ModalFragment.CustomModalInterface {

    private GruposRecuperablesFragmentArgs gruposRecuperablesFragmentArgs;
    private @NonNull FragmentGruposRecuperablesBinding binding;
    private ArrayList<Grupo> grupos;
    private ArrayList<Integer> imagenesGrupos;
    private CCAlbumFamiliar cliente;
    private TextView saludoUsuario;
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
        saludoUsuario = activity.findViewById(R.id.saludoUsuario);
        registerForContextMenu(binding.gridView);
        binding.botonNuevaFamilia.setOnClickListener(this);
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
        else if (idMenuItem == R.id.eliminarGrupo) {
            Toast.makeText(requireContext(), "Grupo eliminado.", Toast.LENGTH_SHORT).show();
            eliminarGrupo(itemId);
            refrescarFragment();
            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }
    }

    public void eliminarGrupo(int idGrupo) {
        Thread tarea = new Thread(() -> {
            try {
                cliente.eliminarGrupo(idGrupo);
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
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_album_admin, menu);

        GridView gv = (GridView) v;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = info.position;

    }

    public void cargarNombreUsuario(Integer idUsuario) throws ExcepcionAlbumFamiliar {
        saludoUsuario.setText("¡Hey, " + cliente.leerUsuario(idUsuario).getNombre().split(" ", 2)[0] + "!");
    }

    public void cargarGrupos(Integer idUsuario) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("uig.COD_USUARIO", "="+idUsuario);
        filtros.put("g.FECHA_ELIMINACION", "is not null");
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
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ModalFragment modal = new ModalFragment(this, "textoModal veryveryvery laaargooo", "botonPositivo", null);
        modal.show(getActivity().getSupportFragmentManager(), "testmodal");
    }

    private void refrescarFragment() {
        NavController navController = Navigation.findNavController(requireView());
        Integer id = navController.getCurrentDestination().getId();
        if (id != null) {
//            navController.popBackStack(id, true);
        }
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh() {
        cargarVistaGrupos(idUsuario);
        Toast.makeText(getContext(), "Se han actualizado las familias.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPositiveClick() {
        Toast.makeText(getContext(), "positivo.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNegativeClick() {
        Toast.makeText(getContext(), "negativo.", Toast.LENGTH_SHORT).show();
    }
}
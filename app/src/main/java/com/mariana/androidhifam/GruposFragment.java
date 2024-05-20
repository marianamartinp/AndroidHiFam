package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mariana.androidhifam.databinding.FragmentGruposBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;

public class GruposFragment extends Fragment implements View.OnClickListener, View.OnCreateContextMenuListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout {

    private GruposFragmentArgs gruposFragmentArgs;
    private @NonNull FragmentGruposBinding binding;
    private ArrayList<Grupo> grupos;
    private ArrayList<File> imagenesGrupos;
    private GridAdapter<Grupo> adapter;
    private CCAlbumFamiliar cliente;
    private TextView saludoUsuario;
    private Integer idUsuario;
    private Boolean animar;
    private  MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private String saludo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gruposFragmentArgs = GruposFragmentArgs.fromBundle(getArguments());
            idUsuario = gruposFragmentArgs.getIdUsuario();
        }
        animar = true;
        grupos = new ArrayList<>();
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGruposBinding.inflate(inflater, container, false);
        cliente = activity.getCliente();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (animar) {
            activity.mostrarToolbar(true);
            animar= false;
        }
        activity.setRefreshLayout(this);
        saludoUsuario = activity.findViewById(R.id.saludoUsuario);
        registerForContextMenu(binding.gridView);
        binding.botonNuevaFamilia.setOnClickListener(this);
        binding.botonPapelera.setOnClickListener(this);
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
            grupos.remove(position);
            adapter.notifyDataSetChanged();
            mostrarTextoAlternativo();
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
        inflater.inflate(R.menu.menu_grupos_admin, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonNuevaFamilia) {
            findNavController(v).navigate(GruposFragmentDirections.actionGruposFragmentToMenuAnyadirGrupoFragment());
        }
        else if (id == R.id.botonPapelera) {
            findNavController(v).navigate(GruposFragmentDirections.actionGruposFragmentToGruposRecuperablesFragment(idUsuario));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        findNavController(view).navigate(GruposFragmentDirections.actionGruposFragmentToAlbumesFragment((int)id));
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh() {
        cargarVistaGrupos(idUsuario);
        Toast.makeText(getContext(), "Se han actualizado las familias.", Toast.LENGTH_SHORT).show();
    }

    public void cargarNombreUsuario(Integer idUsuario) throws ExcepcionAlbumFamiliar {
        String nombreUsuario = cliente.leerUsuario(idUsuario).getNombre().split(" ", 2)[0];
        saludo = getString(R.string.saludoUsuarioPersonalizado, nombreUsuario);
//        saludoUsuario.setText(saludo);
    }

    public void cargarGrupos(Integer idUsuario) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("uig.COD_USUARIO", "="+idUsuario);
        filtros.put("g.FECHA_ELIMINACION", "is null");
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("g.titulo", "asc");
        grupos = cliente.leerGrupos(filtros,ordenacion);
    }

    public void cargarGrid() {
        imagenesGrupos = activity.getImagenes();
        adapter = new GridAdapter<>(requireContext(), grupos, imagenesGrupos, false);
        binding.gridView.setAdapter(adapter);
    }

    public void actualizarInterfaz() {
        cargarGrid();
        mostrarTextoAlternativo();
    }


    public void cargarVistaGrupos(Integer idUsuario) {
        executorService.execute(() -> {
            try {
                cargarNombreUsuario(idUsuario);
            } catch (ExcepcionAlbumFamiliar e) {
                throw new RuntimeException(e);
            }
            mainHandler.post(() -> {
                // Update UI components here
                saludoUsuario.setText(saludo);
            });
        });
        executorService.execute(() -> {
            try {
                cargarGrupos(idUsuario);

                // Post results back to the main thread
                mainHandler.post(() -> {
                    // Update UI components here
                    actualizarInterfaz();
                });
            } catch (ExcepcionAlbumFamiliar e) {
                // Handle exceptions here
            }
        });
        executorService.execute(() -> {
            activity.cargarImagenesDrive();

            // Post results back to the main thread
            mainHandler.post(() -> {
                // Update UI components here
                actualizarInterfaz();
            });
        });
//        Thread tarea = new Thread(() -> {
//            try {
//                cargarNombreUsuario(idUsuario);
//                activity.cargarImagenesDrive();
//                cargarGrupos(idUsuario);
//            } catch (ExcepcionAlbumFamiliar e) {
//                // Error
//            }
//        });
//        tarea.start();
//        try {
//            tarea.join(5000);
//        } catch (InterruptedException e) {
//            // Error
//        }
//                cargarGrid();
//                mostrarTextoAlternativo();

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


    public void mostrarTextoAlternativo() {
        if (grupos.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        }
        else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }

}
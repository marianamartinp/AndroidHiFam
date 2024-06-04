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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.sql.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;

public class GruposFragment extends Fragment implements View.OnClickListener, View.OnCreateContextMenuListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout {

    private GruposFragmentArgs gruposFragmentArgs;
    private @NonNull FragmentGruposBinding binding;
    private ArrayList<Grupo> grupos;
    private ArrayList<Album> albumes;
    private ArrayList<File> imagenesGrupos;
    private GridAdapter<Grupo> adapter;
    private CCAlbumFamiliar cliente;
    private TextView saludoUsuario;
    private Integer tokenUsuario;
    private Boolean animar;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private boolean vistaCreada = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gruposFragmentArgs = GruposFragmentArgs.fromBundle(getArguments());
            animar = gruposFragmentArgs.getAnimacionToolbar();
        }
        grupos = new ArrayList<>();
        albumes = new ArrayList<>();
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGruposBinding.inflate(inflater, container, false);
        cliente = activity.getCliente();
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (animar) {
            activity.mostrarToolbar(true, true);
        }
        else {
            activity.mostrarToolbar(true, false);
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
                // Método inutilizado
            }
        });
        binding.gridView.setOnItemClickListener(this);
        if (vistaCreada) {
            resumirVistaGrupos(tokenUsuario);
        }
        else {
            cargarVistaGrupos(tokenUsuario, null);
        }
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
        else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (activity.getHabilitarInteraccion()) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_context_grupos, menu);
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        }
    }

    @Override
    public void onClick(View v) {
        if (activity.getHabilitarInteraccion()) {
            int id = v.getId();
            if (id == R.id.botonNuevaFamilia) {
                findNavController(v).navigate(GruposFragmentDirections.actionGruposFragmentToMenuAnyadirGrupoFragment());
            } else if (id == R.id.botonPapelera) {
                findNavController(v).navigate(GruposFragmentDirections.actionGruposFragmentToGruposRecuperablesFragment());
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (activity.getHabilitarInteraccion()) {
            findNavController(view).navigate(GruposFragmentDirections.actionGruposFragmentToAlbumesFragment((int) id));
        }
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh(SwipeRefreshLayout refreshLayout) {
        if (activity.getHabilitarInteraccion()) {
            cargarVistaGrupos(tokenUsuario, refreshLayout);
        }
        else {
            refreshLayout.setRefreshing(false);
        }
    }


    public void cargarNombreUsuario(Integer tokenUsuario, CountDownLatch latch) {
        try {
            String nombreUsuario = cliente.leerUsuario(tokenUsuario).getNombre().split(" ", 2)[0];
            String saludo = getString(R.string.saludoUsuarioPersonalizado, nombreUsuario);
            mainHandler.post(() -> saludoUsuario.setText(saludo));
        }
        catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            latch.countDown();
        }
    }

    public void cargarGrupos(Integer tokenUsuario, CountDownLatch latch) {
        try {
            LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
            filtros.put("uig.COD_USUARIO", "=" + tokenUsuario);
            filtros.put("g.FECHA_ELIMINACION", "is null");
            LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
            ordenacion.put("g.titulo", "asc");
            grupos = cliente.leerGrupos(filtros, ordenacion);
            mainHandler.post(this::actualizarInterfaz);
        }
        catch(ExcepcionAlbumFamiliar e) {
            mainHandler.post(this::errorAlCargarInterfaz);
        }
        finally {
            latch.countDown();
        }
    }

    public void cargarGrid() {
        imagenesGrupos = activity.getImagenes();
        adapter = new GridAdapter<>(requireContext(), grupos, imagenesGrupos, false, albumes);
        binding.gridView.setAdapter(adapter);
    }

    public void cargarAlbumesNoEliminados(CountDownLatch latch) {
        try {
            LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
            filtros.put("a.FECHA_ELIMINACION", "is null");
            String codigosGrupos = "0 ";
            for (Grupo grupo : grupos) {
                codigosGrupos = codigosGrupos + ", " + grupo.getCodGrupo();
            }
            filtros.put("a.COD_GRUPO_CREA_ALBUM", "in (" + codigosGrupos + ")");
            albumes = cliente.leerAlbumes(filtros, null);
            mainHandler.post(this::actualizarInterfaz);
        } catch (ExcepcionAlbumFamiliar e) {
            mainHandler.post(() -> Toast.makeText(getContext(), "Se ha producido un error.", Toast.LENGTH_SHORT).show());
        }
        finally {
            if (null != latch) {
                latch.countDown();
            }
        }
    }

    public void actualizarInterfaz() {
        cargarGrid();
        mostrarTextoAlternativo();
    }

    public void cargarImagenesDrive(CountDownLatch latch) {
        try {
            activity.cargarImagenesDrive(true);
        } finally {
            latch.countDown();
        }
    }

    public void cargarVistaGrupos(Integer tokenUsuario, SwipeRefreshLayout refreshLayout) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        CountDownLatch latch = new CountDownLatch(4);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> cargarNombreUsuario(tokenUsuario, latch));
        executorService.execute(() -> cargarGrupos(tokenUsuario, latch));
        executorService.execute(() -> {
            cargarImagenesDrive(latch);
            cargarAlbumesNoEliminados(latch);
        });
        executorService.execute(() -> {
            try {
                latch.await();
                mainHandler.post(() -> {
                    binding.gridView.clearAnimation();
                    if (null != refreshLayout) {
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "Se han actualizado las familias.", Toast.LENGTH_SHORT).show();
                    }
                    activity.setHabilitarInteraccion(true);
                    vistaCreada = true;
                });
            } catch (InterruptedException e) {
                mainHandler.post(this::errorAlCargarInterfaz);
            }
        });
    }

    public void resumirVistaGrupos(Integer tokenUsuario) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        CountDownLatch latch = new CountDownLatch(2);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> {
            cargarGrupos(tokenUsuario, latch);
            cargarAlbumesNoEliminados(latch);
        });
        executorService.execute(() -> {
            try {
                latch.await();
                mainHandler.post(() -> {
                    binding.gridView.clearAnimation();
                    activity.setHabilitarInteraccion(true);
                    vistaCreada = true;
                });
            } catch (InterruptedException e) {
                mainHandler.post(this::errorAlCargarInterfaz);
            }
        });
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


    public void errorAlCargarInterfaz() {
        Toast.makeText(getContext(), "Error al cargar las familias.", Toast.LENGTH_SHORT).show();
    }

}
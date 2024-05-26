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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mariana.androidhifam.databinding.FragmentGruposRecuperablesBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;

public class GruposRecuperablesFragment extends Fragment implements View.OnCreateContextMenuListener, AdapterView.OnItemClickListener, MainActivity.SwipeToRefreshLayout, ModalFragment.CustomModalInterface {

    private GruposRecuperablesFragmentArgs gruposRecuperablesFragmentArgs;
    private @NonNull FragmentGruposRecuperablesBinding binding;
    private ArrayList<Grupo> grupos;
    private ArrayList<File> imagenesGrupos;
    private GridAdapter<Grupo> adapter;
    private CCAlbumFamiliar cliente;
    private Integer idUsuario;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private boolean vistaCreada = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gruposRecuperablesFragmentArgs = GruposRecuperablesFragmentArgs.fromBundle(getArguments());
            idUsuario = gruposRecuperablesFragmentArgs.getIdUsuario();
        }
        cliente = new CCAlbumFamiliar();
        grupos = new ArrayList<>();
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
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
        registerForContextMenu(binding.gridView);
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
        if (vistaCreada) {
            actualizarInterfaz();
        }
        else {
            cargarVistaGrupos(idUsuario, false);
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

        if (idMenuItem == R.id.recuperarGrupo) {
            modalRecuperarGrupo(position, itemId);
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
            inflater.inflate(R.menu.menu_grupos_recuperables, menu);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (activity.getHabilitarInteraccion()) {
            modalRecuperarGrupo(position, (int) id);
        }
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh() {
        cargarVistaGrupos(idUsuario, true);
    }

    @Override
    public void onPositiveClick(Integer position, Integer id) {
        if (activity.getHabilitarInteraccion()) {
            recuperarGrupo(position, id);
        }
    }

    public void cargarGrupos(Integer idUsuario) {
        try {
            LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
            filtros.put("g.COD_USUARIO_ADMIN_GRUPO", "=" + idUsuario);
            filtros.put("g.FECHA_ELIMINACION", "is not null");
            LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
            ordenacion.put("g.titulo", "asc");
            grupos = cliente.leerGrupos(filtros, ordenacion);
            mainHandler.post(this::actualizarInterfaz);
        }
        catch (ExcepcionAlbumFamiliar e) {
            throw new RuntimeException(e);
        }
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

    public void cargarVistaGrupos(Integer idUsuario, boolean refrescar) {
        activity.setHabilitarInteraccion(false);
        Animation parpadeo = AnimationUtils.loadAnimation(getContext(), R.anim.parpadeo);
        binding.gridView.startAnimation(parpadeo);
        executorService.execute(() -> {
            cargarGrupos(idUsuario);
            binding.gridView.clearAnimation();
            if (refrescar) {
                Toast.makeText(getContext(), "Se han actualizado las familias eliminadas.", Toast.LENGTH_SHORT).show();
            }
            activity.setHabilitarInteraccion(true);
            vistaCreada = true;
        });
    }

    public void modalRecuperarGrupo(int position, int id) {
        ModalFragment modal = new ModalFragment(position, (int) id, this, "¿Desea recuperar este grupo?", getString(R.string.btnRecuperar), getString(R.string.btnCancelar));
        modal.show(getActivity().getSupportFragmentManager(), "modalRecuperarGrupo");
    }

    public void recuperarGrupo(int position, int idGrupo) {
        executorService.execute(() -> {
            try {
                cliente.restaurarGrupo(idGrupo);
                mainHandler.post(() -> {
                    grupos.remove(position);
                    adapter.notifyDataSetChanged();
                    mostrarTextoAlternativo();
                    Toast.makeText(requireContext(), "Grupo recuperado.", Toast.LENGTH_SHORT).show();
                });
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(this::errorAlCargarInterfaz);
            }
        });
    }

    public void mostrarTextoAlternativo() {
        if (grupos.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        } else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }

    public void errorAlCargarInterfaz() {
        Toast.makeText(getContext(), "Error al cargar las familias eliminadas.", Toast.LENGTH_SHORT).show();
    }
}
package com.mariana.androidhifam;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.mariana.androidhifam.databinding.FragmentDetallesGrupoBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.Usuario;
import utils.ViewPagerAdapter;

public class DetallesGrupoFragment extends Fragment implements View.OnClickListener {

    private @NonNull DetallesGrupoFragmentArgs detallesGrupoFragmentArgs;
    private @NonNull FragmentDetallesGrupoBinding binding;
    private NavController navController;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private Integer idGrupo, paginaActual, tokenUsuario;
    private Grupo grupo;

    // Método de creación del fragmento, inicializa variables y recupera argumentos.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtiene los argumentos pasados al Fragment
        if (getArguments() != null) {
            detallesGrupoFragmentArgs = DetallesGrupoFragmentArgs.fromBundle(getArguments());
            idGrupo = detallesGrupoFragmentArgs.getIdGrupo();
            paginaActual = detallesGrupoFragmentArgs.getPaginaActual();
        }
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        cliente = new CCAlbumFamiliar();
    }

    // Método que se ejecuta al crear la vista
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.findViewById(R.id.refreshLayout).setEnabled(false);
        navController = NavHostFragment.findNavController(this);
        tokenUsuario = Integer.parseInt(activity.getToken());
        // Infla el layout del Fragment y obtiene una instancia del binding
        binding = FragmentDetallesGrupoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // Método que se ejecuta una vez la vista ha sido creada
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Carga los detalles del grupo
        cargarGrupo();
        // Configura los fragmentos para el ViewPager2
        ArrayList<Fragment> pantallas = new ArrayList<>();
        pantallas.add(new TabDetallesGrupoFragment());
        pantallas.add(new TabMiembrosGrupoFragment());
        binding.botonAtras.setOnClickListener(this);
        binding.viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), getLifecycle(), pantallas));

        // Registra un callback para cuando cambia la página del ViewPager2
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
            }
        });

        // Agrega un listener al TabLayout para cambiar la página del ViewPager2 cuando se selecciona una pestaña
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No precisado.
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No precisado.
            }
        });

        // Cambia a la página guardada anteriormente en el ViewPager2. Esto sirve para decidir la página
        // que se va a mostrar inicialmente al llamarse desde otros fragments.
        binding.viewPager.post(() -> {
            binding.viewPager.setCurrentItem(paginaActual);
        });
    }

    // Método para manejar los eventos de clic en los botones
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonModificarFamilia) {
            modificarGrupo();
        }
        else if (id == R.id.botonAtras) {
            navController.popBackStack();
        }
    }

    // Método llamado al destruir la vista del Fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Método para obtener el ID del grupo recibido por argumento desde el fragment hijo
    public Integer getGrupoId() {
        return idGrupo;
    }

    // Método para modificar los detalles del grupo
    public void modificarGrupo() {

        // Obtiene el fragmento que muestra los detalles del grupo
        TabDetallesGrupoFragment informacionGrupo = (TabDetallesGrupoFragment) getChildFragmentManager().findFragmentByTag("f0");
        if (null != informacionGrupo && informacionGrupo.getCamposModificados()) {
            String tituloFamilia = informacionGrupo.getEditextTituloFamilia();
            String descripcionFamilia = informacionGrupo.getEditextDescripcionFamilia();
            Date fechaCreacion = informacionGrupo.getFechaCreacion();
            if (!tituloFamilia.isEmpty()) {
                Grupo grupo = new Grupo();
                grupo.setFechaCreacion(fechaCreacion);
                grupo.setTitulo(tituloFamilia);
                grupo.setDescripcion(descripcionFamilia);
                Usuario usuarioAdmin = new Usuario();
                usuarioAdmin.setCodUsuario(tokenUsuario);
                grupo.setUsuarioAdminGrupo(usuarioAdmin);

                executorService.execute(() -> {
                    try {
                        cliente.modificarGrupo(idGrupo, grupo);
                        mainHandler.post(() -> {
                            Toast.makeText(requireContext(), "La familia se ha modificado correctamente.", Toast.LENGTH_SHORT).show();
                            navController.popBackStack();
                        });
                    } catch (ExcepcionAlbumFamiliar e) {
                        manejadorExcepcionAlbumFamiliar(e);
                    }
                });
            } else {
                Toast.makeText(requireContext(), "El título de la familia es obligatorio.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            navController.popBackStack();
        }
    }

    // Método para cargar los detalles del grupo desde el servidor.
    public void cargarGrupo() {
        executorService.execute(() -> {
            try {
                grupo = cliente.leerGrupo(idGrupo);
                mainHandler.post(this::revisarPermisos);
            } catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    // Método para manejar excepciones relacionadas con operaciones en el servidor de forma genérica.
    public void manejadorExcepcionAlbumFamiliar(ExcepcionAlbumFamiliar e) {
        String mensaje;
        mensaje = e.getMensajeUsuario();
        mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
    }

    // Método para revisar los permisos del usuario actual sobre el grupo.
    public void revisarPermisos() {
        // Permisos en función de si el usuario es admin o no
        if (Objects.equals(tokenUsuario, grupo.getUsuarioAdminGrupo().getCodUsuario())) {
            binding.botonModificarFamilia.setVisibility(View.VISIBLE);
            binding.botonModificarFamilia.setOnClickListener(this);
        }
        else {
            binding.botonModificarFamilia.setVisibility(View.INVISIBLE);
        }
    }
}
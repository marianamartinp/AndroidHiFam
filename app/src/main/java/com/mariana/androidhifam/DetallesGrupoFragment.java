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
import com.mariana.androidhifam.databinding.FragmentRegistroBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.Usuario;

public class DetallesGrupoFragment extends Fragment implements View.OnClickListener {

    private DetallesGrupoFragmentArgs detallesGrupoFragmentArgs;
    private FragmentDetallesGrupoBinding binding;
    private NavController navController;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private Integer idGrupo, tokenUsuario;
    private Grupo grupo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            detallesGrupoFragmentArgs = DetallesGrupoFragmentArgs.fromBundle(getArguments());
            idGrupo = detallesGrupoFragmentArgs.getIdGrupo();
        }
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        cliente = new CCAlbumFamiliar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        navController = NavHostFragment.findNavController(this);
        tokenUsuario = Integer.parseInt(activity.getToken());
        binding = FragmentDetallesGrupoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarGrupo();
        ArrayList<Fragment> pantallas = new ArrayList<>();
        pantallas.add(new TabDetallesGrupoFragment());
        pantallas.add(new TabMiembrosGrupoFragment());
        binding.botonAtras.setOnClickListener(this);
        binding.viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), getLifecycle(), pantallas));
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
            }
        });
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
    }

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

    public Integer getGrupoId() {
        return idGrupo;
    }

    public void modificarGrupo() {
        TabDetallesGrupoFragment informacionGrupo = (TabDetallesGrupoFragment) getChildFragmentManager().findFragmentByTag("f0");
        if (null != informacionGrupo && informacionGrupo.getCamposModificados()) {
            String tituloFamilia = informacionGrupo.getEditextTituloFamilia();
            String descripcionFamilia = informacionGrupo.getEditextDescripcionFamilia();
            Date fechaCreacion = informacionGrupo.getFechaCreacion();
            if (!tituloFamilia.isEmpty()) {
                Grupo grupo = new Grupo();
                grupo.setFechaCreacion(fechaCreacion);
                grupo.setTitulo(tituloFamilia);
                if (!descripcionFamilia.isEmpty()) {
                    grupo.setDescripcion(descripcionFamilia);
                }
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

    public void manejadorExcepcionAlbumFamiliar(ExcepcionAlbumFamiliar e) {
        String mensaje;
        mensaje = e.getMensajeUsuario();
        mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
    }

    public void revisarPermisos() {
        if (Objects.equals(tokenUsuario, grupo.getUsuarioAdminGrupo().getCodUsuario())) {
            binding.botonModificarFamilia.setVisibility(View.VISIBLE);
            binding.botonModificarFamilia.setOnClickListener(this);
        }
        else {
            binding.botonModificarFamilia.setVisibility(View.INVISIBLE);
        }
    }
}
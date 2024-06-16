package com.mariana.androidhifam;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.mariana.androidhifam.databinding.FragmentDetallesUsuarioBinding;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import utils.ViewPagerAdapter;

public class DetallesUsuarioFragment extends Fragment implements View.OnClickListener {

    private @NonNull FragmentDetallesUsuarioBinding binding;
    private NavController navController;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private Integer tokenUsuario;

    // Método llamado cuando se crea el Fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        cliente = new CCAlbumFamiliar();
    }

    // Método llamado cuando se crea la interfaz de usuario del Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity.findViewById(R.id.refreshLayout).setEnabled(false);
        navController = NavHostFragment.findNavController(this);
        tokenUsuario = Integer.parseInt(activity.getToken());
        // Inflar y retornar el binding de la interfaz de usuario
        binding = FragmentDetallesUsuarioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // Método llamado después de que se haya creado la interfaz de usuario del Fragment
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Crear una lista de fragmentos para el ViewPager2
        ArrayList<Fragment> pantallas = new ArrayList<>();
        pantallas.add(new TabDetallesUsuarioFragment());
        pantallas.add(new TabGruposUsuarioFragment());

        // Configurar el adaptador y el callback del ViewPager2
        binding.botonAtras.setOnClickListener(this);
        binding.viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), getLifecycle(), pantallas));
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
            }
        });

        // Manejar la selección de pestañas en el TabLayout
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

    // Método para manejar clics en vistas
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonAtras) {
            navController.popBackStack();
        }
    }

    // Método llamado al destruir la vista del Fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
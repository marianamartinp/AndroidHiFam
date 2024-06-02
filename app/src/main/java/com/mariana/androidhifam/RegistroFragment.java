package com.mariana.androidhifam;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentRegistroBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Usuario;

public class RegistroFragment extends Fragment implements View.OnClickListener {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_TOKEN = "userToken";
    private FragmentRegistroBinding binding;
    private NavController navController;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private String nombre, usuario, correo, telefono, fechaNacimiento, contrasenyaHasheada;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        cliente = new CCAlbumFamiliar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        navController = NavHostFragment.findNavController(this);
        binding = FragmentRegistroBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SpannableString content = new SpannableString(binding.linkIniciarSesion.getText()) ;
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0) ;
        binding.linkIniciarSesion.setText(content);

        ArrayList<Fragment> pantallas = new ArrayList<>();
        pantallas.add(new PrimeraPaginaRegistroFragment());
        pantallas.add(new SegundaPaginaRegistroFragment());

        binding.viewPager.setAdapter(new ViewPagerAdapter(requireActivity().getSupportFragmentManager(), getLifecycle(), pantallas));
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        });

        binding.btnSiguienteLogin.setOnClickListener(this);
        binding.botonAtras.setOnClickListener(this);
        binding.linkIniciarSesion.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSiguienteLogin) {
            if (binding.viewPager.getCurrentItem() == 0) {
                procesarPrimeraPantalla();
            }
            else {
                procesarSegundaPantalla();
            }
        }
        else if (id == R.id.botonAtras) {
            binding.viewPager.setCurrentItem(0);
            binding.btnSiguienteLogin.setText(getString(R.string.btnSiguiente));
            binding.botonAtras.setVisibility(View.INVISIBLE);
        }
        else if (id == R.id.linkIniciarSesion) {
            navController.popBackStack();
        }
    }

    private void procesarPrimeraPantalla() {
        PrimeraPaginaRegistroFragment primeraPantalla = (PrimeraPaginaRegistroFragment) activity.getSupportFragmentManager().findFragmentByTag("f" + binding.viewPager.getCurrentItem());
        if (null != primeraPantalla && primeraPantalla.validacionCompleta()) {
            nombre = primeraPantalla.getEditextNombre();
            usuario = primeraPantalla.getEditextUsuario();
            correo = primeraPantalla.getEditextCorreo();
            telefono = primeraPantalla.getEditextTelefonoConPrefijo();
            binding.viewPager.setCurrentItem(1);
            binding.btnSiguienteLogin.setText(getString(R.string.btnRegistro));
            binding.botonAtras.setVisibility(View.VISIBLE);
        }
        else {
            Toast.makeText(getContext(), "Completa la información para continuar.", Toast.LENGTH_SHORT).show();
        }
    }

    private void procesarSegundaPantalla() {
        SegundaPaginaRegistroFragment segundaPantalla = (SegundaPaginaRegistroFragment) activity.getSupportFragmentManager().findFragmentByTag("f" + binding.viewPager.getCurrentItem());
        if (null != segundaPantalla && segundaPantalla.validacionCompleta()) {
            fechaNacimiento = segundaPantalla.getEditextFechaNacimiento();
            contrasenyaHasheada = Utils.hashearContrasenya(segundaPantalla.getEditextContrasenya());
            executorService.execute(() -> {
                try {
                    Usuario nuevoUsuario = new Usuario(null, nombre, usuario, correo, telefono, contrasenyaHasheada, Utils.parsearFechaADate(fechaNacimiento), null);
                    int resultadoInsercion = cliente.insertarUsuario(nuevoUsuario);
                    ArrayList<Usuario> usuariosInsertados;
                    if (resultadoInsercion > 0) {
                        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                        filtros.put("u.FECHA_ELIMINACION", "is null");
                        filtros.put("u.EMAIL", "='" + correo + "'");
                        filtros.put("u.CONTRASENYA", "='" + contrasenyaHasheada + "'");
                        usuariosInsertados = cliente.leerUsuarios(filtros, null);

                        if (!usuariosInsertados.isEmpty()) {
                            Integer idUsuario = usuariosInsertados.get(0).getCodUsuario();
                            String token = String.valueOf(idUsuario);
                            // Save login state and token
                            SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(KEY_IS_LOGGED_IN, true);
                            editor.putString(KEY_USER_TOKEN, token);
                            editor.apply();
                        }
                    } else {
                        usuariosInsertados = new ArrayList<>();
                    }
                    mainHandler.post(() -> {
                        if (resultadoInsercion > 0 && !usuariosInsertados.isEmpty()) {
                            navController.navigate(RegistroFragmentDirections.actionRegistroFragmentToGruposFragment());
                        }
                        else {
                            Toast.makeText(getContext(), "Completa la información para continuar.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (ExcepcionAlbumFamiliar e) {
                    throw new RuntimeException(e);
                }
            });

        }
        else {
            Toast.makeText(getContext(), "Completa la información para continuar.", Toast.LENGTH_SHORT).show();
        }
    }

}
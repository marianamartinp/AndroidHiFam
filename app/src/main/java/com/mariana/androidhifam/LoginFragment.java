package com.mariana.androidhifam;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mariana.androidhifam.databinding.FragmentLoginBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Usuario;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_TOKEN = "userToken";
    private FragmentLoginBinding binding;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private NavController navController;
    private boolean mostrarContrasenya;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        cliente = new CCAlbumFamiliar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        navController = NavHostFragment.findNavController(this);
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = (MainActivity) getActivity();
        SpannableString content = new SpannableString(binding.linkRegistro.getText()) ;
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0) ;
        binding.linkRegistro.setText(content);
        binding.btnLogin.setOnClickListener(this);
        binding.linkRegistro.setOnClickListener(this);
        binding.botonMostrarContrasenya.setOnClickListener(this);
    }

    public void iniciarSesion() {
        boolean esEmail;
        String usuarioOEmail = binding.editextUsuario.getText().toString().trim();
        String contrasenyaUsuario = binding.editextContrasenya.getText().toString();
        if (!usuarioOEmail.isEmpty() && !contrasenyaUsuario.isEmpty()) {
            if (ValidadorEmail.esEmailValido(usuarioOEmail)) {
                esEmail = true;
            } else {
                esEmail = false;
            }

            LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
            filtros.put("u.FECHA_ELIMINACION", "is null");
            if (esEmail) {
                filtros.put("u.EMAIL", "= '" + usuarioOEmail + "'");
            } else {
                filtros.put("u.USUARIO", "= '" + usuarioOEmail + "'");
            }
            executorService.execute(() -> {
                try {
                    ArrayList<Usuario> usuarios = cliente.leerUsuarios(filtros, null);
                    mainHandler.post(() -> {
                        boolean autenticacionLograda = false;
                        int idUsuario = 0;

                        if (null != usuarios && !usuarios.isEmpty()) {
                            idUsuario = usuarios.get(0).getCodUsuario();
                            String contrasenya = usuarios.get(0).getContrasenya().trim();
                            autenticacionLograda = Utils.comprobarContrasenya(contrasenyaUsuario, contrasenya);
                        }
                        if (autenticacionLograda) {
                            String token = String.valueOf(idUsuario);
                            // Save login state and token
                            SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(KEY_IS_LOGGED_IN, true);
                            editor.putString(KEY_USER_TOKEN, token);
                            editor.apply();
                            navController.navigate(LoginFragmentDirections.actionLoginFragmentToGruposFragment());
                        } else {
                            Toast.makeText(getContext(), "Credenciales incorrectas. Vuelve a intentarlo.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (ExcepcionAlbumFamiliar e) {
                    mainHandler.post(() -> Toast.makeText(getContext(), e.getCodErrorBd(), Toast.LENGTH_SHORT).show());
                }
            });
        }
        else {
            Toast.makeText(getContext(), "Completa los campos para continuar.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnLogin) {
            iniciarSesion();
        }
        else if (id == R.id.linkRegistro) {
            navController.navigate(LoginFragmentDirections.actionLoginFragmentToRegistroFragment());
        }
        else if (id == R.id.botonMostrarContrasenya) {
            mostrarContrasenya();
        }
    }

    private void mostrarContrasenya(){
        if (!mostrarContrasenya) {
            binding.botonMostrarContrasenya.setImageResource(R.drawable.eye);
            binding.editextContrasenya.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            mostrarContrasenya = true;
        }
        else {
            binding.botonMostrarContrasenya.setImageResource(R.drawable.eyeoff);
            binding.editextContrasenya.setTransformationMethod(PasswordTransformationMethod.getInstance());
            mostrarContrasenya = false;
        }
    }
    

}
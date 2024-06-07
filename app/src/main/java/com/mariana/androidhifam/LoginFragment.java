package com.mariana.androidhifam;


import static android.content.Context.MODE_PRIVATE;

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
import utils.Utils;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_TOKEN = "userToken";
    private @NonNull LoginFragmentArgs loginFragmentArgs;
    private @NonNull FragmentLoginBinding binding;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private NavController navController;
    private boolean mostrarContrasenya, usuarioLoggeado, vistaUtilizada;
    private int animar;
    private static int ANIMACION_AVANZAR = 1;
    private static int ANIMACION_RETROCEDER = 2;

    // Método que se llama cuando se crea el fragmento
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            loginFragmentArgs = LoginFragmentArgs.fromBundle(getArguments());
            animar = loginFragmentArgs.getAnimacionToolbar();
        }
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        cliente = new CCAlbumFamiliar();
    }

    // Método que se llama para crear la vista del fragmento
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity.findViewById(R.id.refreshLayout).setEnabled(false);
        navController = NavHostFragment.findNavController(this);
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        usuarioLoggeado = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        return binding.getRoot();
    }

    // Método que se llama después de que la vista ha sido creada
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Mostramos la barra de herramientas con animaciones o sin ellas en función del fragment anterior que llame
        // a la vista
        if (animar == ANIMACION_RETROCEDER && !vistaUtilizada) {
            activity.mostrarToolbar(false, true, false);
            vistaUtilizada = true;
        }
        else if (animar == ANIMACION_AVANZAR) {
            activity.mostrarToolbar(false, true, true);
            vistaUtilizada = true;
        }
        else if (!usuarioLoggeado){
            activity.mostrarToolbar(false, false, false);
        }

        // Subrayamos el texto del enlace de registro
        SpannableString content = new SpannableString(binding.linkRegistro.getText()) ;
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0) ;
        binding.linkRegistro.setText(content);
        // Establecemos los listeners para los botones
        binding.btnLogin.setOnClickListener(this);
        binding.linkRegistro.setOnClickListener(this);
        binding.botonMostrarContrasenya.setOnClickListener(this);
    }

    // Método que se llama cuando la vista se destruye
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Método que se llama cuando se hace clic en un botón
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

    // Método para iniciar sesión
    public void iniciarSesion() {
        boolean esEmail;
        String usuarioOEmail = binding.editextUsuario.getText().toString().trim();
        String contrasenyaUsuario = binding.editextContrasenya.getText().toString();
        // Verificamos si es un email válido y si hay contraseña introducida
        if (!usuarioOEmail.isEmpty() && !contrasenyaUsuario.isEmpty()) {
            if (Utils.esEmailValido(usuarioOEmail)) {
                esEmail = true;
            } else {
                esEmail = false;
            }

            // Ejecutamos la búsqueda en segundo plano del usuario
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
                            // Guardamos el estado de login y el token del usuario
                            SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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

    // Método para mostrar u ocultar la contraseña con el botón de ojo de la interfaz
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
package com.mariana.androidhifam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mariana.androidhifam.databinding.FragmentModificarContrasenyaBinding;
import com.mariana.androidhifam.databinding.FragmentNuevoComentarioBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Comentario;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;
import pojosalbumfamiliar.Usuario;
import utils.Utils;

public class ModificarContrasenyaFragment extends DialogFragment implements View.OnClickListener, DialogInterface.OnDismissListener {

    private @NonNull FragmentModificarContrasenyaBinding binding;
    private CCAlbumFamiliar cliente;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private boolean mostrarContrasenya, validezContrasenya;
    private Usuario usuario;
    private Integer tokenUsuario, idPublicacion;
    private View rootView;
    private ViewTreeObserver.OnGlobalLayoutListener listenerGlobal;

    // Método onCreate: se llama cuando se crea la instancia del fragmento.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Método onCreateDialog: se llama para crear y configurar el diálogo del fragmento.
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Infla el diseño de la interfaz de usuario.
        binding = FragmentModificarContrasenyaBinding.inflate(getLayoutInflater());
        cliente = activity.getCliente();
        tokenUsuario = Integer.parseInt(activity.getToken());
        // Crea un constructor de AlertDialog con el contexto de la actividad principal.
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(binding.getRoot());
        binding.botonModificarContrasenya.setOnClickListener(this);
        binding.botonMostrarContrasenya.setOnClickListener(this);
        binding.botonAtras.setOnClickListener(this);

        // Registrar un OnGlobalLayoutListener para detectar cambios en la visibilidad del teclado
        rootView = activity.findViewById(R.id.rootViewUsuario);
        listenerGlobal = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (tecladoEscondido(rootView)) {
                    eliminarFocusEditext();
                }
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(listenerGlobal);

        // Configurar el listener para el EditText de la contraseña
        binding.editextContrasenya.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String contrasenya = binding.editextContrasenya.getText().toString().trim();
                    if (!contrasenya.isEmpty()) {
                        if (Utils.validarContrasenya(contrasenya)) {
                            validarEditext(binding.editextContrasenya, Utils.EnumValidacionEditText.VALIDO);
                        }
                        else {
                            validarEditext(binding.editextContrasenya, Utils.EnumValidacionEditText.FORMATO_NO_VALIDO);
                        }
                    } else {
                        validarEditext(binding.editextContrasenya, Utils.EnumValidacionEditText.VACIO);
                    }
                }
            }
        });
        cargarDetallesUsuario();
        return builder.create();
    }

    // Método onDestroyView: se llama cuando se destruye la vista del fragmento.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(listenerGlobal);
        binding = null;
    }

    // Método onClick: se llama cuando se hace clic en un elemento de la interfaz de usuario.
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonModificarContrasenya) {
            modificarContrasenya();
        }
        else if (id == R.id.botonMostrarContrasenya) {
            mostrarContrasenya();
        }
        else if (id == R.id.botonAtras) {
            dismiss();
        }
    }

    // Método onResume: se llama cuando el fragmento vuelve a estar visible para el usuario.
    @Override
    public void onResume() {
        super.onResume();
        // Modificación del alto y ancho del modal
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        int marginAncho = getResources().getDimensionPixelSize(R.dimen.dialog_ingreso_grupo_width_margin);
        int marginAlto = getResources().getDimensionPixelSize(R.dimen.dialog_ingreso_grupo_heigth_margin);
        int anchoPantalla = getResources().getDisplayMetrics().widthPixels;
        int altoPantalla = getResources().getDisplayMetrics().heightPixels;
        params.width = anchoPantalla - (2 * marginAncho);
        params.height = altoPantalla - (2 * marginAlto);
        window.setAttributes(params);
    }

    // Método onDismiss: se llama cuando el diálogo se cierra.
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    // Método insertarComentario que se encarga de enviar el comentario al servidor.
    public void modificarContrasenya() {
        if (validezContrasenya) {
            executorService.execute(() -> {
                try {
                    String contrasenyaHasheada = Utils.hashearContrasenya(binding.editextContrasenya.getText().toString().trim());
                    usuario.setContrasenya(contrasenyaHasheada);
                    // Inserta el comentario utilizando el cliente del álbum familiar.
                    cliente.modificarUsuario(tokenUsuario, usuario);
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "Se ha modificado tu contraseña.", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                } catch (ExcepcionAlbumFamiliar e) {
                    String mensaje = e.getMensajeUsuario();
                    mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    // Método para cargar los detalles del usuario
    public void cargarDetallesUsuario() {
        executorService.execute(() -> {
            try {
                usuario = cliente.leerUsuario(tokenUsuario);
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(getContext(), "Se ha producido un error al cargar la información.", Toast.LENGTH_SHORT).show());
            }
        });
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

    // Método para verificar si el teclado está oculto
    private boolean tecladoEscondido(View rootView) {
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        int alturaPantalla = rootView.getHeight();
        int alturaTeclado = alturaPantalla - r.bottom;

        // Determinar si el teclado está visible o no
        return alturaTeclado < alturaPantalla * 0.15; // Umbral arbitrario para detectar la visibilidad del teclado
    }

    // Método para quitar el foco de los EditText (lo cual derivará en una validación)
    private void eliminarFocusEditext() {
        // Quitar el enfoque de los campos de entrada
        binding.editextContrasenya.clearFocus();
    }

    // Método para mostrar las validaciones de la interfaz gráfica
    private void validarEditext(EditText editext, Utils.EnumValidacionEditText valorValidacion) {
        int id = editext.getId();

        switch (valorValidacion) {
            case VALIDO:
                if (id == R.id.editextContrasenya) {
                    validezContrasenya = true;
                }
                editext.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.contorno_gris, activity.getTheme()));
                break;

            case VACIO:
                if (id == R.id.editextContrasenya) {
                    validezContrasenya = false;
                    Toast.makeText(getContext(), "Ingrese una contraseña.", Toast.LENGTH_SHORT).show();
                }
                editext.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.contorno_naranja, activity.getTheme()));
                break;

            case FORMATO_NO_VALIDO:
                if (id == R.id.editextContrasenya) {
                    validezContrasenya = false;
                    Toast.makeText(getContext(), "Mínimo: 8 caracteres, 1 mayúscula, 1 minúscula, 1 dígito, 1 caracter especial.", Toast.LENGTH_LONG).show();
                }
                editext.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.contorno_naranja, activity.getTheme()));
                break;
        }
    }
}
package com.mariana.androidhifam;

import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentLoginBinding;
import com.mariana.androidhifam.databinding.FragmentPrimeraPaginaRegistroBinding;
import com.mariana.androidhifam.databinding.FragmentRegistroBinding;

import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;

public class PrimeraPaginaRegistroFragment extends Fragment {

    private FragmentPrimeraPaginaRegistroBinding binding;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private NavController navController;
    private boolean validezNombre, validezUsuario, validezCorreo, validezTelefono, validezPrefijo = true;
    private enum EnumValidacionEditText {
        VALIDO, FORMATO_NO_VALIDO, ERROR, VACIO, EN_USO
    }

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
        binding = FragmentPrimeraPaginaRegistroBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.editextUsuario.setFilters(new InputFilter[]{new FiltroNombreUsuario()});

        final View rootView = activity.findViewById(R.id.rootViewRegistro);
        // Registrar un OnGlobalLayoutListener para detectar cambios en la visibilidad del teclado
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (tecladoEscondido(rootView)) {
                    eliminarFocusEditext();
                }
            }
        });

        binding.editextCorreoElectronico.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String email = binding.editextCorreoElectronico.getText().toString().trim();
                    if (!email.isEmpty()) {
                        if (ValidadorEmail.esEmailValido(email)) {
                            executorService.execute(() -> {
                                try {
                                    LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                                    filtros.put("u.EMAIL", "= '" + email + "'");
                                    int usuariosExistentes = cliente.leerUsuarios(filtros, null).size();
                                    mainHandler.post(() -> {
                                        if (usuariosExistentes > 0) {
                                            validarEditext(binding.editextCorreoElectronico, EnumValidacionEditText.EN_USO);
                                        } else {
                                            validarEditext(binding.editextCorreoElectronico, EnumValidacionEditText.VALIDO);
                                        }
                                    });
                                }
                                catch (ExcepcionAlbumFamiliar e) {
                                    mainHandler.post(() -> {
                                        validarEditext(binding.editextCorreoElectronico, EnumValidacionEditText.ERROR);
                                    });
                                }
                            });
                        }
                        else {
                            validarEditext(binding.editextCorreoElectronico, EnumValidacionEditText.FORMATO_NO_VALIDO);
                        }
                    }
                    else {
                        validarEditext(binding.editextCorreoElectronico, EnumValidacionEditText.VACIO);
                    }
                }
            }
        });

        binding.editextNombre.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String nombre = binding.editextNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        validarEditext(binding.editextNombre, EnumValidacionEditText.VACIO);
                    }
                    else {
                        validarEditext(binding.editextNombre, EnumValidacionEditText.VALIDO);
                    }
                }
            }
        });

        binding.editextUsuario.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String usuario = binding.editextUsuario.getText().toString().trim();
                    if (!usuario.isEmpty()) {
                        executorService.execute(() -> {
                            try {
                                LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                                filtros.put("u.USUARIO", "= '" + usuario + "'");
                                int usuariosExistentes = cliente.leerUsuarios(filtros, null).size();
                                mainHandler.post(() -> {
                                    if (usuariosExistentes > 0) {
                                        validarEditext(binding.editextUsuario, EnumValidacionEditText.EN_USO);
                                    }
                                    else {
                                        validarEditext(binding.editextUsuario, EnumValidacionEditText.VALIDO);
                                    }
                                });
                            } catch (ExcepcionAlbumFamiliar e) {
                                mainHandler.post(() -> {
                                    validarEditext(binding.editextUsuario, EnumValidacionEditText.ERROR);
                                });
                            }
                        });
                    }
                    else {
                        validarEditext(binding.editextUsuario, EnumValidacionEditText.VACIO);
                    }
                }
            }
        });

        binding.editextPrefijo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String prefijo = binding.editextPrefijo.getText().toString().trim();
                    if (!prefijo.isEmpty()) {
                        validarEditext(binding.editextPrefijo, EnumValidacionEditText.VALIDO);
                    } else {
                        validarEditext(binding.editextPrefijo, EnumValidacionEditText.VACIO);
                    }
                }
            }
        });

        binding.editextTelefono.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String numeroTelefono = binding.editextTelefono.getText().toString().trim();
                    String prefijoTelefono = binding.editextPrefijo.getText().toString().trim();
                    if (!numeroTelefono.isEmpty() && validezPrefijo) {
                        String telefonoProcesado = Utils.limpiarNumeroDeTelefono(prefijoTelefono + numeroTelefono);
                        if (Utils.validarNumeroDeTelefono("+" + telefonoProcesado)) {
                            executorService.execute(() -> {
                                try {
                                    LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                                    filtros.put("u.TELEFONO", "= '" + prefijoTelefono.length() + telefonoProcesado + "'");
                                    int usuariosExistentes = cliente.leerUsuarios(filtros, null).size();
                                    mainHandler.post(() -> {
                                        if (usuariosExistentes > 0) {
                                            validarEditext(binding.editextTelefono, EnumValidacionEditText.EN_USO);
                                        } else {
                                            validarEditext(binding.editextTelefono, EnumValidacionEditText.VALIDO);
                                        }
                                    });
                                } catch (ExcepcionAlbumFamiliar e) {
                                    mainHandler.post(() -> {
                                        validarEditext(binding.editextTelefono, EnumValidacionEditText.ERROR);
                                    });
                                }
                            });
                        }
                        else {
                            validarEditext(binding.editextTelefono, EnumValidacionEditText.FORMATO_NO_VALIDO);
                        }
                    } else {
                        validarEditext(binding.editextTelefono, EnumValidacionEditText.VACIO);
                    }
                }
            }
        });
    }

    private void validarEditext(EditText editext, EnumValidacionEditText valorValidacion) {
        int id = editext.getId();

        switch (valorValidacion) {
            case VALIDO:
                if (id == R.id.editextNombre) {
                    validezNombre = true;
                }
                else if (id == R.id.editextUsuario) {
                    validezUsuario = true;
                }
                else if (id == R.id.editextPrefijo) {
                    validezPrefijo = true;
                }
                else if (id == R.id.editextTelefono) {
                    validezTelefono = true;
                }
                else if (id == R.id.editextCorreoElectronico) {
                    validezCorreo = true;
                }
                editext.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.contorno_gris, activity.getTheme()));
                break;

            case EN_USO:
                if (id == R.id.editextNombre) {
                    validezNombre = false;
                }
                else if (id == R.id.editextUsuario) {
                    validezUsuario = false;
                    Toast.makeText(getContext(), "El nombre de usuario ya está en uso.", Toast.LENGTH_SHORT).show();
                }
                else if (id == R.id.editextPrefijo) {
                    validezPrefijo = false;
                }
                else if (id == R.id.editextTelefono) {
                    validezTelefono = false;
                    Toast.makeText(getContext(), "El número de teléfono ya está registrado.", Toast.LENGTH_SHORT).show();
                }
                else if (id == R.id.editextCorreoElectronico) {
                    validezCorreo = false;
                    Toast.makeText(getContext(), "El correo ya está en uso.", Toast.LENGTH_SHORT).show();
                }
                editext.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.contorno_naranja, activity.getTheme()));
                break;

            case ERROR:
                if (id == R.id.editextNombre) {
                    validezNombre = false;
                }
                else if (id == R.id.editextUsuario) {
                    validezUsuario = false;
                    Toast.makeText(getContext(), "Error al comprobar la validez del usuario.", Toast.LENGTH_SHORT).show();
                }
                else if (id == R.id.editextPrefijo) {
                    validezPrefijo = false;
                }
                else if (id == R.id.editextTelefono) {
                    validezTelefono = false;
                    Toast.makeText(getContext(), "Error al comprobar la validez del número de teléfono.", Toast.LENGTH_SHORT).show();
                }
                else if (id == R.id.editextCorreoElectronico) {
                    validezCorreo = false;
                    Toast.makeText(getContext(), "Error al comprobar la validez del correo electrónico .", Toast.LENGTH_SHORT).show();
                }
                editext.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.contorno_naranja, activity.getTheme()));
                break;

            case VACIO:
                if (id == R.id.editextNombre) {
                    validezNombre = false;
                    Toast.makeText(getContext(), "Ingrese su nombre.", Toast.LENGTH_SHORT).show();
                }
                else if (id == R.id.editextUsuario) {
                    validezUsuario = false;
                    Toast.makeText(getContext(), "Ingrese un usuario.", Toast.LENGTH_SHORT).show();
                }
                else if (id == R.id.editextPrefijo) {
                    validezPrefijo = false;
                    Toast.makeText(getContext(), "Ingrese un prefijo para el teléfono.", Toast.LENGTH_SHORT).show();
                }
                else if (id == R.id.editextTelefono) {
                    validezTelefono = false;
                    Toast.makeText(getContext(), "Ingrese su número de teléfono.", Toast.LENGTH_SHORT).show();
                }
                else if (id == R.id.editextCorreoElectronico) {
                    validezCorreo = false;
                    Toast.makeText(getContext(), "Ingrese su correo electrónico.", Toast.LENGTH_SHORT).show();
                }
                editext.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.contorno_naranja, activity.getTheme()));
                break;

            case FORMATO_NO_VALIDO:
                if (id == R.id.editextCorreoElectronico) {
                    validezCorreo = false;
                    Toast.makeText(getContext(), "El formato del correo es incorrecto.", Toast.LENGTH_SHORT).show();
                }
                else if (id == R.id.editextTelefono) {
                    validezTelefono = false;
                    Toast.makeText(getContext(), "El formato del teléfono es incorrecto.", Toast.LENGTH_SHORT).show();
                }
                editext.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.contorno_naranja, activity.getTheme()));
                break;
        }
    }

    public boolean validacionCompleta() {
        if (validezNombre && validezCorreo && validezUsuario && validezTelefono && validezPrefijo) {
            return true;
        }
        return false;
    }

    public String getEditextNombre() {
        return binding.editextNombre.getText().toString().trim();
    }

    public String getEditextUsuario() {
        return binding.editextUsuario.getText().toString().trim();
    }

    public String getEditextCorreo() {
        return binding.editextCorreoElectronico.getText().toString().trim();
    }

    public String getEditextTelefonoConPrefijo() {
        String numeroTelefono = binding.editextTelefono.getText().toString().trim();
        String prefijoTelefono = binding.editextPrefijo.getText().toString().trim();
        return Utils.limpiarNumeroDeTelefono(prefijoTelefono + numeroTelefono);
    }

    private boolean tecladoEscondido(View rootView) {
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        int alturaPantalla = rootView.getHeight();
        int alturaTeclado = alturaPantalla - r.bottom;

        // Determinar si el teclado está visible o no
        return alturaTeclado < alturaPantalla * 0.15; // Umbral arbitrario para detectar la visibilidad del teclado
    }

    private void eliminarFocusEditext() {
        // Quitar el enfoque de los campos de entrada
        binding.editextNombre.clearFocus();
        binding.editextUsuario.clearFocus();
        binding.editextCorreoElectronico.clearFocus();
        binding.editextPrefijo.clearFocus();
        binding.editextTelefono.clearFocus();
    }
}
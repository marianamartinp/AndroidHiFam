package com.mariana.androidhifam;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mariana.androidhifam.databinding.FragmentTabDetallesUsuarioBinding;

import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Usuario;
import utils.Utils;

public class TabDetallesUsuarioFragment extends Fragment implements View.OnClickListener, ModalFragment.CustomModalInterface, TextWatcher{

    private @NonNull FragmentTabDetallesUsuarioBinding binding;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private Usuario usuario;
    private Integer tokenUsuario;
    private boolean camposModificados;
    private NavController navController;
    private boolean validezNombre = true, validezUsuario = true, validezCorreo = true, validezTelefono = true, validezPrefijo = true;

    // Método de creación
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicialización de variables y objetos
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        cliente = new CCAlbumFamiliar();
    }

    // Método de creación de la vista
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicialización de vistas y obtención del token de usuario
        navController = NavHostFragment.findNavController(this);
        binding = FragmentTabDetallesUsuarioBinding.inflate(inflater, container, false);
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    // Método que se llama cuando la vista se crea completamente
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activarValidacion();
        // Configuración de la validación y carga de detalles del usuario
        final View rootView = activity.findViewById(R.id.rootViewUsuario);
        // Registrar un OnGlobalLayoutListener para detectar cambios en la visibilidad del teclado
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (tecladoEscondido(rootView)) {
                    eliminarFocusEditext();
                }
            }
        });
        binding.editextUsuario.addTextChangedListener(this);
        binding.editextCorreoElectronico.addTextChangedListener(this);
        binding.editextTelefono.addTextChangedListener(this);
        binding.editextFechaNacimiento.addTextChangedListener(this);
        binding.editextNombreCompleto.addTextChangedListener(this);
        binding.botonCambiarContrasenya.setOnClickListener(this);
        binding.botonEliminarUsuario.setOnClickListener(this);
        binding.botonModificarUsuario.setOnClickListener(this);
        // Carga de detalles del usuario
        cargarDetallesUsuario();
    }

    // Implementación de TextWatcher
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
       // No precisado
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        camposModificados = true;
    }
    @Override
    public void afterTextChanged(Editable s) {
        // No precisado
    }

    // Método para manejar clics en vistas
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonCambiarContrasenya) {
            navController.navigate(DetallesUsuarioFragmentDirections.actionDetallesUsuarioFragmentToModificarContrasenyaFragment());
        }
        else if (id == R.id.botonEliminarUsuario) {
            modalEliminarUsuario(tokenUsuario);
        }
        else if (id == R.id.botonModificarUsuario) {
            modificarUsuario();
        }
    }

    // Método para manejar clics en el diálogo modal
    @Override
    public void onPositiveClick(String idModal, Integer position, Integer id) {
        if (activity.getHabilitarInteraccion()) {
            // Realizar acciones según el ID del modal y la acción realizada
            switch (idModal) {
                case "eliminarUsuario":
                    if (null != usuario) {
                        eliminarUsuario(usuario);
                    }
                    break;
            }
        }
    }

    // Método para mostrar el modal de eliminación de usuario
    public void modalEliminarUsuario(int id) {
        ModalFragment modal = new ModalFragment("eliminarUsuario", null, id, this, "¿Desea eliminar su usuario?\nEsta acción será irreversible.", getString(R.string.btnEliminar), getString(R.string.btnCancelar));
        modal.show(activity.getSupportFragmentManager(), "modalEliminarUsuario");
    }

    // Método para cargar los detalles del usuario
    public void cargarDetallesUsuario() {
        executorService.execute(() -> {
            try {
                usuario = cliente.leerUsuario(tokenUsuario);
                mainHandler.post(() -> {
                    if (null != usuario) {
                        actualizarInterfaz(usuario);
                    }
                });
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(getContext(), "Se ha producido un error al cargar la información.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Método para actualizar la interfaz con los detalles del usuario
    public void actualizarInterfaz(Usuario usuario) {
        binding.editextUsuario.setText(usuario.getUsuario());
        binding.editextFechaNacimiento.setText(Utils.parsearDateAString(usuario.getFechaNacimiento()));
        int longitudPrefijo = Integer.parseInt(usuario.getTelefono().substring(0,1)) + 1;
        binding.editextPrefijo.setText(usuario.getTelefono().substring(1,longitudPrefijo));
        binding.editextTelefono.setText(usuario.getTelefono().substring(longitudPrefijo));
        binding.editextNombreCompleto.setText(usuario.getNombre());
        binding.editextCorreoElectronico.setText(usuario.getEmail());
    }

    // Método para eliminar un usuario
    public void eliminarUsuario(Usuario usuario) {
        executorService.execute(() -> {
            try {
                cliente.eliminarUsuario(usuario.getCodUsuario());
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Usuario eliminado.", Toast.LENGTH_SHORT).show();
                    activity.cerrarSesion();
                });
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(requireContext(), "Error al eliminar la publicación.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Método para modificar un usuario
    public void modificarUsuario() {
        String nombre = binding.editextNombreCompleto.getText().toString().trim();
        String nombreUsuario = binding.editextUsuario.getText().toString().trim();
        String prefijo = binding.editextPrefijo.getText().toString().trim();
        String telefono = binding.editextTelefono.getText().toString().trim();
        String correo = binding.editextCorreoElectronico.getText().toString().trim();
        String fechaNacimiento = binding.editextFechaNacimiento.getText().toString().trim();
        if (camposModificados && validacionCompleta()) {

            executorService.execute(() -> {
                try {
                    Usuario nuevoUsuario = new Usuario(null, nombre, nombreUsuario, correo, prefijo.length() + prefijo + telefono, usuario.getContrasenya(), Utils.parsearFechaADate(fechaNacimiento), null);
                    int resultadoInsercion = cliente.modificarUsuario(usuario.getCodUsuario(), nuevoUsuario);
                    mainHandler.post(() -> {
                        if (resultadoInsercion > 0) {
                            Toast.makeText(requireContext(), "Tu usuario se ha modificado correctamente.", Toast.LENGTH_SHORT).show();
                            navController.popBackStack();
                        }
                    });
                } catch (ExcepcionAlbumFamiliar e) {
                    manejadorExcepcionAlbumFamiliar(e);
                }
            });
        }
        else if (!camposModificados && validacionCompleta()){
            Toast.makeText(requireContext(), "Tu usuario se ha modificado correctamente.", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        }
        else {
            Toast.makeText(getContext(), "Completa la información para continuar.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para activar la validación de los campos de entrada
    public void activarValidacion() {
        // Validación del correo electrónico
        binding.editextCorreoElectronico.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String email = binding.editextCorreoElectronico.getText().toString().trim();
                    if (!email.isEmpty()) {
                        if (Utils.esEmailValido(email)) {
                            executorService.execute(() -> {
                                try {
                                    LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                                    filtros.put("u.EMAIL", "= '" + email + "'");
                                    int usuariosExistentes = cliente.leerUsuarios(filtros, null).size();
                                    mainHandler.post(() -> {
                                        if (usuariosExistentes > 0 && !email.equals(usuario.getEmail())) {
                                            validarEditext(binding.editextCorreoElectronico, Utils.EnumValidacionEditText.EN_USO);
                                        } else {
                                            validarEditext(binding.editextCorreoElectronico, Utils.EnumValidacionEditText.VALIDO);
                                        }
                                    });
                                }
                                catch (ExcepcionAlbumFamiliar e) {
                                    mainHandler.post(() -> {
                                        validarEditext(binding.editextCorreoElectronico, Utils.EnumValidacionEditText.ERROR);
                                    });
                                }
                            });
                        }
                        else {
                            validarEditext(binding.editextCorreoElectronico, Utils.EnumValidacionEditText.FORMATO_NO_VALIDO);
                        }
                    }
                    else {
                        validarEditext(binding.editextCorreoElectronico, Utils.EnumValidacionEditText.VACIO);
                    }
                }
            }
        });

        // Validación del nombre
        binding.editextNombreCompleto.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String nombre = binding.editextNombreCompleto.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        validarEditext(binding.editextNombreCompleto, Utils.EnumValidacionEditText.VACIO);
                    }
                    else {
                        validarEditext(binding.editextNombreCompleto, Utils.EnumValidacionEditText.VALIDO);
                    }
                }
            }
        });

        // Validación del nombre de usuario
        binding.editextUsuario.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String usuarioEditext = binding.editextUsuario.getText().toString().trim();
                    if (!usuarioEditext.isEmpty()) {
                        executorService.execute(() -> {
                            try {
                                LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                                filtros.put("u.USUARIO", "= '" + usuarioEditext + "'");
                                int usuariosExistentes = cliente.leerUsuarios(filtros, null).size();
                                mainHandler.post(() -> {
                                    if (usuariosExistentes > 0 && !usuarioEditext.equals(usuario.getUsuario())) {
                                        validarEditext(binding.editextUsuario, Utils.EnumValidacionEditText.EN_USO);
                                    }
                                    else {
                                        validarEditext(binding.editextUsuario, Utils.EnumValidacionEditText.VALIDO);
                                    }
                                });
                            } catch (ExcepcionAlbumFamiliar e) {
                                mainHandler.post(() -> {
                                    validarEditext(binding.editextUsuario, Utils.EnumValidacionEditText.ERROR);
                                });
                            }
                        });
                    }
                    else {
                        validarEditext(binding.editextUsuario, Utils.EnumValidacionEditText.VACIO);
                    }
                }
            }
        });

        // Validación del prefijo telefónico
        binding.editextPrefijo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String prefijo = binding.editextPrefijo.getText().toString().trim();
                    if (!prefijo.isEmpty()) {
                        validarEditext(binding.editextPrefijo, Utils.EnumValidacionEditText.VALIDO);
                    } else {
                        validarEditext(binding.editextPrefijo, Utils.EnumValidacionEditText.VACIO);
                    }
                }
            }
        });

        // Validación del teléfono considerando el prefijo
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
                                        if (usuariosExistentes > 0 && !(prefijoTelefono.length() + telefonoProcesado).equals(usuario.getTelefono())) {
                                            validarEditext(binding.editextTelefono, Utils.EnumValidacionEditText.EN_USO);
                                        } else {
                                            validarEditext(binding.editextTelefono, Utils.EnumValidacionEditText.VALIDO);
                                        }
                                    });
                                } catch (ExcepcionAlbumFamiliar e) {
                                    mainHandler.post(() -> {
                                        validarEditext(binding.editextTelefono, Utils.EnumValidacionEditText.ERROR);
                                    });
                                }
                            });
                        }
                        else {
                            validarEditext(binding.editextTelefono, Utils.EnumValidacionEditText.FORMATO_NO_VALIDO);
                        }
                    } else {
                        validarEditext(binding.editextTelefono, Utils.EnumValidacionEditText.VACIO);
                    }
                }
            }
        });
    }

    // Método para validar un campo de entrada y mostrar retroalimentación visual
    private void validarEditext(EditText editext, Utils.EnumValidacionEditText valorValidacion) {
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

    // Método para verificar si el teclado virtual está oculto o visible
    private boolean tecladoEscondido(View rootView) {
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        int alturaPantalla = rootView.getHeight();
        int alturaTeclado = alturaPantalla - r.bottom;

        // Determinar si el teclado está visible o no
        return alturaTeclado < alturaPantalla * 0.15; // Umbral arbitrario para detectar la visibilidad del teclado
    }

    // Método para eliminar el foco de los campos de entrada
    private void eliminarFocusEditext() {
        // Quitar el enfoque de los campos de entrada
        binding.editextNombreCompleto.clearFocus();
        binding.editextUsuario.clearFocus();
        binding.editextCorreoElectronico.clearFocus();
        binding.editextPrefijo.clearFocus();
        binding.editextTelefono.clearFocus();
    }

    // Método para verificar si la validación de todos los campos es completa
    public boolean validacionCompleta() {
        if (validezNombre && validezCorreo && validezUsuario && validezTelefono && validezPrefijo) {
            return true;
        }
        return false;
    }

    // Método para manejar excepciones de la clase ExcepcionAlbumFamiliar
    public void manejadorExcepcionAlbumFamiliar(ExcepcionAlbumFamiliar e) {
        String mensaje;
        mensaje = e.getMensajeUsuario();
        mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
    }
}
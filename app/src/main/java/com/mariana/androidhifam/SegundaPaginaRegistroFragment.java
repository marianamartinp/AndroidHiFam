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
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.mariana.androidhifam.databinding.FragmentSegundaPaginaRegistroBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import utils.Utils;

public class SegundaPaginaRegistroFragment extends Fragment implements  View.OnClickListener {

    private @NonNull FragmentSegundaPaginaRegistroBinding binding;
    private MainActivity activity;
    private boolean validezFechaNacimiento, validezContrasenya, mostrarContrasenya;
    // Enumeración para la validación de EditText
    private enum EnumValidacionEditText {
        VALIDO, FORMATO_NO_VALIDO, VACIO
    }

    // Método para crear la vista del fragmento
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    // Método ejecutado tras crear la vista del fragmento
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Devolver la vista raíz del fragmento
        binding = FragmentSegundaPaginaRegistroBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // Método que se llama después de que la vista se haya creado completamente
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.botonCalendario.setOnClickListener(this);
        binding.botonMostrarContrasenya.setOnClickListener(this);

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

        // Configurar el listener para el EditText de la contraseña
        binding.editextContrasenya.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String contrasenya = binding.editextContrasenya.getText().toString().trim();
                    if (!contrasenya.isEmpty()) {
                        if (Utils.validarContrasenya(contrasenya)) {
                            validarEditext(binding.editextContrasenya, EnumValidacionEditText.VALIDO);
                        }
                        else {
                            validarEditext(binding.editextContrasenya, EnumValidacionEditText.FORMATO_NO_VALIDO);
                        }
                    } else {
                        validarEditext(binding.editextContrasenya, SegundaPaginaRegistroFragment.EnumValidacionEditText.VACIO);
                    }
                }
            }
        });
    }

    // Método que maneja los clics en las vistas
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonCalendario) {
            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .build();

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setCalendarConstraints(constraints)
                    .setTheme(R.style.DatePickerDialogTheme)
                    .setTitleText("Selecciona tu fecha de nacimiento")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(seleccionFecha -> {
                String formattedDate = formatearFecha(seleccionFecha);
                binding.editextFechaNacimiento.setText(formattedDate);
                validezFechaNacimiento = true;
            });

            datePicker.show(getParentFragmentManager(), "Selector fecha de nacimiento");
        }
        else if (id == R.id.botonMostrarContrasenya) {
            mostrarContrasenya();
        }
    }

    // Método para formatear la fecha seleccionada
    private String formatearFecha(long milisegundos) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date fecha = new Date(milisegundos);
        return sdf.format(fecha);
    }

    // Método para mostrar las validaciones de la interfaz gráfica
    private void validarEditext(EditText editext, EnumValidacionEditText valorValidacion) {
        int id = editext.getId();

        switch (valorValidacion) {
            case VALIDO:
                if (id == R.id.editextContrasenya) {
                    validezContrasenya = true;
                    binding.requisitosContrasenya.setVisibility(View.INVISIBLE);
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
                    binding.requisitosContrasenya.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "La contraseña no es segura.", Toast.LENGTH_SHORT).show();
                }
                editext.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.contorno_naranja, activity.getTheme()));
                break;
        }
    }

    // Método para verificar si la validación está completa
    public boolean validacionCompleta() {
        if (validezFechaNacimiento && validezContrasenya) {
            return true;
        }
        return false;
    }

    // Getters de los datos del fragment para el fragment padre
    public String getEditextFechaNacimiento() {
        return binding.editextFechaNacimiento.getText().toString().trim();
    }

    public String getEditextContrasenya() {
        return binding.editextContrasenya.getText().toString().trim();
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

    // Método para mostrar u ocultar la contraseña
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
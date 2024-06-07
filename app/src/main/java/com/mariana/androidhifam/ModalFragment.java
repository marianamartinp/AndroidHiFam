package com.mariana.androidhifam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.InputFilter;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentIngresoGrupoBinding;
import com.mariana.androidhifam.databinding.FragmentModalBinding;

import java.util.concurrent.atomic.AtomicReference;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.SolicitudEntradaGrupo;
import pojosalbumfamiliar.Usuario;

public class ModalFragment extends DialogFragment implements DialogInterface.OnDismissListener {
    private @NonNull FragmentModalBinding binding;
    private MainActivity activity;
    private CustomModalInterface customModalInterface;
    private String textoModal, botonPositivo, botonNegativo, idModal;
    private Integer position, id;

    // Interfaz personalizada para manejar clics en los botones del modal
    public interface CustomModalInterface {
        void onPositiveClick(String idModal, Integer position, Integer id);
    }

    // Constructor de la clase
    public ModalFragment(String idModal, Integer position, Integer id, CustomModalInterface customModalInterface, String textoModal, String botonPositivo, String botonNegativo) {
        this.position = position;
        this.id = id;
        this.textoModal = textoModal;
        this.botonPositivo = botonPositivo;
        this.botonNegativo = botonNegativo;
        this.customModalInterface = customModalInterface;
        this.idModal = idModal;
    }

    // Método llamado para crear el diálogo
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = FragmentModalBinding.inflate(getLayoutInflater());
        activity = (MainActivity) getActivity();
        // Crear un constructor de diálogos
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(binding.getRoot());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 10);

        // Añadir el botón negativo si existe
        if (null != botonNegativo) {
            anyadirBotonNegativo(params);
        }
        // Añadir el botón positivo si existe
        if (null != botonPositivo) {
            anyadirBotonPositivo(params);
        }

        binding.textViewModal.setText(textoModal);
        return builder.create();
    }

    // Método llamado cuando la vista del fragmento está a punto de ser destruida
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Método llamado cuando el fragmento está a punto de ser visible para el usuario
    @Override
    public void onResume() {
        super.onResume();
        // Establecer el ancho del diálogo
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        int margin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        params.width = screenWidth - (2 * margin);
        window.setAttributes(params);
    }

    // Método para añadir un botón negativo al diálogo
    public void anyadirBotonNegativo(LinearLayout.LayoutParams params) {
        Button negativeButton = (Button) getLayoutInflater().inflate(R.layout.boton_modal, null);
        negativeButton.setText(botonNegativo);
        negativeButton.setLayoutParams(params);
        TextViewCompat.setTextAppearance(negativeButton, R.style.CustomButtonStyle);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        binding.contenedorBotones.addView(negativeButton);
    }

    // Método para añadir un botón positivo al diálogo
    public void anyadirBotonPositivo(LinearLayout.LayoutParams params) {
        Button positiveButton = (Button) getLayoutInflater().inflate(R.layout.boton_modal, null);
        positiveButton.setText(botonPositivo);
        positiveButton.setLayoutParams(params);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != customModalInterface) {
                    customModalInterface.onPositiveClick(idModal, position, id);
                }
                dismiss();
            }
        });
        binding.contenedorBotones.addView(positiveButton);
    }

}
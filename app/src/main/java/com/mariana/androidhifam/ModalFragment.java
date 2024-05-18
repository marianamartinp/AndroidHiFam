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
    private NavController navController;
    private FragmentModalBinding binding;
    private CCAlbumFamiliar cliente;
    private MainActivity activity;
    private CustomModalInterface customModalInterface;
    private String textoModal, botonPositivo, botonNegativo;

    public ModalFragment(CustomModalInterface customModalInterface, String textoModal, String botonPositivo, String botonNegativo) {
        this.textoModal = textoModal;
        this.botonPositivo = botonPositivo;
        this.botonNegativo = botonNegativo;
        this.customModalInterface = customModalInterface;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cliente = new CCAlbumFamiliar();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = FragmentModalBinding.inflate(getLayoutInflater());
        activity = (MainActivity) getActivity();
        navController = NavHostFragment.findNavController(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(binding.getRoot());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 10);

        if (null != botonNegativo) {
            Button negativeButton = (Button) getLayoutInflater().inflate(R.layout.boton_modal, null);
            negativeButton.setText(botonNegativo);
            negativeButton.setLayoutParams(params);
            TextViewCompat.setTextAppearance(negativeButton, R.style.CustomButtonStyle);
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != customModalInterface) {
                        customModalInterface.onNegativeClick();
                    }
                    dismiss();
                }
            });
            binding.contenedorBotones.addView(negativeButton);
        }

        if (null != botonPositivo) {
            Button positiveButton = (Button) getLayoutInflater().inflate(R.layout.boton_modal, null);
            positiveButton.setText(botonPositivo);
            positiveButton.setLayoutParams(params);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != customModalInterface) {
                        customModalInterface.onPositiveClick();
                    }
                    dismiss();
                }
            });
            binding.contenedorBotones.addView(positiveButton);
        }

        binding.textViewModal.setText(textoModal);
        // Add buttons to the button container

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public interface CustomModalInterface {
        void onPositiveClick();
        void onNegativeClick();
    }

}
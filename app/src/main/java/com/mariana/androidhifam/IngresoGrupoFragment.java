package com.mariana.androidhifam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentIngresoGrupoBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.SolicitudEntradaGrupo;
import pojosalbumfamiliar.Usuario;

public class IngresoGrupoFragment extends DialogFragment implements View.OnClickListener, DialogInterface.OnDismissListener {

    private FragmentIngresoGrupoBinding binding;
    private NavController navController;
    private CCAlbumFamiliar cliente;
    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = FragmentIngresoGrupoBinding.inflate(getLayoutInflater());
        cliente = activity.getCliente();
        navController = NavHostFragment.findNavController(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(binding.getRoot());
        binding.editableCodigo.setFilters(new InputFilter[] {new FiltroNumerico()});
        binding.botonEnviarSolicitud.setOnClickListener(this);
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonEnviarSolicitud) {
            solicitarEntradaEnGrupo();
            dismiss();
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void solicitarEntradaEnGrupo() {
        AtomicReference<Integer> resultado = new AtomicReference<>();
        Thread tarea = new Thread(() -> {
            try {
                Usuario usuario = new Usuario();
                usuario.setCodUsuario(activity.getIdUsuario());
                Grupo grupo = new Grupo();
                grupo.setCodGrupo(Integer.parseInt(binding.editableCodigo.getText().toString().trim()));
                resultado.set(cliente.insertarSolicitudEntradaGrupo(new SolicitudEntradaGrupo(grupo,usuario,null)));
            } catch (ExcepcionAlbumFamiliar e) {
                // Error
            }
        });
        tarea.start();
        try {
            tarea.join(5000);
        } catch (InterruptedException e) {
            // Error
        }
        if (null != resultado.get() && resultado.get() > 0) {
            Toast.makeText(requireContext(), "Se ha enviado tu solicitud.", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(requireContext(), "Error.", Toast.LENGTH_SHORT).show();
        }
    }

}
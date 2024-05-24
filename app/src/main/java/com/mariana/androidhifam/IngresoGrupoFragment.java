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

import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private ExecutorService executorService;
    private Handler mainHandler;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
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
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void solicitarEntradaEnGrupo() {
        executorService.execute(() -> {
            try {
                Usuario usuario = new Usuario();
                usuario.setCodUsuario(activity.getIdUsuario());
                Grupo grupo = new Grupo();
                grupo.setCodGrupo(Integer.parseInt(binding.editableCodigo.getText().toString().trim()));
                cliente.insertarSolicitudEntradaGrupo(new SolicitudEntradaGrupo(grupo,usuario,null));
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Se ha enviado tu solicitud.", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
            catch (ExcepcionAlbumFamiliar e) {
                String mensaje;
                switch (e.getCodErrorBd()) {
                    case 1400:
                    case 20008:
                    case 2291:
                        mensaje = "Introduce un código de grupo válido.";
                        break;
                    case 1:
                        mensaje = "No se pueden realizar varias solicitudes a un mismo grupo";
                        break;
                    default:
                        mensaje = e.getMensajeUsuario();
                        break;
                }
                mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
            }
        });
    }

}
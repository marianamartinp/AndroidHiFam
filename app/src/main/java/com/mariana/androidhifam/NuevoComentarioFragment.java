package com.mariana.androidhifam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mariana.androidhifam.databinding.FragmentIngresoGrupoBinding;
import com.mariana.androidhifam.databinding.FragmentNuevoComentarioBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Comentario;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.Publicacion;
import pojosalbumfamiliar.SolicitudEntradaGrupo;
import pojosalbumfamiliar.Usuario;

public class NuevoComentarioFragment extends DialogFragment implements View.OnClickListener, DialogInterface.OnDismissListener {

    private NuevoComentarioFragmentArgs nuevoComentarioFragmentArgs;
    private FragmentNuevoComentarioBinding binding;
    private NavController navController;
    private CCAlbumFamiliar cliente;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private Integer tokenUsuario, idPublicacion;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nuevoComentarioFragmentArgs = NuevoComentarioFragmentArgs.fromBundle(getArguments());
            idPublicacion = nuevoComentarioFragmentArgs.getIdPublicacion();
        }
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = FragmentNuevoComentarioBinding.inflate(getLayoutInflater());
        cliente = activity.getCliente();
        tokenUsuario = Integer.parseInt(activity.getToken());
        navController = NavHostFragment.findNavController(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(binding.getRoot());
        binding.botonEnviarComentario.setOnClickListener(this);
        binding.botonAtras.setOnClickListener(this);
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
        if (id == R.id.botonEnviarComentario) {
            solicitarEntradaEnGrupo();
        }
        else if (id == R.id.botonAtras) {
            dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        navController.popBackStack();
    }

    public void solicitarEntradaEnGrupo() {
        executorService.execute(() -> {
            try {
                Usuario usuario = new Usuario();
                usuario.setCodUsuario(tokenUsuario);
                Comentario comentario = new Comentario();
                Publicacion publicacion = new Publicacion();
                publicacion.setCodPublicacion(idPublicacion);
                comentario.setPublicacionTieneComentario(publicacion);
                comentario.setUsuarioCreaComentario(usuario);
                comentario.setTexto(binding.editableTexto.getText().toString().trim());

                cliente.insertarComentario(comentario);
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Se ha enviado tu comentario.", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
            catch (ExcepcionAlbumFamiliar e) {
                String mensaje = e.getMensajeUsuario();
                mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
            }
        });
    }

}
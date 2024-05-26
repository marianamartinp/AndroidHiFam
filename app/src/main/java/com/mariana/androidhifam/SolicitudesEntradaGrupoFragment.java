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
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentIngresoGrupoBinding;
import com.mariana.androidhifam.databinding.FragmentSolicitudesEntradaGrupoBinding;

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
import pojosalbumfamiliar.UsuarioIntegraGrupo;

public class SolicitudesEntradaGrupoFragment extends DialogFragment implements View.OnClickListener, DialogInterface.OnDismissListener, ListAdapter.OnItemClickListener {

    private FragmentSolicitudesEntradaGrupoBinding binding;
    private SolicitudesEntradaGrupoFragmentArgs solicitudesEntradaGrupoFragmentArgs;
    private NavController navController;
    private ListAdapter<SolicitudEntradaGrupo> adapter;
    private ServicioSolicitudEntradaGrupo servicioSolicitudEntradaGrupo;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private ArrayList<SolicitudEntradaGrupo> solicitudes;
    private Integer idGrupo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            solicitudesEntradaGrupoFragmentArgs = SolicitudesEntradaGrupoFragmentArgs.fromBundle(getArguments());
            idGrupo = solicitudesEntradaGrupoFragmentArgs.getIdGrupo();
        }
        activity = (MainActivity) getActivity();
        solicitudes = new ArrayList<>();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        servicioSolicitudEntradaGrupo = new ServicioSolicitudEntradaGrupo();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = FragmentSolicitudesEntradaGrupoBinding.inflate(getLayoutInflater());
        navController = NavHostFragment.findNavController(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(binding.getRoot());
        binding.botonAtras.setOnClickListener(this);
        cargarSolicitudesEntradaGrupo(idGrupo);
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
        if (id == R.id.botonAtras) {
            dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        int marginAncho = getResources().getDimensionPixelSize(R.dimen.dialog_solicitudes_width_margin);
        int marginAlto = getResources().getDimensionPixelSize(R.dimen.dialog_solicitudes_heigth_margin);
        int anchoPantalla = getResources().getDisplayMetrics().widthPixels;
        int altoPantalla = getResources().getDisplayMetrics().heightPixels;
        params.width = anchoPantalla - (2 * marginAncho);
        params.height = altoPantalla - (2 * marginAlto);
        window.setAttributes(params);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onItemClick(Object item, int position, int idButton) {
        if (idButton == R.id.iconoEquis) {
            rechazarSolicitudUsuario(item, position);
        }
        else if (idButton == R.id.iconoTick) {
            aceptarSolicitudUsuario(item, position);
        }
    }

    public void cargarSolicitudesEntradaGrupo(int idGrupo) {
        executorService.execute(() -> {
            try {
                solicitudes = servicioSolicitudEntradaGrupo.leerSolicitudesEntradaGrupo(idGrupo);
                mainHandler.post(() -> {
                    adapter = new ListAdapter<>(requireContext(), solicitudes, ItemsListAdapter.ITEM_SOLICITUD_GRUPO, this);
                    binding.miembrosGrupo.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.miembrosGrupo.setAdapter(adapter);
                    mostrarTextoAlternativo();
                });
            }
            catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    public void aceptarSolicitudUsuario(Object item, int position) {
        SolicitudEntradaGrupo seg = (SolicitudEntradaGrupo) item;
        executorService.execute(() -> {
            try {
                servicioSolicitudEntradaGrupo.aceptarSolicitudEntradaGrupo(seg);
                mainHandler.post(() -> {
                    actualizarGrid(position);
                });
            } catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    public void rechazarSolicitudUsuario(Object item, int position) {
        SolicitudEntradaGrupo seg = (SolicitudEntradaGrupo) item;
        executorService.execute(() -> {
            try {
                servicioSolicitudEntradaGrupo.rechazarSolicitudEntradaGrupo(seg);
                mainHandler.post(() -> {
                    actualizarGrid(position);
                });
            } catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    public void actualizarGrid(int position) {
        solicitudes.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, solicitudes.size() - position);
        mostrarTextoAlternativo();
    }

    public void mostrarTextoAlternativo() {
        if (solicitudes.isEmpty()) {
            new Handler().postDelayed(() -> {
                binding.textoAlternativo.setVisibility(View.VISIBLE);
            }, 200);
        }
        else {
            binding.textoAlternativo.setVisibility(View.INVISIBLE);
        }
    }

    public void manejadorExcepcionAlbumFamiliar(ExcepcionAlbumFamiliar e) {
        String mensaje;
        mensaje = e.getMensajeUsuario();
        mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
    }

}
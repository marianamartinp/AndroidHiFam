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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mariana.androidhifam.databinding.FragmentSolicitudesEntradaGrupoBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.SolicitudEntradaGrupo;
import pojosalbumfamiliar.UsuarioIntegraGrupo;
import utils.ItemsListAdapter;
import utils.ListAdapter;

public class SolicitudesEntradaGrupoFragment extends DialogFragment implements View.OnClickListener, DialogInterface.OnDismissListener, ListAdapter.OnItemClickListener {

    private @NonNull FragmentSolicitudesEntradaGrupoBinding binding;
    private @NonNull SolicitudesEntradaGrupoFragmentArgs solicitudesEntradaGrupoFragmentArgs;
    private ListAdapter<SolicitudEntradaGrupo> adapter;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private ArrayList<SolicitudEntradaGrupo> solicitudes;
    private Integer idGrupo;
    private CCAlbumFamiliar cliente;


    // Método de creación
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
    }

    // Método de creación del diálogo
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        cliente = activity.getCliente();
        binding = FragmentSolicitudesEntradaGrupoBinding.inflate(getLayoutInflater());
        // Creación del diálogo y configuración de vistas
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(binding.getRoot());
        binding.botonAtras.setOnClickListener(this);
        // Método para cargar las solicitudes de entrada a un grupo
        cargarSolicitudesEntradaGrupo(idGrupo);
        return builder.create();
    }

    // Método para realizar acciones cuando se destruye la vista
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Método para manejar clics en vistas
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonAtras) {
            dismiss();
        }
    }

    // Método para realizar acciones al reanudar el diálogo
    @Override
    public void onResume() {
        super.onResume();
        // Configuración del tamaño del diálogo
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

    // Método para manejar el evento de cierre del diálogo
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    // Método para manejar el clic en elementos de la lista
    @Override
    public void onItemClick(Object item, int position, int idButton) {
        if (activity.getHabilitarInteraccion()) {
            if (idButton == R.id.iconoEquis) {
                rechazarSolicitudUsuario((SolicitudEntradaGrupo) item, position);
            } else if (idButton == R.id.iconoTick) {
                aceptarSolicitudUsuario((SolicitudEntradaGrupo) item, position);
            }
        }
    }

    // Método para cargar las solicitudes de entrada a un grupo
    public void cargarSolicitudesEntradaGrupo(int idGrupo) {
        executorService.execute(() -> {
            try {
                LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                filtros.put("g.COD_GRUPO", "=" + idGrupo);
                LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
                ordenacion.put("seg.FECHA_SOLICITUD", "asc");
                solicitudes = cliente.leerSolicitudesEntradaGrupo(filtros, ordenacion);
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

    // Método para aceptar solicitudes de entrada
    public void aceptarSolicitudUsuario(SolicitudEntradaGrupo item, int position) {
        SolicitudEntradaGrupo seg = item;
        executorService.execute(() -> {
            try {
                cliente.insertarUsuarioIntegraGrupo(new UsuarioIntegraGrupo(seg.getUsuario(), seg.getGrupo()));
                cliente.eliminarSolicitudEntradaGrupo(seg.getUsuario().getCodUsuario(), seg.getGrupo().getCodGrupo());
                mainHandler.post(() -> {
                    actualizarGrid(position);
                });
            } catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    // Método para rechazar solicitudes de entrada
    public void rechazarSolicitudUsuario(SolicitudEntradaGrupo item, int position) {
        executorService.execute(() -> {
            try {
                cliente.eliminarSolicitudEntradaGrupo(item.getUsuario().getCodUsuario(), item.getGrupo().getCodGrupo());
                mainHandler.post(() -> {
                    actualizarGrid(position);
                });
            } catch (ExcepcionAlbumFamiliar e) {
                manejadorExcepcionAlbumFamiliar(e);
            }
        });
    }

    // Métodos para aceptar y rechazar solicitudes de entrada
    public void actualizarGrid(int position) {
        // Actualización de la lista
        solicitudes.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, solicitudes.size() - position);
        mostrarTextoAlternativo();
    }

    // Método para mostrar un mensaje cuando no hay solicitudes
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

    // Método para manejar excepciones relacionadas con el álbum familiar
    public void manejadorExcepcionAlbumFamiliar(ExcepcionAlbumFamiliar e) {
        String mensaje;
        mensaje = e.getMensajeUsuario();
        mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
    }

}
package com.mariana.androidhifam;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentDetallesGrupoBinding;
import com.mariana.androidhifam.databinding.FragmentTabDetallesGrupoBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.Usuario;
import pojosalbumfamiliar.UsuarioIntegraGrupo;

public class TabDetallesGrupoFragment extends Fragment implements TextWatcher {

    private FragmentTabDetallesGrupoBinding binding;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private CCAlbumFamiliar cliente;
    private DetallesGrupoFragment parentFragment;
    private Grupo grupo;
    private Integer tokenUsuario, idGrupo;
    private boolean camposModificados;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        cliente = new CCAlbumFamiliar();
        parentFragment = (DetallesGrupoFragment) this.getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTabDetallesGrupoBinding.inflate(inflater, container, false);
        tokenUsuario = Integer.parseInt(activity.getToken());
        idGrupo = parentFragment.getGrupoId();
        binding.editextDescripcionFamilia.addTextChangedListener(this);
        binding.editextTituloFamilia.addTextChangedListener(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarDetallesGrupo();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        camposModificados = true;
    }
    @Override
    public void afterTextChanged(Editable s) {

    }

    public void cargarDetallesGrupo() {
        executorService.execute(() -> {
            try {
                grupo = cliente.leerGrupo(idGrupo);
                mainHandler.post(() -> {
                    if (null != grupo) {
                        revisarPermisos(tokenUsuario, grupo);
                        actualizarInterfaz(grupo);
                    }
                });
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(getContext(), "Se ha producido un error al cargar la información.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void actualizarInterfaz(Grupo grupo) {
        binding.editextCodigoGrupo.setText(grupo.getCodGrupo().toString());
        binding.editextFechaCreacion.setText(Utils.parsearDateAString(grupo.getFechaCreacion()));
        binding.editextAdminGrupo.setText("@" + grupo.getUsuarioAdminGrupo().getUsuario());
        binding.editextTituloFamilia.setText(grupo.getTitulo());
        binding.editextDescripcionFamilia.setText(grupo.getDescripcion());
    }

    public void revisarPermisos(Integer tokenUsuario, Grupo grupo) {
        if (Objects.equals(tokenUsuario, grupo.getUsuarioAdminGrupo().getCodUsuario())) {
            binding.editextTituloFamilia.setEnabled(true);
            binding.editextTituloFamilia.setInputType(InputType.TYPE_CLASS_TEXT);
            binding.editextTituloFamilia.setTextColor(getResources().getColor(R.color.darkGrey, activity.getTheme()));
            binding.editextDescripcionFamilia.setEnabled(true);
            binding.editextDescripcionFamilia.setInputType(InputType.TYPE_CLASS_TEXT);
            binding.editextDescripcionFamilia.setTextColor(getResources().getColor(R.color.darkGrey, activity.getTheme()));
        }
        else {
            binding.editextTituloFamilia.setEnabled(false);
            binding.editextTituloFamilia.setInputType(InputType.TYPE_NULL);
            binding.editextTituloFamilia.setTextColor(getResources().getColor(R.color.mediumGrey, activity.getTheme()));
            binding.editextDescripcionFamilia.setEnabled(false);
            binding.editextDescripcionFamilia.setInputType(InputType.TYPE_NULL);
            binding.editextDescripcionFamilia.setTextColor(getResources().getColor(R.color.mediumGrey, activity.getTheme()));
        }
    }

    public String getEditextTituloFamilia() {
        return binding.editextTituloFamilia.getText().toString().trim();
    }

    public String getEditextDescripcionFamilia() {
        return binding.editextDescripcionFamilia.getText().toString().trim();
    }

    public Date getFechaCreacion() {
        return grupo.getFechaCreacion();
    }

    public boolean getCamposModificados() {
        return camposModificados;
    }
}
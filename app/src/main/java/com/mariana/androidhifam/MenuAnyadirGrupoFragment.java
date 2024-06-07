package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mariana.androidhifam.R.id.*;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mariana.androidhifam.databinding.FragmentMenuAnyadirGrupoBinding;
import com.mariana.androidhifam.databinding.FragmentPublicacionesBinding;

public class MenuAnyadirGrupoFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    private @NonNull FragmentMenuAnyadirGrupoBinding binding;
    private NavController navController;

    // Método llamado para crear y devolver la vista asociada con el fragmento
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        navController = NavHostFragment.findNavController(this);
        // Inflar y vincular el diseño del fragmento utilizando la clase de enlace generada
        binding = FragmentMenuAnyadirGrupoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // Método llamado después de que la vista del fragmento haya sido creada
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Configurar los listeners de clics para los botones del fragmento
        binding.botonGrupoExistente.setOnClickListener(this);
        binding.botonNuevoGrupo.setOnClickListener(this);
        binding.botonAtras.setOnClickListener(this);
    }

    // Método para manejar los clics en los elementos del fragmento
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonGrupoExistente) {
            navController.navigate(MenuAnyadirGrupoFragmentDirections.actionMenuAnyadirGrupoFragmentToIngresoGrupoFragment());
        }
        else if (id == R.id.botonNuevoGrupo) {
            navController.navigate(MenuAnyadirGrupoFragmentDirections.actionMenuAnyadirGrupoFragmentToNuevoGrupoFragment());
        }
        else if (id == R.id.botonAtras) {
            navController.popBackStack();
        }
    }
}
package com.mariana.androidhifam;


import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mariana.androidhifam.databinding.FragmentGruposBinding;
import com.mariana.androidhifam.databinding.FragmentLoginBinding;

import pojosalbumfamiliar.ExcepcionAlbumFamiliar;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private FragmentLoginBinding binding;
    private MainActivity activity;
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = (MainActivity) getActivity();
        binding.btnLogin.setOnClickListener(this);
    }

//    public Boolean iniciarSesion(int idUsuario) {
//
//    }

//    public void cargarVistaGrupos(Integer idUsuario) {
//        Thread tarea = new Thread(() -> {
//            try {
//                cargarNombreUsuario(idUsuario);
//                cargarGrupos(idUsuario);
//            } catch (ExcepcionAlbumFamiliar e) {
//                // Error
//            }
//        });
//        tarea.start();
//        try {
//            tarea.join(5000);
//        } catch (InterruptedException e) {
//            // Error
//        }
//        cargarGrid();
//        mostrarTextoAlternativo();
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnLogin) {
            int idUsuario = 1;
            activity.setIdUsuario(idUsuario);

            findNavController(v).navigate(LoginFragmentDirections.actionLoginFragmentToGruposFragment(idUsuario));
        }
    }
}
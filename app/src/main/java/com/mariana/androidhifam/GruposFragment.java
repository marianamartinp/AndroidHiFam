package com.mariana.androidhifam;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentGruposBinding;

import java.util.ArrayList;

public class GruposFragment extends Fragment {

    GruposFragmentArgs gruposFragmentArgs;
    FragmentGruposBinding binding;
    ArrayList<String> nombresGrupos;
    ArrayList<Integer> imagenesGrupos;
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGruposBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            GruposFragmentArgs args = GruposFragmentArgs.fromBundle(getArguments());
            Integer idUsuario = args.getIdUsuario();  // Extracting the string argument

            // Use the argument as needed
            Log.d("GruposFragment", "Received argument: " + idUsuario);
        }
        nombresGrupos = new ArrayList<>();
        nombresGrupos.add("grupo familiar");
        nombresGrupos.add("mejores amigosssssss");
        nombresGrupos.add("amigos de infancia");
        nombresGrupos.add("otroEjemplo");

        imagenesGrupos = new ArrayList<>();
        imagenesGrupos.add(R.drawable.imagen2);
        imagenesGrupos.add(R.drawable.imagen3);
        imagenesGrupos.add(R.drawable.imagen1);
        imagenesGrupos.add(R.drawable.imagen4);

        GridAdapter adapter = new GridAdapter(requireContext(), nombresGrupos, imagenesGrupos);
        binding.gridView.setAdapter(adapter);


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton ib = view.findViewById(R.id.botonnuevo);
        ib.setOnClickListener(v -> anyadirPublicacion());
    }

    public void anyadirPublicacion() {
        Toast.makeText(requireContext(), "Añadir publicación", Toast.LENGTH_SHORT).show();
    }
}
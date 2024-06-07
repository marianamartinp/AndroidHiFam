package com.mariana.androidhifam;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Fragment para el contenido de la toolbar
public class MenuFragment extends Fragment {

    // Método llamado cuando se crea el Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar la vista
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }
}
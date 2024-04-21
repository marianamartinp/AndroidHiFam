package com.mariana.androidhifam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> nombresGrupos;
    ArrayList<Integer> imagenesGrupos;

    LayoutInflater inflater;
    public GridAdapter(Context context, ArrayList<String> nombresGrupos, ArrayList<Integer> imagenesGrupos) {
        this.context = context;
        this.nombresGrupos = nombresGrupos;
        this.imagenesGrupos = imagenesGrupos;
    }

    @Override
    public int getCount() {
        return nombresGrupos.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, null);
        }

        ImageView imagen = convertView.findViewById(R.id.imagenGrupo);
        TextView titulo = convertView.findViewById(R.id.tituloGrupo);
        ImageView opciones = convertView.findViewById(R.id.opciones);

        imagen.setImageResource(imagenesGrupos.get(position));
        imagen.setOnClickListener(v -> Toast.makeText(context, "imagen: Grupo " + nombresGrupos.get(position), Toast.LENGTH_SHORT).show());
        titulo.setText(nombresGrupos.get(position));
        titulo.setOnClickListener(v -> Toast.makeText(context, "texto: Grupo " + nombresGrupos.get(position), Toast.LENGTH_SHORT).show());

        opciones.setOnClickListener(v -> Toast.makeText(context, "Las opciones de " + nombresGrupos.get(position), Toast.LENGTH_SHORT).show());

        //convertView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 550));

        return convertView;
    }
}

package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pojosalbumfamiliar.*;

public class GridAdapter<T> extends BaseAdapter {
    private Context context;
    private ArrayList<T> objetos;
    private ArrayList<Integer> imagenes;
    private Boolean vistaIndividual;
    LayoutInflater inflater;


    public GridAdapter(Context context, ArrayList<T> arraylistObjetos, ArrayList<Integer> imagenesGrupos, Boolean vistaIndividual) {
        this.context = context;
        this.objetos = arraylistObjetos;
        this.imagenes = imagenesGrupos;
        this.vistaIndividual = vistaIndividual;
    }

    @Override
    public int getCount() {
        return objetos.size();
    }

    @Override
    public T getItem(int position) {
        return objetos.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (objetos.get(0) instanceof Publicacion) {
            return ((ArrayList<Publicacion>) objetos).get(position).getCodPublicacion();
        }
        else if (objetos.get(0) instanceof Grupo) {
            return ((ArrayList<Grupo>) objetos).get(position).getCodGrupo();
        }
        else if (objetos.get(0) instanceof Album) {
            return ((ArrayList<Album>) objetos).get(position).getCodAlbum();
        }
        else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            if (objetos.get(0) instanceof Publicacion && !vistaIndividual) {
                convertView = inflater.inflate(R.layout.grid_item_simple, null);
            }
            else if (objetos.get(0) instanceof Publicacion && vistaIndividual) {
                convertView = inflater.inflate(R.layout.grid_item_simple_individual, null);
            }
            else {
                convertView = inflater.inflate(R.layout.grid_item_complejo, null);
            }
        }

        TextView titulo = convertView.findViewById(R.id.titulo);
        ImageView imagen = convertView.findViewById(R.id.imagen);

        if (position <= imagenes.size()) {
            imagen.setImageResource(imagenes.get(position));
        }
        else {
            imagen.setImageResource(R.drawable.hifamisot);
        }

        if (objetos.get(0) instanceof Album) {
            inflarAlbumes(position, convertView, imagen, titulo);
        }

        if (objetos.get(0) instanceof Publicacion) {
            inflarPublicaciones(position, convertView, imagen);
        }

        if (objetos.get(0) instanceof Grupo) {
            inflarGrupos(position, convertView, imagen, titulo);
        }

        return convertView;
    }

    public void inflarGrupos(int position, View convertView, ImageView imagen, TextView titulo) {
        ArrayList<Grupo> grupos = (ArrayList<Grupo>) objetos;
        titulo.setText(grupos.get(position).getTitulo());
    }

    public void inflarAlbumes(int position, View convertView, ImageView imagen, TextView titulo) {
        ArrayList<Album> albumes = (ArrayList<Album>) objetos;
        titulo.setText(albumes.get(position).getTitulo());
    }

    public void inflarPublicaciones(int position, View convertView, ImageView imagen){
        ArrayList<Publicacion> publicaciones = (ArrayList<Publicacion>) objetos;
        //imagen.setImageResource(R.drawable.hifamisot);
    }

}

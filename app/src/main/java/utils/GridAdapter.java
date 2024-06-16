package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mariana.androidhifam.R;

import java.io.File;
import java.util.ArrayList;

import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.Publicacion;

public class GridAdapter<T> extends BaseAdapter {
    private Context context;
    private ArrayList<T> objetos;
    private ArrayList<File> imagenes;
    private ArrayList<Album> albumes;
    private Boolean vistaIndividual;
    private LayoutInflater inflater;


    public GridAdapter(Context context, ArrayList<T> objetos, ArrayList<File> imagenes, Boolean vistaIndividual) {
        this.context = context;
        this.objetos = objetos;
        this.imagenes = imagenes;
        this.vistaIndividual = vistaIndividual;
    }

    // Constructor particularmente para cargar grupos con sus respectivas imágenes
    public GridAdapter(Context context, ArrayList<T> objetos, ArrayList<File> imagenes, Boolean vistaIndividual, ArrayList<Album> albumes) {
        this.context = context;
        this.objetos = objetos;
        this.imagenes = imagenes;
        this.vistaIndividual = vistaIndividual;
        this.albumes = albumes;
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

        imagen.setImageResource(R.drawable.noimagen);

        if (objetos.get(0) instanceof Album) {
            inflarAlbumes(position, imagen, titulo);
        }

        if (objetos.get(0) instanceof Publicacion) {
            inflarPublicaciones(position, imagen);
        }

        if (objetos.get(0) instanceof Grupo) {
            inflarGrupos(position, context, imagen, titulo);
        }

        return convertView;
    }

    public void inflarGrupos(int position, Context context, ImageView imagen, TextView titulo) {
        ArrayList<Grupo> grupos = (ArrayList<Grupo>) objetos;
        titulo.setText(grupos.get(position).getTitulo());
        if (!imagenes.isEmpty()) {
            for (File imagenLista : imagenes) {
                String grupoImagen = imagenLista.getName().substring(0,3);
                String grupoEsperado = String.format("%03d", grupos.get(position).getCodGrupo());
                if (null != albumes) {
                    String albumImagen = imagenLista.getName().substring(3,8);
                    for (Album album : albumes) {
                        String albumEsperado = String.format("%05d", album.getCodAlbum());
                        if (grupoImagen.equals(grupoEsperado) && albumImagen.equals(albumEsperado)) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagenLista.getAbsolutePath());
                            if (bitmap != null) {
                                imagen.setImageBitmap(bitmap);
                                break;
                            } else {
                                Log.e("ImageView", "Error al cargar las portadas." + imagenLista.getAbsolutePath());
                            }
                        }
                    }
                }
                else {
                    if (grupoImagen.equals(grupoEsperado)) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imagenLista.getAbsolutePath());
                        if (bitmap != null) {
                            imagen.setImageBitmap(bitmap);
                            break;
                        } else {
                            Log.e("ImageView", "Error al cargar las portadas." + imagenLista.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    public void inflarAlbumes(int position, ImageView imagen, TextView titulo) {
        ArrayList<Album> albumes = (ArrayList<Album>) objetos;
        titulo.setText(albumes.get(position).getTitulo());
        if (!imagenes.isEmpty()) {
            for (File imagenLista : imagenes) {
                String nombreImagen = imagenLista.getName().substring(0,8);
                String stringEsperado = String.format("%03d", albumes.get(position).getGrupoCreaAlbum().getCodGrupo()) +
                        String.format("%05d", albumes.get(position).getCodAlbum());
                if (nombreImagen.equals(stringEsperado)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagenLista.getAbsolutePath());
                    if (bitmap != null) {
                        imagen.setImageBitmap(bitmap);
                        break;
                    } else {
                        Log.e("ImageView", "Failed to decode the image file: " + imagenLista.getAbsolutePath());
                    }
                }
            }
        }
    }

    public void inflarPublicaciones(int position, ImageView imagen){
        ArrayList<Publicacion> publicaciones = (ArrayList<Publicacion>) objetos;
        if (!imagenes.isEmpty()) {
            for (File imagenLista : imagenes) {
                if (imagenLista.getName().equals(publicaciones.get(position).getArchivo().getTitulo())) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagenLista.getAbsolutePath());
                    if (bitmap != null) {
                        imagen.setImageBitmap(bitmap);
                        break;
                    } else {
                        Log.e("ImageView", "Failed to decode the image file: " + imagenLista.getAbsolutePath());
                    }
                }
            }
        }
    }
}

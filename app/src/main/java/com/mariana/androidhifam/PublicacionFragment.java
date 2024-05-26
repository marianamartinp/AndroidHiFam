package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mariana.androidhifam.databinding.FragmentPublicacionBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Comentario;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;


public class PublicacionFragment extends Fragment implements View.OnClickListener, MainActivity.SwipeToRefreshLayout {
    private PublicacionFragmentArgs publicacionFragmentArgs;
    private @NonNull FragmentPublicacionBinding binding;
    private Publicacion publicacion;
    private String tituloAlbum;
    private ArrayList<Comentario> comentarios;
    private Integer idPublicacion, imagenPublicacion;
    private CCAlbumFamiliar cliente;
    private ListAdapter<Comentario> adapter;
    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicacionFragmentArgs = PublicacionFragmentArgs.fromBundle(getArguments());
            idPublicacion = publicacionFragmentArgs.getIdPublicacion();
        }
        activity = (MainActivity) getActivity();
        comentarios = new ArrayList<>();
        publicacion = new Publicacion();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPublicacionBinding.inflate(inflater, container, false);
        cliente = activity.getCliente();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setRefreshLayout(this);
        binding.botonNuevoComentario.setOnClickListener(this);
        binding.botonOpciones.setOnClickListener(this);
        cargarVistaPublicacion(idPublicacion);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void cargarTituloAlbum(Integer idAlbum) throws ExcepcionAlbumFamiliar {
        tituloAlbum = cliente.leerAlbum(idAlbum).getTitulo();
    }

    public void cargarPublicacion(Integer idPublicacion) throws ExcepcionAlbumFamiliar {
        publicacion = cliente.leerPublicacion(idPublicacion);
    }

    public void cargarComentarios(Integer idPublicacion) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("c.COD_PUBL_TIENE_COMENTARIO", "="+idPublicacion);
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("c.FECHA_CREACION", "desc");
        comentarios = cliente.leerComentarios(filtros,ordenacion);
    }

    public void cargarListaComentarios() {
        if (!comentarios.isEmpty()) {
            adapter = new ListAdapter<>(requireContext(), comentarios, ItemsListAdapter.ITEM_COMENTARIO, null);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerView.setAdapter(adapter);
        }
        else {
            binding.textoAlternativo.setText("No hay nada por aquí.");
        }
    }
    public void cargarFramePublicacion() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String usuario = publicacion.getUsuarioCreaPublicacion().getUsuario();
        String fecha = formatter.format(publicacion.getFechaCreacion());
        binding.infoAdicional.setText("@" + usuario + ", " + fecha);
        binding.tituloDescripcion.setText(publicacion.getTitulo() + ": " + publicacion.getTexto());
        binding.tituloAlbum.setText(tituloAlbum);
        establecerImagen();
    }

    public void establecerImagen() {
        ArrayList<File> imagenes = activity.getImagenes();
        if (!imagenes.isEmpty()) {
            for (File imagenLista : imagenes) {
                if (imagenLista.getName().equals(publicacion.getArchivo().getTitulo())) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagenLista.getAbsolutePath());
                    if (bitmap != null) {
                        binding.imagen.setImageBitmap(bitmap);
                        break;
                    } else {
                        Log.e("ImageView", "Failed to decode the image file: " + imagenLista.getAbsolutePath());
                    }
                }
            }
        }
    }

    public void cargarVistaPublicacion(Integer idPublicacion) {
        Thread tarea = new Thread(() -> {
            try {
                cargarPublicacion(idPublicacion);
                cargarTituloAlbum(publicacion.getPublicacionEnAlbum());
                cargarComentarios(idPublicacion);
            } catch (ExcepcionAlbumFamiliar e) {
                // Error
            }
        });
        tarea.start();
        try {
            tarea.join(5000);
        } catch (InterruptedException e) {
            // Error
        }
        cargarFramePublicacion();
        cargarListaComentarios();
    }

    public void menuPopUp() {
        PopupMenu popup = new PopupMenu(requireActivity(), binding.botonOpciones);
        popup.getMenuInflater()
                .inflate(R.menu.menu_context_grupos, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(requireActivity(), "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        popup.show();
    }
//
//    public void actualizarAdapter(){
//        adapter.notifyDataSetChanged();
//    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonNuevoComentario) {
            Toast.makeText(requireContext(), "Añadir comentario", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.botonOpciones) {
            menuPopUp();
        }
    }

    // Implementación de la interfaz creada para definir las acciones a llevar a cabo al cargar la página.
    @Override
    public void onSwipeToRefresh() {
        cargarVistaPublicacion(idPublicacion);
        Toast.makeText(getContext(), "Se ha actualizado la publicación.", Toast.LENGTH_SHORT).show();
    }
}
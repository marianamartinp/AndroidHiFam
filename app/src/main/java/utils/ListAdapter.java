package utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mariana.androidhifam.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

import pojosalbumfamiliar.Comentario;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.SolicitudEntradaGrupo;
import pojosalbumfamiliar.Usuario;

public class ListAdapter<T> extends RecyclerView.Adapter<ListAdapter.ViewHolder>{
    private ArrayList<T> objetos;
    private Context context;
    private LayoutInflater inflater;
    private OnItemClickListener listener;
    private static Integer item, adminAlbum, adminGrupo, tokenUsuario; // Constante con un número que referencia al tipo de de dato a inflar.


    public interface OnItemClickListener {
        void onItemClick(Object item, int position, int idButton);
    }

    // Constructor genérico
    public ListAdapter(Context context, ArrayList<T> objetos, int item, OnItemClickListener listener) {
        this.objetos = objetos;
        this.context = context;
        ListAdapter.item = item;
        this.listener = listener;
    }

    // Constructor para los comentarios
    public ListAdapter(Context context, ArrayList<T> objetos, int item, OnItemClickListener listener, Integer adminGrupo, Integer adminAlbum, int tokenUsuario) {
        this.objetos = objetos;
        this.context = context;
        ListAdapter.item = item;
        this.listener = listener;
        ListAdapter.adminAlbum = adminAlbum;
        ListAdapter.adminGrupo = adminGrupo;
        ListAdapter.tokenUsuario = tokenUsuario;
    }

    // Constructor para la gestión de miembros de un grupo.
    public ListAdapter(Context context, ArrayList<T> objetos, int item, OnItemClickListener listener, Integer adminGrupo, Integer tokenUsuario) {
        this.objetos = objetos;
        this.context = context;
        ListAdapter.item = item;
        this.listener = listener;
        ListAdapter.adminGrupo = adminGrupo;
        ListAdapter.tokenUsuario = tokenUsuario;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        View itemLista = null;

        switch(item) {
            case ItemsListAdapter.ITEM_COMENTARIO:
                itemLista = inflater.inflate(R.layout.comentario_item, parent, false);
                break;
            case ItemsListAdapter.ITEM_MIEMBRO_GRUPO:
                itemLista = inflater.inflate(R.layout.item_anyadir_miembro_grupo, parent, false);
                break;
            case ItemsListAdapter.ITEM_SOLICITUD_GRUPO:
                itemLista = inflater.inflate(R.layout.item_solicitud_grupo, parent, false);
                break;
            case ItemsListAdapter.ITEM_GRUPO_USUARIO:
                itemLista = inflater.inflate(R.layout.item_grupo_usuario, parent, false);
                break;
        }
        return new ViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(objetos.get(position), listener, position);
    }


    @Override
    public int getItemCount() {
        return objetos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView usuario, contenido, fecha, titulo;
        public ImageView iconoEquis, iconoTick, iconoEliminar;

        public ViewHolder(View itemView) {
            super(itemView);
            switch(item) {
                case ItemsListAdapter.ITEM_COMENTARIO:
                    this.usuario = itemView.findViewById(R.id.usuarioComentario);
                    this.contenido = itemView.findViewById(R.id.contenidoComentario);
                    this.fecha = itemView.findViewById(R.id.fechaComentario);
                    this.iconoEliminar = itemView.findViewById(R.id.iconoEliminar);
                    break;
                case ItemsListAdapter.ITEM_MIEMBRO_GRUPO:
                    this.usuario = itemView.findViewById(R.id.usuarioMiembro);
                    this.iconoEquis = itemView.findViewById(R.id.iconoEquis);
                    break;
                case ItemsListAdapter.ITEM_SOLICITUD_GRUPO:
                    this.usuario = itemView.findViewById(R.id.usuarioSolicitante);
                    this.fecha = itemView.findViewById(R.id.fechaSolicitud);
                    this.iconoEquis = itemView.findViewById(R.id.iconoEquis);
                    this.iconoTick = itemView.findViewById(R.id.iconoTick);
                    break;
                case ItemsListAdapter.ITEM_GRUPO_USUARIO:
                    this.titulo = itemView.findViewById(R.id.tituloGrupo);
                    this.iconoEquis = itemView.findViewById(R.id.iconoEquis);
                    break;
            }
        }


        public void bind(final Object objeto, final OnItemClickListener listener, int position) {
            switch(item) {
                case ItemsListAdapter.ITEM_COMENTARIO:
                    Comentario comentario = (Comentario) objeto;
                    this.usuario.setText("@" + comentario.getUsuarioCreaComentario().getUsuario());
                    this.fecha.setText(Utils.parsearDateAString(comentario.getFechaCreacion()));
                    this.contenido.setText(comentario.getTexto());
                    if (Objects.equals(tokenUsuario, comentario.getUsuarioCreaComentario().getCodUsuario()) ||
                        Objects.equals(tokenUsuario, adminAlbum) || Objects.equals(tokenUsuario, adminGrupo)) {
                        this.iconoEliminar.setVisibility(View.VISIBLE);
                        this.iconoEliminar.setOnClickListener(new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                listener.onItemClick(objeto, position, R.id.iconoEliminar);
                            }
                        });
                    }
                    else {
                        this.iconoEliminar.setVisibility(View.INVISIBLE);
                    }
                    break;
                case ItemsListAdapter.ITEM_MIEMBRO_GRUPO:
                    Usuario usuario = (Usuario) objeto;
                    this.usuario.setText("@" + usuario.getUsuario());
                    // Caso que se dará al poblar los miembros al modificar un grupo
                    if (null != tokenUsuario && null != adminGrupo) {
                        if (Objects.equals(tokenUsuario, adminGrupo)) {
                            if (!Objects.equals(tokenUsuario, usuario.getCodUsuario())) {
                                this.iconoEquis.setVisibility(View.VISIBLE);
                                this.iconoEquis.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        listener.onItemClick(objeto, position, R.id.iconoEquis);
                                    }
                                });
                            }
                            else {
                                this.iconoEquis.setVisibility(View.INVISIBLE);
                            }
                        }
                        else {
                            this.iconoEquis.setVisibility(View.INVISIBLE);
                        }
                    }
                    // Resto de casos
                    else {
                        this.iconoEquis.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                listener.onItemClick(objeto, position, R.id.iconoEquis);
                            }
                        });
                    }
                    break;
                case ItemsListAdapter.ITEM_SOLICITUD_GRUPO:
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    sdf.setLenient(false);
                    SolicitudEntradaGrupo solicitud = (SolicitudEntradaGrupo) objeto;
                    this.usuario.setText("@"+solicitud.getUsuario().getUsuario());
                    this.fecha.setText(sdf.format(solicitud.getFechaSolicitud()));
                    this.iconoEquis.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            listener.onItemClick(objeto, position, R.id.iconoEquis);
                        }
                    });
                    this.iconoTick.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            listener.onItemClick(objeto, position, R.id.iconoTick);
                        }
                    });
                    break;
                case ItemsListAdapter.ITEM_GRUPO_USUARIO:
                    Grupo grupo = (Grupo) objeto;
                    this.titulo.setText(grupo.getTitulo());
                    this.iconoEquis.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onItemClick(objeto, position, R.id.iconoEquis);
                        }
                    });
                    break;
            }
        }
    }
}
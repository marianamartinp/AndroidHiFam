package com.mariana.androidhifam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import pojosalbumfamiliar.Comentario;
import pojosalbumfamiliar.SolicitudEntradaGrupo;
import pojosalbumfamiliar.Usuario;
import pojosalbumfamiliar.UsuarioIntegraGrupo;

public class ListAdapter<T> extends RecyclerView.Adapter<ListAdapter.ViewHolder>{
    private ArrayList<T> objetos;
    private Context context;
    private LayoutInflater inflater;
    private OnItemClickListener listener;
    private static int item; // Constante con un número que referencia al tipo de de dato a inflar.


    public interface OnItemClickListener {
        void onItemClick(Object item, int position, int idButton);
    }

    // RecyclerView recyclerView;
    public ListAdapter(Context context, ArrayList<T> objetos, int item, OnItemClickListener listener) {
        this.objetos = objetos;
        this.context = context;
        ListAdapter.item = item;
        this.listener = listener;
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
        public TextView usuario, contenido, fechaSolicitud;
        public ImageView iconoEquis, iconoTick;

        public ViewHolder(View itemView) {
            super(itemView);
            switch(item) {
                case ItemsListAdapter.ITEM_COMENTARIO:
                    this.usuario = itemView.findViewById(R.id.usuarioComentario);
                    this.contenido = itemView.findViewById(R.id.contenidoComentario);
                    break;
                case ItemsListAdapter.ITEM_MIEMBRO_GRUPO:
                    this.usuario = itemView.findViewById(R.id.usuarioMiembro);
                    this.iconoEquis = itemView.findViewById(R.id.iconoEquis);
                    break;
                case ItemsListAdapter.ITEM_SOLICITUD_GRUPO:
                    this.usuario = itemView.findViewById(R.id.usuarioSolicitante);
                    this.fechaSolicitud = itemView.findViewById(R.id.fechaSolicitud);
                    this.iconoEquis = itemView.findViewById(R.id.iconoEquis);
                    this.iconoTick = itemView.findViewById(R.id.iconoTick);
                    break;
            }
        }


        public void bind(final Object objeto, final OnItemClickListener listener, int position) {
            switch(item) {
                case ItemsListAdapter.ITEM_COMENTARIO:
                    Comentario comentario = (Comentario) objeto;
                    this.usuario.setText(comentario.getUsuarioCreaComentario().getUsuario());
                    this.contenido.setText(comentario.getTexto());
                    break;
                case ItemsListAdapter.ITEM_MIEMBRO_GRUPO:
                    Usuario usuario = (Usuario) objeto;
                    this.usuario.setText("@"+usuario.getUsuario());
                    this.iconoEquis.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            listener.onItemClick(objeto, position, R.id.iconoEquis);
                        }
                    });
                    break;
                case ItemsListAdapter.ITEM_SOLICITUD_GRUPO:
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    sdf.setLenient(false);
                    SolicitudEntradaGrupo solicitud = (SolicitudEntradaGrupo) objeto;
                    this.usuario.setText("@"+solicitud.getUsuario().getUsuario());
                    this.fechaSolicitud.setText(sdf.format(solicitud.getFechaSolicitud()));

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
            }
        }
    }
}
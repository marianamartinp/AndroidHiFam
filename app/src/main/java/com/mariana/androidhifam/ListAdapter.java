package com.mariana.androidhifam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pojosalbumfamiliar.Comentario;
import pojosalbumfamiliar.UsuarioIntegraGrupo;

public class ListAdapter<T> extends RecyclerView.Adapter<ListAdapter.ViewHolder>{
    private ArrayList<T> objetos;
    private Context context;
    private LayoutInflater inflater;
    private static int item; // Constante con un número que referencia al tipo de de dato a inflar.

    // RecyclerView recyclerView;
    public ListAdapter(Context context, ArrayList<T> objetos, int item) {
        this.objetos = objetos;
        this.context = context;
        this.item = item;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        View itemLista = null;

        switch(item) {
            case ItemsListAdapter.ITEM_COMENTARIO:
                itemLista= inflater.inflate(R.layout.comentario_item, parent, false);
                break;
            case ItemsListAdapter.ITEM_MIEMBRO_GRUPO:
                itemLista= inflater.inflate(R.layout.item_anyadir_miembro_grupo, parent, false);
                break;
        }
        return new ViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch(item) {
            case ItemsListAdapter.ITEM_COMENTARIO:
                ArrayList<Comentario> comentarios = (ArrayList<Comentario>) objetos;
                holder.usuario.setText(comentarios.get(position).getUsuarioCreaComentario().getUsuario());
                holder.contenido.setText(comentarios.get(position).getTexto());
                break;
            case ItemsListAdapter.ITEM_MIEMBRO_GRUPO:
                ArrayList<String> usuarios = (ArrayList<String>) objetos;
                holder.usuario.setText(usuarios.get(position));
                break;
        }
    }


    @Override
    public int getItemCount() {
        return objetos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView usuario, contenido;
        public ViewHolder(View itemView) {
            super(itemView);
            switch(item) {
                case ItemsListAdapter.ITEM_COMENTARIO:
                    this.usuario = itemView.findViewById(R.id.usuarioComentario);
                    this.contenido = itemView.findViewById(R.id.contenidoComentario);
                    break;
                case ItemsListAdapter.ITEM_MIEMBRO_GRUPO:
                    this.usuario = itemView.findViewById(R.id.usuarioMiembro);
                    break;
            }
        }
    }
}
package com.mariana.androidhifam;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;

public class ServicioPublicacion {

    CCAlbumFamiliar cliente;
    public ServicioPublicacion() {
        cliente = new CCAlbumFamiliar();
    }
    public String cargarTituloAlbum(Integer idAlbum) throws ExcepcionAlbumFamiliar {
        return cliente.leerAlbum(idAlbum).getTitulo();
    }

    public ArrayList<Publicacion> cargarPublicaciones(Integer idAlbum) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("pea.COD_ALBUM", "="+idAlbum);
        filtros.put("p.FECHA_ELIMINACION", "is null");
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("p.COD_PUBLICACION", "asc");
        return cliente.leerPublicaciones(filtros,ordenacion);
    }


}

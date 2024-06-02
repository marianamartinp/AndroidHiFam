package com.mariana.androidhifam;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Publicacion;

public class ServicioPublicacion {

    CCAlbumFamiliar cliente;
    public ServicioPublicacion() {
        cliente = new CCAlbumFamiliar();
    }
    public Album cargarAlbum(Integer idAlbum) throws ExcepcionAlbumFamiliar {
        return cliente.leerAlbum(idAlbum);
    }

    public ArrayList<Publicacion> cargarPublicaciones(Integer idAlbum) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("pea.COD_ALBUM", "="+idAlbum);
        filtros.put("p.FECHA_ELIMINACION", "is null");
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("p.COD_PUBLICACION", "asc");
        return cliente.leerPublicaciones(filtros,ordenacion);
    }

    public int insertarPublicacion(Publicacion publicacion) throws ExcepcionAlbumFamiliar {
        return cliente.insertarPublicacion(publicacion);
    }


}

package com.mariana.androidhifam;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.SolicitudEntradaGrupo;
import pojosalbumfamiliar.UsuarioIntegraGrupo;

public class ServicioSolicitudEntradaGrupo {

    CCAlbumFamiliar cliente;
    public ServicioSolicitudEntradaGrupo() {
        cliente = new CCAlbumFamiliar();
    }

    public ArrayList<SolicitudEntradaGrupo> leerSolicitudesEntradaGrupo(int idGrupo) throws ExcepcionAlbumFamiliar {
        LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
        filtros.put("g.COD_GRUPO", "=" + idGrupo);
        LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
        ordenacion.put("seg.FECHA_SOLICITUD", "asc");
        return cliente.leerSolicitudesEntradaGrupo(filtros, ordenacion);
    }

    public void aceptarSolicitudEntradaGrupo(SolicitudEntradaGrupo seg) throws ExcepcionAlbumFamiliar {
        cliente.insertarUsuarioIntegraGrupo(new UsuarioIntegraGrupo(seg.getUsuario(), seg.getGrupo()));
        cliente.eliminarSolicitudEntradaGrupo(seg.getUsuario().getCodUsuario(), seg.getGrupo().getCodGrupo());
    }

    public void rechazarSolicitudEntradaGrupo(SolicitudEntradaGrupo seg) throws ExcepcionAlbumFamiliar {
        cliente.eliminarSolicitudEntradaGrupo(seg.getUsuario().getCodUsuario(), seg.getGrupo().getCodGrupo());
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojosalbumfamiliar;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author DAM213
 */
public class SolicitudEntradaGrupo implements Serializable{
 
    //Atributos
    
    private static final long serialVersionUID = 11L;
    private Grupo grupo;
    private Usuario usuario;
    private Date fechaSolicitud;
    
    
    //Constructores

    public SolicitudEntradaGrupo() {
    }

    public SolicitudEntradaGrupo(Grupo grupo, Usuario usuario, Date fechaSolicitud) {
        this.grupo = grupo;
        this.usuario = usuario;
        this.fechaSolicitud = fechaSolicitud;
    }
    
    
    //Getters

    public Grupo getGrupo() {
        return grupo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Date getFechaSolicitud() {
        return fechaSolicitud;
    }

    
    //Setters

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setFechaSolicitud(Date fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    
    //toString

    @Override
    public String toString() {
        return "SolicitudEntradaGrupo{" + "grupo=" + grupo + ", usuario=" + usuario + ", fechaSolicitud=" + fechaSolicitud + '}';
    }   
    
}

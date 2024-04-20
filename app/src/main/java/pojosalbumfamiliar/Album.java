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
public class Album implements Serializable{
    
    //Atributos
    
    private static final long serialVersionUID = 1L;
    private Integer codAlbum;
    private String titulo;
    private String tipo;
    private String descripcion;
    private Date fechaCreacion;
    private Grupo grupoCreaAlbum;
    private Usuario usuarioAdminAlbum;
    private Date fechaEliminacion;
    
    
    //Constructores

    public Album() {
    }

    public Album(Integer codAlbum, String titulo, String tipo, String descripcion, Date fechaCreacion, Date fechaEliminacion, Grupo grupoCreaAlbum, Usuario usuarioAdminAlbum) {
        this.codAlbum = codAlbum;
        this.titulo = titulo;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.grupoCreaAlbum = grupoCreaAlbum;
        this.usuarioAdminAlbum = usuarioAdminAlbum;
        this.fechaEliminacion = fechaEliminacion;
    }
    
    
    //Getters

    public Integer getCodAlbum() {
        return codAlbum;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public Grupo getGrupoCreaAlbum() {
        return grupoCreaAlbum;
    }

    public Usuario getUsuarioAdminAlbum() {
        return usuarioAdminAlbum;
    }

    public Date getFechaEliminacion() {
        return fechaEliminacion;
    }
    
    
    //Setters

    public void setCodAlbum(Integer codAlbum) {
        this.codAlbum = codAlbum;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setGrupoCreaAlbum(Grupo codGrupoCreaAlbum) {
        this.grupoCreaAlbum = codGrupoCreaAlbum;
    }

    public void setUsuarioAdminAlbum(Usuario codUsuarioAdminAlbum) {
        this.usuarioAdminAlbum = codUsuarioAdminAlbum;
    }

    public void setFechaEliminacion(Date fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;
    }
    
    
    //toString

    @Override
    public String toString() {
        return "Album{" + "codAlbum=" + codAlbum + ", titulo=" + titulo + ", tipo=" + tipo + ", descripcion=" + descripcion + ", fechaCreacion=" + fechaCreacion + ", grupoCreaAlbum=" + grupoCreaAlbum + ", usuarioAdminAlbum=" + usuarioAdminAlbum + ", fechaEliminacion=" + fechaEliminacion + '}';
    }
    
}

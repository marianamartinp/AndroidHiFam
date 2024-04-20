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
public class Publicacion implements Serializable{
    
    //Atributos
    
    private static final long serialVersionUID = 5L;
    private Integer codPublicacion;
    private String titulo;
    private String texto;
    private Date fechaCreacion;
    private Usuario usuarioCreaPublicacion;
    private Archivo archivo;
    private Integer publicacionEnAlbum;
    private Date fechaEliminacion;
    
    
    //Constructores
    
    public Publicacion() {
    }

    public Publicacion(Integer codPublicacion, String titulo, Date fechaCreacion, Usuario usuarioCreaPublicacion, String texto, 
        Archivo archivo, Integer publicacionEnAlbum, Date fechaEliminacion) {
        this.codPublicacion = codPublicacion;
        this.titulo = titulo;
        this.fechaCreacion = fechaCreacion;
        this.usuarioCreaPublicacion = usuarioCreaPublicacion;
        this.archivo = archivo;
        this.publicacionEnAlbum = publicacionEnAlbum;
        this.texto = texto;
        this.fechaEliminacion = fechaEliminacion;
    }
    
    
    //Getters

    public Integer getCodPublicacion() {
        return codPublicacion;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTexto() {
        return texto;
    }
    
    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public Usuario getUsuarioCreaPublicacion() {
        return usuarioCreaPublicacion;
    }

    public Archivo getArchivo() {
        return archivo;
    }

    public Integer getPublicacionEnAlbum() {
        return publicacionEnAlbum;
    }

    public Date getFechaEliminacion() {
        return fechaEliminacion;
    }
    
    
    //Setters

    public void setCodPublicacion(Integer codPublicacion) {
        this.codPublicacion = codPublicacion;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
    
    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setUsuarioCreaPublicacion(Usuario usuarioCreaPublicacion) {
        this.usuarioCreaPublicacion = usuarioCreaPublicacion;
    }

    public void setArchivo(Archivo archivo) {
        this.archivo = archivo;
    }

    public void setPublicacionEnAlbum(Integer publicacionEnAlbum) {
        this.publicacionEnAlbum = publicacionEnAlbum;
    }

    public void setFechaEliminacion(Date fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;
    }
    
    
    //toString

    @Override
    public String toString() {
        return "Publicacion{" + "codPublicacion=" + codPublicacion + ", titulo=" + titulo + ", texto=" + texto + ", fechaCreacion=" + fechaCreacion + ", usuarioCreaPublicacion=" + usuarioCreaPublicacion + ", archivo=" + archivo + ", publicacionEnAlbum=" + publicacionEnAlbum + ", fechaEliminacion=" + fechaEliminacion + '}';
    }

}

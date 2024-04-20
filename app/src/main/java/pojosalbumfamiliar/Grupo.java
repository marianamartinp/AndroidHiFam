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
public class Grupo implements Serializable{
    
    //Atributos
    
    private static final long serialVersionUID = 4L;
    private Integer codGrupo;
    private String titulo;
    private String descripcion;
    private Date fechaCreacion;
    private Usuario usuarioAdminGrupo;
    private Date fechaEliminacion;
    
    
    //Constructores

    public Grupo() {
    }
   
    public Grupo(Integer codGrupo, String titulo, String descripcion, Date fechaCreacion, Date fechaEliminacion, Usuario usuarioAdminGrupo) {
        this.codGrupo = codGrupo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.usuarioAdminGrupo = usuarioAdminGrupo;
        this.fechaEliminacion = fechaEliminacion;
    }
    
    
    //Getters

    public Integer getCodGrupo() {
        return codGrupo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public Usuario getUsuarioAdminGrupo() {
        return usuarioAdminGrupo;
    }

    public Date getFechaEliminacion() {
        return fechaEliminacion;
    }
    
    
    //Setters

    public void setCodGrupo(Integer codGrupo) {
        this.codGrupo = codGrupo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setUsuarioAdminGrupo(Usuario usuarioAdminGrupo) {
        this.usuarioAdminGrupo = usuarioAdminGrupo;
    }
    
    public void setFechaEliminacion(Date fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;
    }
    
    //toString

    @Override
    public String toString() {
        return "Grupo{" + "codGrupo=" + codGrupo + ", titulo=" + titulo + ", descripcion=" + descripcion + ", fechaCreacion=" + fechaCreacion + ", usuarioAdminGrupo=" + usuarioAdminGrupo + ", fechaEliminacion=" + fechaEliminacion + '}';
    }
    
}

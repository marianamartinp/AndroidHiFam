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
public class Comentario implements Serializable {
    
    //Atributos
    
    private static final long serialVersionUID = 12L;
    private Integer codComentario;
    private String texto;
    private Date fechaCreacion;
    private Publicacion publicacionTieneComentario;
    private Usuario usuarioCreaComentario;
    
    
    //Constructores
    
    public Comentario() {
    }

    public Comentario(Integer codComentario, String texto, Date fechaCreacion, Publicacion publicacionTieneComentario, Usuario usuarioCreaComentario) {
        this.codComentario = codComentario;
        this.texto = texto;
        this.fechaCreacion = fechaCreacion;
        this.publicacionTieneComentario = publicacionTieneComentario;
        this.usuarioCreaComentario = usuarioCreaComentario;
    }
    
    
    //Getters

    public Integer getCodComentario() {
        return codComentario;
    }

    public String getTexto() {
        return texto;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public Publicacion getPublicacionTieneComentario() {
        return publicacionTieneComentario;
    }

    public Usuario getUsuarioCreaComentario() {
        return usuarioCreaComentario;
    }
    
    
    //Setters

    public void setCodComentario(Integer codComentario) {
        this.codComentario = codComentario;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setPublicacionTieneComentario(Publicacion publicacionTieneComentario) {
        this.publicacionTieneComentario = publicacionTieneComentario;
    }

    public void setUsuarioCreaComentario(Usuario usuarioCreaComentario) {
        this.usuarioCreaComentario = usuarioCreaComentario;
    }
    
    
    //toString

    @Override
    public String toString() {
        return "Comentario{" + "codComentario=" + codComentario + ", texto=" + texto + ", fechaCreacion=" + fechaCreacion + ", publicacionTieneComentario=" + publicacionTieneComentario + ", usuarioCreaComentario=" + usuarioCreaComentario + '}';
    }
    
}

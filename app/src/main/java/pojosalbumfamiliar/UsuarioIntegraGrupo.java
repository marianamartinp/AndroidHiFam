/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojosalbumfamiliar;

import java.io.Serializable;

/**
 *
 * @author DAM213
 */
public class UsuarioIntegraGrupo implements Serializable{
    
    //Atributos
    
    private static final long serialVersionUID = 10L;
    private Usuario usuario;
    private Grupo grupo;
    
    
    //Constructores

    public UsuarioIntegraGrupo() {
    }

    public UsuarioIntegraGrupo(Usuario usuario, Grupo grupo) {
        this.usuario = usuario;
        this.grupo = grupo;
    }
    
    
    //Getters

    public Usuario getUsuario() {
        return usuario;
    }

    public Grupo getGrupo() {
        return grupo;
    }
    
    
    //Setters

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }
    
    
    //toString

    @Override
    public String toString() {
        return "UsuarioIntegraGrupo{" + "usuario=" + usuario + ", grupo=" + grupo + '}';
    }
    
}

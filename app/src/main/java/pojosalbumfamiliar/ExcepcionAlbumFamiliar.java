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
public class ExcepcionAlbumFamiliar extends Exception implements Serializable{
    
    //Atributos
    
    private static final long serialVersionUID = 3L;
    private String mensajeUsuario;
    private String mensajeAdministrador;
    private String metodoError;
    private String sentenciaSql;
    private Integer codErrorBd;
    
    
    //Constructores

    public ExcepcionAlbumFamiliar() {
    }

    public ExcepcionAlbumFamiliar(String mensajeUsuario, String mensajeAdministrador, String sentenciaSql, String metodoError, Integer codErrorBd) {
        this.mensajeUsuario = mensajeUsuario;
        this.mensajeAdministrador = mensajeAdministrador;
        this.metodoError = metodoError;
        this.sentenciaSql = sentenciaSql;
        this.codErrorBd = codErrorBd;
    }
    
    
    //Getters

    public String getMensajeUsuario() {
        return mensajeUsuario;
    }

    public String getMensajeAdministrador() {
        return mensajeAdministrador;
    }

    public String getSentenciaSql() {
        return sentenciaSql;
    }

    public Integer getCodErrorBd() {
        return codErrorBd;
    }

    public String getMetodoError() {
        return metodoError;
    }
    
    
    //Setters

    public void setMensajeUsuario(String mensajeUsuario) {
        this.mensajeUsuario = mensajeUsuario;
    }

    public void setMensajeAdministrador(String mensajeAdministrador) {
        this.mensajeAdministrador = mensajeAdministrador;
    }

    public void setSentenciaSql(String sentenciaSql) {
        this.sentenciaSql = sentenciaSql;
    }

    public void setCodErrorBd(Integer codErrorBd) {
        this.codErrorBd = codErrorBd;
    }

    public void setMetodoError(String metodoError) {
        this.metodoError = metodoError;
    }
    
    
    //toString

    @Override
    public String toString() {
        return "ExcepcionAlbumFamiliar{" + "mensajeUsuario=" + mensajeUsuario + ", mensajeAdministrador=" + mensajeAdministrador + ", metodoError=" + metodoError + ", sentenciaSql=" + sentenciaSql + ", codErrorBd=" + codErrorBd + '}';
    }    
            
}

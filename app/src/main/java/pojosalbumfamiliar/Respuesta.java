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
public class Respuesta implements Serializable{
    
    private static final long serialVersionUID = 8L;
    private Integer idOperacion, cantidad;
    private Object entidad;
    private ExcepcionAlbumFamiliar e;

    public Respuesta() {
    }

    public Integer getIdOperacion() {
        return idOperacion;
    }

    public void setIdOperacion(Integer idOperacion) {
        this.idOperacion = idOperacion;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Object getEntidad() {
        return entidad;
    }

    public void setEntidad(Object entidad) {
        this.entidad = entidad;
    }

    public ExcepcionAlbumFamiliar getE() {
        return e;
    }

    public void setE(ExcepcionAlbumFamiliar e) {
        this.e = e;
    }

    @Override
    public String toString() {
        return "Respuesta{" + "idOperacion=" + idOperacion + ", cantidad=" + cantidad + ", entidad=" + entidad + ", e=" + e + '}';
    }
    
}

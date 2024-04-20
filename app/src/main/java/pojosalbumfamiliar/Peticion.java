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
public class Peticion implements Serializable{
    
    private static final long serialVersionUID = 7L;
    private Integer idOperacion;
    private Object entidad, idEntidad;

    public Peticion() {
    }

    public Peticion(Integer idOperacion, Object idEntidad, Object entidad) {
        this.idOperacion = idOperacion;
        this.idEntidad = idEntidad;
        this.entidad = entidad;
    }

    public Integer getIdOperacion() {
        return idOperacion;
    }

    public void setIdOperacion(Integer idOperacion) {
        this.idOperacion = idOperacion;
    }

    public Object getIdEntidad() {
        return idEntidad;
    }

    public void setIdEntidad(Object idEntidad) {
        this.idEntidad = idEntidad;
    }

    public Object getEntidad() {
        return entidad;
    }

    public void setEntidad(Object entidad) {
        this.entidad = entidad;
    }

    @Override
    public String toString() {
        return "Peticion{" + "idOperacion=" + idOperacion + ", idEntidad=" + idEntidad + ", entidad=" + entidad + '}';
    }
    
}

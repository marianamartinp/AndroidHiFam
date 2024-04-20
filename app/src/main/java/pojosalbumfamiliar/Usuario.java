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
public class Usuario implements Serializable{
    
    //Atributos
    
    private static final long serialVersionUID = 9L;
    private Integer codUsuario;
    private String nombre;
    private String usuario;
    private String email;
    private String telefono;
    private String contrasenya;
    private Date fechaNacimiento;
    private Date fechaEliminacion;
    
    
    //Constructores
    
    public Usuario() {
    }

    public Usuario(Integer codUsuario, String nombre, String usuario, String email, String telefono, String contrasenya, 
        Date fechaNacimiento, Date fechaEliminacion) {
        this.codUsuario = codUsuario;
        this.nombre = nombre;
        this.usuario = usuario;
        this.email = email;
        this.telefono = telefono;
        this.contrasenya = contrasenya;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaEliminacion = fechaEliminacion;
    }

    
    //Getters
    
    public Integer getCodUsuario() {
        return codUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getContrasenya() {
        return contrasenya;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public Date getFechaEliminacion() {
        return fechaEliminacion;
    }
    
    
    //Setters
    
    public void setCodUsuario(Integer codUsuario) {
        this.codUsuario = codUsuario;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setContrasenya(String contrasenya) {
        this.contrasenya = contrasenya;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setFechaEliminacion(Date fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;
    }
    
    
    //toString

    @Override
    public String toString() {
        return "Usuario{" + "codUsuario=" + codUsuario + ", nombre=" + nombre + ", usuario=" + usuario + ", email=" + email + ", telefono=" + telefono + ", contrasenya=" + contrasenya + ", fechaNacimiento=" + fechaNacimiento + ", fechaEliminacion=" + fechaEliminacion + '}';
    }
    
        
}

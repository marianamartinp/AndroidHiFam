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
public class Archivo implements Serializable{
 
    //Atributos
    
    private static final long serialVersionUID = 2L;
    private Integer codArchivo;
    private String titulo;
    private String ruta;
    
    
    //Constructores

    public Archivo() {
    }

    public Archivo(Integer codArchivo, String titulo, String ruta) {
        this.codArchivo = codArchivo;
        this.titulo = titulo;
        this.ruta = ruta;
    }
    
    
    //Getters

    public Integer getCodArchivo() {
        return codArchivo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getRuta() {
        return ruta;
    }

    
    //Setters

    public void setCodArchivo(Integer codArchivo) {
        this.codArchivo = codArchivo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    
    //toString

    @Override
    public String toString() {
        return "Archivo{" + "codArchivo=" + codArchivo + ", titulo=" + titulo + ", ruta=" + ruta + '}';
    }
    
    
}

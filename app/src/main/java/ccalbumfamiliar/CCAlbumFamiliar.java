/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccalbumfamiliar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.Comentario;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;
import pojosalbumfamiliar.Operaciones;
import pojosalbumfamiliar.Peticion;
import pojosalbumfamiliar.Publicacion;
import pojosalbumfamiliar.Respuesta;
import pojosalbumfamiliar.SolicitudEntradaGrupo;
import pojosalbumfamiliar.Usuario;
import pojosalbumfamiliar.UsuarioIntegraGrupo;

/**
 *
 * @author DAM213
 */
public class CCAlbumFamiliar {
    
    private Socket socketCliente;
    
    public void conectar() throws ExcepcionAlbumFamiliar{
        try {
//            String equipoServidor = "172.16.213.69";
            String equipoServidor = "192.168.1.213";
//            String equipoServidor = "172.16.5.34";
            int puertoServidor = 30500;
            socketCliente = new Socket(equipoServidor, puertoServidor); 
            socketCliente.setSoTimeout(10000);
        } catch (IOException ex) {
            manejadorIOException(ex);
        }
    }
       
    public void desconectar(){
        try {
            socketCliente.close();
        } catch (IOException ex) {
            ex.getMessage();
        }
    }
    
    public Respuesta gestionarComunicacion (Peticion p) throws ExcepcionAlbumFamiliar{
        Respuesta r = null;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socketCliente.getOutputStream());
            oos.writeObject(p);
            ObjectInputStream ois = new ObjectInputStream(socketCliente.getInputStream());
            r = (Respuesta) ois.readObject();
            ois.close();
            oos.close();
        } catch (IOException ex) {
            manejadorIOException(ex);
        } catch (ClassNotFoundException ex) {
            manejadorClassNotFoundException(ex);
        }
        return r;
    }
    
    // MÉTODOS DE PETICIÓN USUARIO.
    public int insertarUsuario(Usuario usuario) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.INSERTAR_USUARIO);
        p.setEntidad(usuario);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }

    public int modificarUsuario(Integer codUsuario, Usuario usuario) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.MODIFICAR_USUARIO);
        p.setIdEntidad(codUsuario);
        p.setEntidad(usuario);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE()!=null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    
    public int eliminarUsuario(Integer codUsuario) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.ELIMINAR_USUARIO);
        p.setIdEntidad(codUsuario);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    public Usuario leerUsuario(Integer codUsuario)throws ExcepcionAlbumFamiliar{
        Usuario usuario = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_USUARIO);
        p.setIdEntidad(codUsuario);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            usuario = (Usuario) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return usuario;
    }
    

    public ArrayList<Usuario> leerUsuarios(LinkedHashMap<String,String> filtros, LinkedHashMap<String,String> ordenacion) throws ExcepcionAlbumFamiliar {
        ArrayList<Usuario> listaUsuarios = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_USUARIOS);
        ArrayList<LinkedHashMap> argumentos = new ArrayList<>();
        argumentos.add(filtros);
        argumentos.add(ordenacion);
        p.setEntidad(argumentos);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            listaUsuarios = (ArrayList<Usuario>) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return listaUsuarios;
    }
    
    // MÉTODOS DE PETICIÓN ALBUM.
    public int insertarAlbum(Album album) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.INSERTAR_ALBUM);
        p.setEntidad(album);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    
    public int modificarAlbum(Integer codAlbum, Album album) throws ExcepcionAlbumFamiliar{        
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.MODIFICAR_ALBUM);
        p.setIdEntidad(codAlbum);
        p.setEntidad(album);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE()!=null) {
            throw r.getE();
        }
        return registrosAfectados;
    }

    public int eliminarAlbum(Integer codAlbum) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.ELIMINAR_ALBUM);
        p.setIdEntidad(codAlbum);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    
    public Album leerAlbum(Integer codAlbum) throws ExcepcionAlbumFamiliar{
        Album album = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_ALBUM);
        p.setIdEntidad(codAlbum);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            album = (Album) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return album;
    }
    

    public ArrayList<Album> leerAlbumes(LinkedHashMap<String,String> filtros, LinkedHashMap<String,String> ordenacion) throws ExcepcionAlbumFamiliar{
        ArrayList<Album> listaAlbumes = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_ALBUMES);
        ArrayList<LinkedHashMap> argumentos = new ArrayList<>();
        argumentos.add(filtros);
        argumentos.add(ordenacion);
        p.setEntidad(argumentos);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            listaAlbumes = (ArrayList<Album>) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return listaAlbumes;
    }
    
    public int restaurarAlbum(Integer codAlbum) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.RESTAURAR_ALBUM);
        p.setIdEntidad(codAlbum);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    // MÉTODOS DE PETICIÓN GRUPO.
    public int insertarGrupo(Grupo grupo) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.INSERTAR_GRUPO);
        p.setEntidad(grupo);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    
    public int modificarGrupo(Integer codGrupo, Grupo grupo) throws ExcepcionAlbumFamiliar{        
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.MODIFICAR_GRUPO);
        p.setIdEntidad(codGrupo);
        p.setEntidad(grupo);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE()!=null) {
            throw r.getE();
        }
        return registrosAfectados;
    }

    public int eliminarGrupo(Integer codGrupo) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.ELIMINAR_GRUPO);
        p.setIdEntidad(codGrupo);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    
    public Grupo leerGrupo(Integer codGrupo) throws ExcepcionAlbumFamiliar{
        Grupo grupo = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_GRUPO);
        p.setIdEntidad(codGrupo);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            grupo = (Grupo) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return grupo;
    }
    

    public ArrayList<Grupo> leerGrupos(LinkedHashMap<String,String> filtros, LinkedHashMap<String,String> ordenacion) throws ExcepcionAlbumFamiliar{
        ArrayList<Grupo> listaGrupos = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_GRUPOS);
        ArrayList<LinkedHashMap> argumentos = new ArrayList<>();
        argumentos.add(filtros);
        argumentos.add(ordenacion);
        p.setEntidad(argumentos);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            listaGrupos = (ArrayList<Grupo>) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return listaGrupos;
    }
    
    public int restaurarGrupo(Integer codGrupo) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.RESTAURAR_GRUPO);
        p.setIdEntidad(codGrupo);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    // MÉTODOS DE PETICIÓN PUBLICACION.
    public int insertarPublicacion(Publicacion publicacion) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.INSERTAR_PUBLICACION);
        p.setEntidad(publicacion);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    public int modificarPublicacion(Integer codPublicacion, Publicacion publicacion) throws ExcepcionAlbumFamiliar{        
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.MODIFICAR_PUBLICACION);
        p.setIdEntidad(codPublicacion);
        p.setEntidad(publicacion);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE()!=null) {
            throw r.getE();
        }
        return registrosAfectados;
    }

    public int eliminarPublicacion(Integer codPublicacion) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.ELIMINAR_PUBLICACION);
        p.setIdEntidad(codPublicacion);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    
    public Publicacion leerPublicacion(Integer codPublicacion) throws ExcepcionAlbumFamiliar{
        Publicacion publicacion = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_PUBLICACION);
        p.setIdEntidad(codPublicacion);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            publicacion = (Publicacion) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return publicacion;
    }
    

    public ArrayList<Publicacion> leerPublicaciones(LinkedHashMap<String,String> filtros, LinkedHashMap<String,String> ordenacion) throws ExcepcionAlbumFamiliar{
        ArrayList<Publicacion> listaPublicaciones = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_PUBLICACIONES);
        ArrayList<LinkedHashMap> argumentos = new ArrayList<>();
        argumentos.add(filtros);
        argumentos.add(ordenacion);
        p.setEntidad(argumentos);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            listaPublicaciones = (ArrayList<Publicacion>) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return listaPublicaciones;
    }
    
    // MÉTODOS DE PETICIÓN COMENTARIO.
    public int insertarComentario(Comentario comentario) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.INSERTAR_COMENTARIO);
        p.setEntidad(comentario);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    public int eliminarComentario(Integer codComentario) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.ELIMINAR_COMENTARIO);
        p.setIdEntidad(codComentario);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    public Comentario leerComentario(Integer codComentario) throws ExcepcionAlbumFamiliar{
        Comentario comentario = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_COMENTARIO);
        p.setIdEntidad(codComentario);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            comentario = (Comentario) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return comentario;
    }
    
    public ArrayList<Comentario> leerComentarios(LinkedHashMap<String,String> filtros, LinkedHashMap<String,String> ordenacion) throws ExcepcionAlbumFamiliar{
        ArrayList<Comentario> listaComentarios = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_COMENTARIOS);
        ArrayList<LinkedHashMap> argumentos = new ArrayList<>();
        argumentos.add(filtros);
        argumentos.add(ordenacion);
        p.setEntidad(argumentos);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            listaComentarios = (ArrayList<Comentario>) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return listaComentarios;
    }
    
    // MÉTODOS DE PETICIÓN SOLICITUD_ENTRADA_GRUPO.
    public int insertarSolicitudEntradaGrupo(SolicitudEntradaGrupo solicitudEntradaGrupo) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.INSERTAR_SOLICITUD_ENTRADA_GRUPO);
        p.setEntidad(solicitudEntradaGrupo);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    public int eliminarSolicitudEntradaGrupo(Integer codUsuario, Integer codGrupo) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.ELIMINAR_SOLICITUD_ENTRADA_GRUPO);
        ArrayList<Integer> idEntidades = new ArrayList<>();
        idEntidades.add(codUsuario);
        idEntidades.add(codGrupo);
        p.setIdEntidad(idEntidades);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    public SolicitudEntradaGrupo leerSolicitudEntradaGrupo(Integer codUsuario, Integer codGrupo) throws ExcepcionAlbumFamiliar{
        SolicitudEntradaGrupo solicitudEntradaGrupo = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_SOLICITUD_ENTRADA_GRUPO);
        ArrayList<Integer> idEntidades = new ArrayList<>();
        idEntidades.add(codUsuario);
        idEntidades.add(codGrupo);
        p.setIdEntidad(idEntidades);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            solicitudEntradaGrupo = (SolicitudEntradaGrupo) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return solicitudEntradaGrupo;
    }
    
    public ArrayList<SolicitudEntradaGrupo> leerSolicitudesEntradaGrupo(LinkedHashMap<String,String> filtros, LinkedHashMap<String,String> ordenacion) throws ExcepcionAlbumFamiliar{
        ArrayList<SolicitudEntradaGrupo> listaSolicitudesEntradaGrupo = null;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.LEER_SOLICITUDES_ENTRADA_GRUPO);
        ArrayList<LinkedHashMap> argumentos = new ArrayList<>();
        argumentos.add(filtros);
        argumentos.add(ordenacion);
        p.setEntidad(argumentos);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getEntidad() != null) {
            listaSolicitudesEntradaGrupo = (ArrayList<SolicitudEntradaGrupo>) r.getEntidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return listaSolicitudesEntradaGrupo;
    }
    
    // MÉTODOS DE PETICIÓN USUARIO_INTEGRA_GRUPO.
    public int insertarUsuarioIntegraGrupo(UsuarioIntegraGrupo usuarioIntegraGrupo) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.INSERTAR_USUARIO_INTEGRA_GRUPO);
        p.setEntidad(usuarioIntegraGrupo);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    public int eliminarUsuarioIntegraGrupo(Integer codUsuario, Integer codGrupo) throws ExcepcionAlbumFamiliar{
        int registrosAfectados = 0;
        conectar();
        Peticion p = new Peticion();
        p.setIdOperacion(Operaciones.ELIMINAR_USUARIO_INTEGRA_GRUPO);
        ArrayList<Integer> idEntidades = new ArrayList<>();
        idEntidades.add(codUsuario);
        idEntidades.add(codGrupo);
        p.setIdEntidad(idEntidades);
        Respuesta r = gestionarComunicacion(p);
        desconectar();
        if (r.getCantidad() != null) {
            registrosAfectados = r.getCantidad();
        }
        else if (r.getE() != null) {
            throw r.getE();
        }
        return registrosAfectados;
    }
    
    public void manejadorIOException(IOException ex) throws ExcepcionAlbumFamiliar{
        ExcepcionAlbumFamiliar e = new ExcepcionAlbumFamiliar();
        e.setMensajeUsuario("Error en la comunicación. Consulte con el administrador.");
        e.setMensajeAdministrador(ex.getMessage());
        throw e;
    }
    
    public void manejadorClassNotFoundException(ClassNotFoundException ex) throws ExcepcionAlbumFamiliar{
        ExcepcionAlbumFamiliar e = new ExcepcionAlbumFamiliar();
        e.setMensajeUsuario("Error general del sistema. Consulte con el administrador.");
        e.setMensajeAdministrador(ex.getMessage());
        throw e;
    }
    
    
}

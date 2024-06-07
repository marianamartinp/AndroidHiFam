package driveapi;

// POJO para retornar datos de Google Drive
public class DatosArchivo {
    private String archivoId;
    private String nombre;

    public DatosArchivo(String archivoId, String nombre) {
        this.archivoId = archivoId;
        this.nombre = nombre;
    }

    public String getArchivoId() {
        return archivoId;
    }

    public String getNombre() {
        return nombre;
    }
}

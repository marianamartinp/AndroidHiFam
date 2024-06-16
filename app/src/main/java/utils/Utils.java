package utils;

import android.icu.text.SimpleDateFormat;
import android.util.Patterns;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.mindrot.jbcrypt.BCrypt;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;

// La clase Utils contiene métodos útiles para diversas tareas de la aplicación
public class Utils {

    // Enumeración que define los posibles resultados de la validación de un EditText
    public enum EnumValidacionEditText {
        VALIDO, FORMATO_NO_VALIDO, ERROR, VACIO, EN_USO
    }

    // Método para hashear una contraseña utilizando la librería BCrypt
    public static String hashearContrasenya(String contrasenyaUsuario) {
        return BCrypt.hashpw(contrasenyaUsuario, BCrypt.gensalt());
    }

    // Método para verificar si una contraseña coincide con su versión hasheada
    public static boolean comprobarContrasenya(String contrasenyaUsuario, String contrasenyaHasheada) {
        return BCrypt.checkpw(contrasenyaUsuario, contrasenyaHasheada);
    }

    // Método para validar un número de teléfono utilizando la librería de Google PhoneNumberUtil
    public static boolean validarNumeroDeTelefono(String telefono) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber telefonoProto = phoneUtil.parse(telefono, "");
            int codigoPais = telefonoProto.getCountryCode();
            String codigoRegion = phoneUtil.getRegionCodeForCountryCode(codigoPais);

            if (codigoRegion == null) {
                System.err.println("Código de país no válido: " + codigoPais);
                return false;
            }
            telefonoProto = phoneUtil.parse(telefono, codigoRegion);
            return phoneUtil.isValidNumber(telefonoProto);
        } catch (NumberParseException e) {
            System.err.println("NumberParseException lanzada: " + e.toString());
            return false;
        }
    }

    // Método para eliminar espacios y caracteres especiales de un número de teléfono
    public static String limpiarNumeroDeTelefono(String numeroTelefono) {
        return numeroTelefono.replaceAll("[\\s\\-\\(\\)\\.]", "");
    }

    // Método para validar una contraseña utilizando expresiones regulares
    public static boolean validarContrasenya(String contrasenya) {
        String regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(regexp);
        if (null == contrasenya || contrasenya.isEmpty()) {
            return false;
        }
        return pattern.matcher(contrasenya).matches();
    }

    // Método para parsear una cadena de fecha a un objeto Date
    public static Date parsearFechaADate(String fecha) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            return sdf.parse(fecha);
        } catch (ParseException e) {
            System.err.println("ParseException was thrown: " + e.toString());
            return null;
        }
    }

    // Método para parsear un objeto Date a una cadena de fecha
    public static String parsearDateAString(Date fecha) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        return sdf.format(fecha);
    }

    // Método para verificar si una dirección de correo electrónico es válida
    public static boolean esEmailValido(CharSequence email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}

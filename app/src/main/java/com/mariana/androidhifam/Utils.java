package com.mariana.androidhifam;
import android.icu.text.SimpleDateFormat;
import android.util.Patterns;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.mindrot.jbcrypt.BCrypt;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pojosalbumfamiliar.ExcepcionAlbumFamiliar;

public class Utils {

    public enum EnumValidacionEditText {
        VALIDO, FORMATO_NO_VALIDO, ERROR, VACIO, EN_USO
    }

    public static String hashearContrasenya(String contrasenyaUsuario) {
        return BCrypt.hashpw(contrasenyaUsuario, BCrypt.gensalt());
    }

    public static boolean comprobarContrasenya(String contrasenyaUsuario, String contrasenyaHasheada) {
        return BCrypt.checkpw(contrasenyaUsuario, contrasenyaHasheada);
    }

    public static boolean validarNumeroDeTelefono(String telefono) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            // Parse the number with an empty region code to infer the country code
            Phonenumber.PhoneNumber telefonoProto = phoneUtil.parse(telefono, "");

            // Get the country code
            int codigoPais = telefonoProto.getCountryCode();

            // Get the region code for the country code
            String codigoRegion = phoneUtil.getRegionCodeForCountryCode(codigoPais);

            if (codigoRegion == null) {
                System.err.println("Invalid country code: " + codigoPais);
                return false;
            }
            // Validate the number with the inferred region code
            telefonoProto = phoneUtil.parse(telefono, codigoRegion);
            return phoneUtil.isValidNumber(telefonoProto); // returns true if valid, false otherwise
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
            return false;
        }
    }

    public static String limpiarNumeroDeTelefono(String numeroTelefono) {
        return numeroTelefono.replaceAll("[\\s\\-\\(\\)\\.]", "");
    }

    public static boolean validarContrasenya(String contrasenya) {
        String regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(regexp);
        if (null == contrasenya || contrasenya.isEmpty()) {
            return false;
        }
        return pattern.matcher(contrasenya).matches();
    }

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

    public static String parsearDateAString(Date fecha) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        return sdf.format(fecha);
    }

    public static boolean esEmailValido(CharSequence email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}

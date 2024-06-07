package utils;

import android.text.InputFilter;
import android.text.Spanned;

public class FiltroNombreUsuario implements InputFilter {

    // Este método es llamado cuando se está ingresando texto en un EditText.
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String caracteresBloqueados = "[^a-zA-Z0-9_]";

        // Verifica si el texto ingresado coincide con el conjunto de caracteres bloqueados.
        if (source.toString().matches(caracteresBloqueados)) {
            // Si coincide, devuelve una cadena vacía para evitar que se ingrese el carácter bloqueado.
            return "";
        }
        return null;
    }
}
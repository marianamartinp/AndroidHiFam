package com.mariana.androidhifam;

import android.util.Patterns;

public class ValidadorEmail {
    public static boolean esEmailValido(CharSequence email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
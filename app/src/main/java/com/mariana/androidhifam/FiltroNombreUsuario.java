package com.mariana.androidhifam;

import android.text.InputFilter;
import android.text.Spanned;

public class FiltroNombreUsuario implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String blockCharacterSet = "[^a-zA-Z0-9_]";

        if (source.toString().matches(blockCharacterSet)) {
            return "";
        }
        return null;
    }
}
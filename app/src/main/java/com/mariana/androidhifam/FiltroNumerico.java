package com.mariana.androidhifam;

import android.text.InputFilter;
import android.text.Spanned;

public class FiltroNumerico implements InputFilter {
    private final String regex = "[0-9]+";
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int length = dest.length() - (dend - dstart) + (end - start);
        if (length > 3) {
            return "";
        }

        for (int i = start; i < end; i++) {
            if (!Character.toString(source.charAt(i)).matches(regex)) {
                return "";
            }
        }
        return null;
    }
}
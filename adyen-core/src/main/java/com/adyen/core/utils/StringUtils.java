package com.adyen.core.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Locale;

public final class StringUtils {

    public static Locale getLocale(Context context) {
        if (Build.VERSION.SDK_INT >= 24) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }

    /**
     * Check whether a string is empty or null.
     * Note: All leading and trailing spaces are removed
     * @param value the sting to check
     * @return true if the string is null or empty (excluding spaces)
     */
    public static boolean isEmptyOrNull(String value) {
        return (value == null) || isEmpty(value);
    }

    private static boolean isEmpty(@NonNull String value) {
        return "".equals(value.trim());
    }

    private StringUtils() {
        // Private Constructor
    }
}

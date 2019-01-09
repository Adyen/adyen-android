/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 3/12/2018.
 */

package com.adyen.checkout.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * Utility class for {@link Locale} associated methods.
 */
public final class LocaleUtil {

    /**
     * Get the default device locale.
     *
     * @param context The current context.
     * @return User current {@link Locale}.
     */
    @NonNull
    public static Locale getLocale(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().locale;
        } else {
            return context.getResources().getConfiguration().getLocales().get(0);
        }
    }

    private LocaleUtil() {
        throw new IllegalStateException("No instances.");
    }
}

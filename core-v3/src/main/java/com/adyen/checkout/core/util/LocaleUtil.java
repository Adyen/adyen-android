/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

package com.adyen.checkout.core.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.exeption.NoConstructorException;

import java.util.Locale;

/**
 * Utility class to use {@link Locale}.
 */
public final class LocaleUtil {

    private static final String TAG_SEPARATOR = "_";
    private static final int EXPECTED_TAG_ROOTS = 2;

    /**
     * Get the current user Locale.
     * @param context The context
     * @return The user Locale
     */
    @NonNull
    public static Locale getLocale(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

    /**
     * Gets the language tag from a Locale.
     *
     * @param locale The locale.
     * @return The tag of the locale.
     */
    @NonNull
    public static String toLanguageTag(@NonNull Locale locale) {
        return locale.getLanguage() + TAG_SEPARATOR + locale.getCountry();
    }

    /**
     * Creates a Locale instance for a specific language tag.
     *
     * @param tag The tag of the language.
     * @return The Locale associated with that Tag
     * @throws IllegalArgumentException If the Tag is not a valid language tag.
     */
    @NonNull
    public static Locale fromLanguageTag(@NonNull String tag) throws IllegalArgumentException {
        final String[] language = tag.split(TAG_SEPARATOR);
        if (language.length == EXPECTED_TAG_ROOTS) {
            return new Locale(language[0], language[1]);
        }
        throw new IllegalArgumentException("Unexpected language tag - " + tag);
    }

    private LocaleUtil() {
        throw new NoConstructorException();
    }
}

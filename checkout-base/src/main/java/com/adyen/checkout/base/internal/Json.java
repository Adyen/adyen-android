package com.adyen.checkout.base.internal;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 05/08/2017.
 */
public final class Json {
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");

    @NonNull
    public static Map<String, String> getDefaultHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");

        return headers;
    }

    @NonNull
    public static String serializeDate(@NonNull Date date) {
        return getDateFormat().format(date);
    }

    @NonNull
    private static DateFormat getDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);
        dateFormat.setTimeZone(TIME_ZONE);

        return dateFormat;
    }

    private Json() {
        throw new IllegalStateException("No instances.");
    }
}

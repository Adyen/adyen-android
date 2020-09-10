/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 22/11/2018.
 */

package com.adyen.checkout.base.encoding;

import androidx.annotation.NonNull;
import android.util.Base64;

import com.adyen.checkout.core.exception.NoConstructorException;

import java.nio.charset.Charset;

public final class Base64Encoder {

    private static final String UTF_8 = "UTF-8";
    private static final Charset DEFAULT_CHARSET = Charset.isSupported(UTF_8) ? Charset.forName(UTF_8) : Charset.defaultCharset();

    @NonNull
    public static String encode(@NonNull String decodedData) {
        return encode(decodedData, Base64.DEFAULT);
    }

    @NonNull
    public static String encode(@NonNull String decodedData, int flags) {
        final byte[] decodedBytes = decodedData.getBytes(DEFAULT_CHARSET);
        return Base64.encodeToString(decodedBytes, flags);
    }

    @NonNull
    public static String decode(@NonNull String encodedData) {
        return decode(encodedData, Base64.DEFAULT);
    }

    @NonNull
    public static String decode(@NonNull String encodedData, int flags) {
        final byte[] decodedBytes = Base64.decode(encodedData, flags);
        return new String(decodedBytes, DEFAULT_CHARSET);
    }

    private Base64Encoder() {
        throw new NoConstructorException();
    }
}

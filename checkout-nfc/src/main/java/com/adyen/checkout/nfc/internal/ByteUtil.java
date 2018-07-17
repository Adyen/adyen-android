package com.adyen.checkout.nfc.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 30/08/2017.
 */
public final class ByteUtil {
    public static final Charset NFC_CHARSET = Charset.forName("ISO-8859-1");

    private static final String HEX_SUBSCRIPT_FORMAT = "[%s]\u2081\u2086";

    private static final String BYTE_HEX = "%02X ";

    private static final String READABLE_REGEX = "[a-zA-Z0-9. ]{2,}";

    private static final String NULL = "null";

    @NonNull
    public static String bytesToHex(@Nullable byte... bytes) {
        if (bytes == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format(BYTE_HEX, b));
        }

        return sb.toString().trim();
    }

    public static String bytesToHexFormatted(@Nullable byte... bytes) {
        return String.format(Locale.US, HEX_SUBSCRIPT_FORMAT, bytesToHex(bytes));
    }

    @NonNull
    public static String bytesToReadable(@Nullable byte... bytes) {
        String readableValue = bytes != null ? new String(bytes, ByteUtil.NFC_CHARSET) : null;

        if (readableValue != null) {
            if (readableValue.matches(READABLE_REGEX)) {
                return readableValue;
            } else {
                return bytesToHexFormatted(bytes);
            }
        } else {
            return NULL;
        }
    }

    public static byte setBitInByte(byte value, int bitIndex, boolean set) {
        return set ? (byte) (value | (1 << bitIndex)) : (byte) (value & ~(1 << bitIndex));
    }

    private ByteUtil() {
        throw new IllegalStateException("No instances.");
    }
}

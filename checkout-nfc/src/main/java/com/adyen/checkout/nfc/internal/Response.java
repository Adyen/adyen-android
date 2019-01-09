/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 30/08/2017.
 */

package com.adyen.checkout.nfc.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

public final class Response extends ByteParsable {
    public static final byte SW1_COMMAND_NOT_ALLOWED = (byte) 0x69;

    public static final byte SW1_WRONG_LENGTH = (byte) 0x67;

    public static final byte SW1_INVALID_LENGTH = (byte) 0x6C;

    public static final byte SW1_SUCCESS = (byte) 0x90;

    @Nullable
    public static Response parseResponse(@NonNull byte[] bytes) {
        Response response = new Response();
        return response.parse(bytes) > 0 ? response : null;
    }

    @Override
    public int parse(@NonNull byte[] bytes) {
        int length = bytes.length;

        if (length >= 2) {
            setBytes(bytes);
            return length;
        } else {
            return 0;
        }
    }

    @NonNull
    public byte[] getValue() {
        byte[] bytes = getBytes();
        int length = Math.max(0, bytes.length - 2);
        return Arrays.copyOfRange(bytes, 0, length);
    }

    public byte getSw1() {
        byte[] bytes = getBytes();
        return bytes[bytes.length - 2];
    }

    public byte getSw2() {
        byte[] bytes = getBytes();
        return bytes[bytes.length - 1];
    }
}

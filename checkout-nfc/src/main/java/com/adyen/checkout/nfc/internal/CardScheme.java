package com.adyen.checkout.nfc.internal;

import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 06/09/2017.
 */
public enum CardScheme {
    VISA(
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x03},
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03},
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x10, (byte) 0x10},
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x10, (byte) 0x20},
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x98, (byte) 0x08, (byte) 0x48},
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x98, (byte) 0x08, (byte) 0x48}
    ),
    MASTERCARD(
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04},
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05},
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x10, (byte) 0x10},
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x30, (byte) 0x60}
    ),
    AMERICAN_EXPRESS(
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x25},
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x50, (byte) 0x10, (byte) 0x40, (byte) 0x2},
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x91, (byte) 0x01, (byte) 0x0}
    ),
    DISCOVER(
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x52, (byte) 0x30, (byte) 0x10}
    ),
    DINERS(
            new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x52, (byte) 0x30, (byte) 0x10}
    );

    private byte[][] mApplicationIdentifiers;

    CardScheme(byte[]... applicationIdentifiers) {
        mApplicationIdentifiers = applicationIdentifiers;
    }

    @NonNull
    public byte[][] getApplicationIdentifiers() {
        return Arrays.copyOf(mApplicationIdentifiers, mApplicationIdentifiers.length);
    }
}

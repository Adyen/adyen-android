/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 07/09/2017.
 */

package com.adyen.checkout.nfc.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.Locale;

@SuppressWarnings("checkstyle:MagicNumber")
public class Length extends ByteParsable {
    private int mIntValue;

    @Nullable
    public static Length parseLength(@NonNull byte[] bytes) {
        Length length = new Length();
        return length.parse(bytes) > 0 ? length : null;
    }

    @Override
    public int parse(@NonNull byte[] bytes) {
        if (bytes.length == 0) {
            return 0;
        }

        int index = 0;
        int length = (bytes[index++] & 0xFF);

        if (length >= 0x80) {
            int remainingLengthBytes = (length & 0x7F);

            if (remainingLengthBytes > 2) {
                return 0;
            }

            length = 0;

            for (int i = 0; i < remainingLengthBytes; i++) {
                length <<= 8;

                if (index < bytes.length) {
                    length |= (bytes[index++] & 0xFF);
                } else {
                    return 0;
                }
            }
        }

        setBytes(Arrays.copyOfRange(bytes, 0, index));
        mIntValue = length;
        return index;
    }

    @Override
    protected void clear() {
        super.clear();

        mIntValue = 0;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%s (%d)", super.toString(), mIntValue);
    }

    public int intValue() {
        return mIntValue;
    }
}

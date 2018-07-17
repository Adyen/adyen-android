package com.adyen.checkout.nfc.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 06/09/2017.
 */
public class Tag extends ByteParsable {
    private static final int MULTIBYTE_TAG_MASK = 0x1F;

    @Nullable
    public static Tag parseTag(@NonNull byte... bytes) {
        Tag tag = new Tag();
        return tag.parse(bytes) > 0 ? tag : null;
    }

    @Override
    public int parse(@NonNull byte[] bytes) {
        byte[] result = new byte[3];
        int index = 0;

        while (index < bytes.length) {
            byte tag = (byte) (bytes[index] & 0xFF);

            if (tag != 0x00) {
                result[index++] = tag;

                if ((tag & MULTIBYTE_TAG_MASK) != MULTIBYTE_TAG_MASK) {
                    break;
                }
            } else {
                break;
            }
        }

        setBytes(Arrays.copyOfRange(result, 0, index));

        return index;
    }
}

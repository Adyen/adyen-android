package com.adyen.checkout.nfc.internal;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 07/09/2017.
 */
public final class Parser {
    private static final String ERROR_MESSAGE = "Error parsing %s.";

    @NonNull
    public static <B extends ByteParsable> List<B> parseList(@NonNull byte[] bytes, @NonNull Class<B> itemClass) {
        List<B> result = new ArrayList<>();

        int offset = 0;
        int length = bytes.length;

        try {
            while (offset < length) {
                B byteParsable = itemClass.newInstance();
                int parsed = byteParsable.parse(Arrays.copyOfRange(bytes, offset, length));

                if (parsed > 0) {
                    result.add(byteParsable);
                    offset += parsed;
                } else {
                    break;
                }
            }
        } catch (InstantiationException e) {
            String message = String.format(ERROR_MESSAGE, itemClass.getSimpleName());
            throw new RuntimeException(message, e);
        } catch (IllegalAccessException e) {
            String message = String.format(ERROR_MESSAGE, itemClass.getSimpleName());
            throw new RuntimeException(message, e);
        }

        return result;
    }

    private Parser() {
        throw new IllegalStateException("No instances.");
    }
}

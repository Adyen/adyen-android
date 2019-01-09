/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 05/11/2018.
 */

package com.adyen.checkout.util.internal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.SpannableStringBuilder;

public final class TextFormat {
    //%1$s
    private static final int INDEXED_STRING_ARG_LENGTH = 4;
    //%s
    private static final int STRING_ARG_LENGTH = 2;

    @NonNull
    public static CharSequence format(@NonNull Context context, @StringRes int resId, @Nullable Object... formatArgs) {
        String string = context.getString(resId, formatArgs);
        SpannableStringBuilder builder = new SpannableStringBuilder(context.getText(resId));

        if (formatArgs != null) {
            for (Object formatArg : formatArgs) {
                String stringArg = formatArg.toString();
                int indexStart = string.indexOf(stringArg);
                // TODO: 05/11/2018 Improve replacing.
                int indexEnd = indexStart + (Character.isDigit(builder.charAt(indexStart + 1)) ? INDEXED_STRING_ARG_LENGTH : STRING_ARG_LENGTH);
                builder.replace(indexStart, indexEnd, formatArg instanceof CharSequence ? ((CharSequence) formatArg) : formatArg.toString());
            }
        }

        return builder;
    }

    private TextFormat() {
        throw new IllegalStateException("No instances.");
    }
}

package com.adyen.checkout.core.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Locale;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 24/11/2017.
 */
public final class SearchUtil {
    public static boolean matches(@Nullable CharSequence query, @NonNull CharSequence string) {
        if (TextUtils.isEmpty(query)) {
            return true;
        }

        String[] normalizedQueryParts = query.toString().toLowerCase(Locale.US).split("\\s+");
        String[] normalizedItemParts = string.toString().toLowerCase(Locale.US).split("\\s+");

        outer: for (String normalizedQueryPart : normalizedQueryParts) {
            for (String normalizedItemPart : normalizedItemParts) {
                if (normalizedItemPart.contains(normalizedQueryPart)) {
                    continue outer;
                }
            }

            return false;
        }

        return true;
    }

    public static boolean anyMatches(@Nullable CharSequence query, @NonNull CharSequence... strings) {
        for (CharSequence string : strings) {
            if (matches(query, string)) {
                return true;
            }
        }

        return false;
    }

    public static boolean allMatch(@Nullable CharSequence query, @NonNull CharSequence... strings) {
        for (CharSequence string : strings) {
            if (!matches(query, string)) {
                return false;
            }
        }

        return true;
    }

    private SearchUtil() {
        throw new IllegalStateException("No instances.");
    }
}

/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 31/01/2018.
 */

package com.adyen.checkout.util.sepadirectdebit;

import android.support.annotation.Nullable;

public final class HolderName {
    private static final String REGEX_AT_LEAST_PARTIAL = "[\\w0-9/?!:().,'+\\- ]*";

    private static final String REGEX_VALID_IF_MATCHED_PARTIAL = ".*[\\w0-9].*[\\w0-9].*[\\w0-9].*";

    /**
     * Check whether a given holder name is valid.
     *
     * @param holderName The holder name to check.
     * @return {@code true} if the holder name is valid.
     */
    public static boolean isValid(@Nullable String holderName) {
        if (holderName == null) {
            return false;
        } else {
            String trimmedHolderName = holderName.trim();

            return trimmedHolderName.matches(REGEX_AT_LEAST_PARTIAL) && trimmedHolderName.matches(REGEX_VALID_IF_MATCHED_PARTIAL);
        }
    }

    /**
     * Check whether a given holder name contains only valid characters, i.e. it is not invalid.
     *
     * @param holderName The holder name to check.
     * @return {@code true} if the holder name contains only valid characters.
     */
    public static boolean isPartial(@Nullable String holderName) {
        return holderName != null
                && holderName.trim().matches(REGEX_AT_LEAST_PARTIAL);
    }

    private HolderName() {
        throw new IllegalStateException("No instances.");
    }
}

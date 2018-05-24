package com.adyen.core.models.paymentdetails;

import android.support.annotation.Nullable;

import java.util.Collection;

public final class InputDetailsUtil {

    private InputDetailsUtil() {

    }

    /**
     * Check if the collection of {@link InputDetail} contains an {@link InputDetail} with a specific key.
     * @param inputDetails Given inputDetails.
     * @param key Given key.
     * @return true if the collection contains an {@link InputDetail} with the given key.
     */
    public static boolean containsKey(Collection<InputDetail> inputDetails, String key) {
        for (InputDetail inputDetail : inputDetails) {
            if (inputDetail.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a specific {@link InputDetail} with a given key.
     * @param inputDetails Collection of {@link InputDetail}.
     * @param key Key of {@link InputDetail} that we want to retrieve.
     * @return {@link InputDetail} if available, otherwise null.
     */
    public static @Nullable InputDetail getInputDetail(Collection<InputDetail> inputDetails, final String key) {
        for (InputDetail inputDetail : inputDetails) {
            if (inputDetail.getKey().equals(key)) {
                return inputDetail;
            }
        }
        return null;
    }
}

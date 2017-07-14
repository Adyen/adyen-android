package com.adyen.core.models.paymentdetails;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for collecting the required PaymentDetails from the shopper to make the payment.
 */
public class PaymentDetails implements Serializable {

    @NonNull private Map<String, InputDetail> inputDetails = new HashMap<>();

    public PaymentDetails(Collection<InputDetail> inputDetails) {
        for (InputDetail inputDetail : inputDetails) {
            this.inputDetails.put(inputDetail.getKey(), inputDetail);
        }
    }

    /**
     * Returns true if this {@link PaymentDetails} instance contains an {@link InputDetail} with a specific key.
     * @param key The key to check.
     * @return True iff an {@link InputDetail} with this key is contained.
     */
    public boolean hasKey(final String key) {
        return this.inputDetails.containsKey(key);
    }

    /**
     * Check if there are any {@link InputDetail} to be filled out.
     * @return true if there are no InputDetails that can be filled out.
     */
    public boolean isEmpty() {
        return inputDetails.isEmpty();
    }

    /**
     * Fills an InputDetails with a key value pair.
     * @param key {@link InputDetail} with this key will be filled.
     * @param value Value to be saved.
     * @return true if the setting of this key was successful.
     */
    public boolean fill(final String key, final String value) {
        InputDetail inputDetail = this.inputDetails.get(key);
        if (inputDetail == null) {
            return false;
        }
        return inputDetail.fill(value);
    }

    /**
     * Fills an InputDetails with a key value pair.
     * @param key {@link InputDetail} with this key will be filled.
     * @param value The boolean value to be saved
     * @return true if the setting of this key was successful.
     */
    public boolean fill(final String key, final boolean value) {
        InputDetail inputDetail = this.inputDetails.get(key);
        if (inputDetail == null) {
            return false;
        }
        return inputDetail.fill(value);
    }

    public boolean isFilled() {
        for (InputDetail inputDetail : inputDetails.values()) {
            if (!inputDetail.isFilled() && !inputDetail.isOptional()) {
                return false;
            }
        }
        return true;
    }

    public @NonNull Collection<InputDetail> getInputDetails() {
        return this.inputDetails.values();
    }

    public @Nullable InputDetail getInputDetail(final String key) {
        return inputDetails.get(key);
    }

}

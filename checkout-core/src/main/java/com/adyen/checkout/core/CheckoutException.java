package com.adyen.checkout.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 03/05/2018.
 */
public final class CheckoutException extends Exception {
    private String mPayload;

    private boolean mFatal;

    private CheckoutException(@NonNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    /**
     * @return The payload that can be submitted to the Adyen payments platform to retrieve further information about the error.
     * @see <a href="https://docs.adyen.com/developers/checkout/android-sdk/quick-start-android/verify-payment-result-android">
     *     Verifying a payment result</a>
     */
    @Nullable
    public String getPayload() {
        return mPayload;
    }

    /**
     * @return Whether the error is fatal for the checkout process.
     */
    public boolean isFatal() {
        return mFatal;
    }

    public static final class Builder {
        private CheckoutException mCheckoutException;

        public Builder(@NonNull String message, @Nullable Throwable cause) {
            mCheckoutException = new CheckoutException(message, cause);
        }

        @NonNull
        public Builder setPayload(@Nullable String payload) {
            mCheckoutException.mPayload = payload;

            return this;
        }

        @NonNull
        public Builder setFatal(boolean fatal) {
            mCheckoutException.mFatal = fatal;

            return this;
        }

        @NonNull
        public CheckoutException build() {
            return mCheckoutException;
        }
    }
}

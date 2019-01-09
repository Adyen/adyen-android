/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 11/06/2018.
 */

package com.adyen.checkout.core;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;

/**
 * Interface offering functionality to handle the details for a {@link PaymentMethod}.
 */
public interface PaymentMethodHandler {
    /**
     * Result code for {@link Activity#onActivityResult(int, int, Intent)} indicating that the operation succeeded.
     */
    int RESULT_CODE_OK = Activity.RESULT_OK;

    /**
     * Result code for {@link Activity#onActivityResult(int, int, Intent)} indicating that the operation was canceled.
     */
    int RESULT_CODE_CANCELED = Activity.RESULT_CANCELED;

    /**
     * Result code for {@link Activity#onActivityResult(int, int, Intent)} indicating that an error occurred.
     */
    int RESULT_CODE_ERROR = Activity.RESULT_FIRST_USER;

    /**
     * Result key for {@link Activity#onActivityResult(int, int, Intent)} to retrieve a {@link PaymentResult} with from the result {@link Intent}
     * in case {@code resultCode == RESULT_CODE_OK}.
     */
    @NonNull
    String RESULT_PAYMENT_RESULT = "RESULT_PAYMENT_RESULT";

    /**
     * Result key for {@link Activity#onActivityResult(int, int, Intent)} to retrieve a {@link CheckoutException} with from the result {@link Intent}
     * in case {@code resultCode != RESULT_CODE_OK}.
     */
    @NonNull
    String RESULT_CHECKOUT_EXCEPTION = "RESULT_CHECKOUT_EXCEPTION";

    /**
     * Handles the retrieval of the {@link com.adyen.checkout.core.model.PaymentMethodDetails PaymentMethodDetails} from the shopper for the given
     * {@link PaymentMethod}.
     *
     * @param activity The current {@link Activity}.
     * @param requestCode The request code used for {@link Activity#startActivityForResult(Intent, int)}.
     */
    void handlePaymentMethodDetails(@NonNull Activity activity, int requestCode);

    /**
     * Factory interface for a {@link PaymentMethodHandler}.
     */
    interface Factory {
        /**
         * Checks whether the {@link PaymentMethodHandler} provided by the {@link Factory} supports the a given {@link PaymentMethod}.
         *
         * @param application The current {@link Application}.
         * @param paymentMethod The {@link PaymentMethod} to be checked whether it is supported.
         * @return Whether the {@link PaymentMethod} is supported by the {@link PaymentMethodHandler} provided by the {@link PaymentMethodHandler}.
         */
        boolean supports(@NonNull Application application, @NonNull PaymentMethod paymentMethod);

        /**
         * Checks whether the given {@link PaymentMethod} is available to the shopper. Calling this method might perform asynchronous requests to
         * determine whether this is the case.
         *
         * @param application The current {@link Application}.
         * @param paymentSession The current {@link PaymentSession}.
         * @param paymentMethod The {@link PaymentMethod} to be checked whether it is available to the shopper.
         * @return Whether the {@link PaymentMethod} is available to the shopper.
         */
        boolean isAvailableToShopper(@NonNull Application application, @NonNull PaymentSession paymentSession, @NonNull PaymentMethod paymentMethod);
    }

    /**
     * Utility class for {@link PaymentMethodHandler PaymentMethodHandlers}.
     */
    final class Util {
        /**
         * Get the {@link PaymentResult} from the result data of {@link Activity#onActivityResult(int, int, Intent)}.
         *
         * @param data The result {@link Intent}.
         * @return The {@link PaymentResult}, or {@code null} if not present.
         */
        @Nullable
        public static PaymentResult getPaymentResult(@Nullable Intent data) {
            return data != null
                    ? data.<PaymentResult>getParcelableExtra(RESULT_PAYMENT_RESULT)
                    : null;
        }

        /**
         * Get the {@link CheckoutException} from the result data of {@link Activity#onActivityResult(int, int, Intent)}.
         *
         * @param data The result {@link Intent}.
         * @return The {@link CheckoutException}, or {@code null} if not present.
         */
        @Nullable
        public static CheckoutException getCheckoutException(@Nullable Intent data) {
            return data != null
                    ? (CheckoutException) data.getSerializableExtra(RESULT_CHECKOUT_EXCEPTION)
                    : null;
        }

        private Util() {
            throw new IllegalStateException("No instances.");
        }
    }
}

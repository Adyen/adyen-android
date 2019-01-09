/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 02/11/2017.
 */

package com.adyen.checkout.core.model;

import com.adyen.checkout.base.internal.JsonObject.SerializedName;

public enum PaymentResultCode {
    /**
     * Indicates that the payment has not reached a final status yet. This may be the case if either the systems that provide the final status
     * information are unavailable, or the shopper needs to take further actions.
     *
     * @see <a href="https://docs.adyen.com/developers/development-resources/payments-with-pending-status">Payments with pending status</a>
     */
    PENDING,
    /**
     * Indicates that all relevant data for the payment has been received and that the payment will be processed. This is the initial state for all
     * payments.
     */
    RECEIVED,
    /**
     * Indicates that the payment authorisation has been completed successfully.
     */
    @SerializedName("authorised")
    AUTHORIZED,
    /**
     * Indicates that an error occurred while processing the payment.
     */
    ERROR,
    /**
     * Indicates that the payment was refused.
     */
    REFUSED,
    /**
     * Indicates that the payment was cancelled by either the shopper or the merchant before processing was completed.
     */
    CANCELLED
}

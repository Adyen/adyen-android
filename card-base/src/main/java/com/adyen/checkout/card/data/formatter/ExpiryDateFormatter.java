/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 19/3/2019.
 */

package com.adyen.checkout.card.data.formatter;

import android.support.annotation.NonNull;

public interface ExpiryDateFormatter {
    /**
     * Formats an expiry date.
     *
     * @param expiryMonth The expiry month.
     * @param expiryYear  The expiry year.
     * @return The formatted expiry date.
     */
    @NonNull
    String formatExpiryDate(int expiryMonth, int expiryYear);

    /**
     * Formats an expiry date.
     *
     * @param expiryDate The expiry date.
     * @return The formatted expiry date.
     */
    @NonNull
    String formatExpiryDate(@NonNull String expiryDate, @NonNull String prevExpiryDate);
}

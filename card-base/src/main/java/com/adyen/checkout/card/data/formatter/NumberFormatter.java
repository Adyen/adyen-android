/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 19/3/2019.
 */

package com.adyen.checkout.card.data.formatter;

import android.support.annotation.NonNull;

public interface NumberFormatter {
    @NonNull
    String unformatNumber(@NonNull String number);

    /**
     * Formats a card number with spaces.
     *
     * @param number The card number to format.
     * @return The formatted card number.
     */
    @NonNull
    String formatNumber(@NonNull String number);

    /**
     * Mask a card number for displaying it in the user interface.
     *
     * @param number The card number.
     * @return The masked card number.
     */
    @NonNull
    String maskNumber(@NonNull String number);
}

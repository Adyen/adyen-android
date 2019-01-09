/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 07/02/2018.
 */

package com.adyen.checkout.core.card;

import android.support.annotation.NonNull;
import android.text.TextWatcher;
import android.widget.EditText;

public interface CardFormatter {
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

    /**
     * Attach an as-you-type formatter to an {@link EditText} to format input to a card number.
     *
     * @param editText The {@link EditText} to attach the formatter to.
     * @return The attached formatter in form of a {@link TextWatcher}.
     */
    @NonNull
    TextWatcher attachAsYouTypeNumberFormatter(@NonNull EditText editText);

    /**
     * Formats an expiry date.
     *
     * @param expiryMonth The expiry month.
     * @param expiryYear The expiry year.
     * @return The formatted expiry date.
     */
    @NonNull
    String formatExpiryDate(int expiryMonth, int expiryYear);

    /**
     * Attach an as-you-type formatter to an {@link EditText} to format input to an expiry date.
     *
     * @param editText The {@link EditText} to attach the formatter to.
     * @return The attached formatter in form of a {@link TextWatcher}.
     */
    @NonNull
    TextWatcher attachAsYouTypeExpiryDateFormatter(@NonNull EditText editText);

    /**
     * Formats a security code.
     *
     * @param securityCode The security code to be formatted.
     * @return The formatted security code.
     */
    @NonNull
    String formatSecurityCode(@NonNull String securityCode);
}

/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/7/2024.
 */

package com.adyen.checkout.giftcard.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.FieldState

/**
 * Validator class responsible for validating input fields in [com.adyen.checkout.giftcard.GiftCardComponent].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface GiftCardValidator {

    /**
     * Validates gift card number.
     *
     * @param number Gift Card number input.
     *
     * @return FieldState object containing validation result.
     */
    fun validateNumber(number: String): FieldState<String>

    /**
     * Validates gift card pin.
     *
     * @param pin Gift Card pin input.
     *
     * @return FieldState object containing validation result.
     */
    fun validatePin(pin: String): FieldState<String>

    /**
     * Validates gift card expiry date.
     *
     * @param expiryDate Gift Card expiry date input.
     *
     * @return FieldState object containing validation result.
     */
    fun validateExpiryDate(expiryDate: String): FieldState<String>
}

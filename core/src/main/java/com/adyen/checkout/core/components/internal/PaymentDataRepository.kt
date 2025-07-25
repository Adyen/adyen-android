/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/8/2022.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo
import androidx.lifecycle.SavedStateHandle

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PaymentDataRepository(
    private val savedStateHandle: SavedStateHandle,
) {
    var paymentData: String?
        get() = savedStateHandle[PAYMENT_DATA_KEY]
        set(paymentData) {
            savedStateHandle[PAYMENT_DATA_KEY] = paymentData
        }

    var nativeRedirectData: String?
        get() = savedStateHandle[NATIVE_REDIRECT_DATA]
        set(paymentData) {
            savedStateHandle[NATIVE_REDIRECT_DATA] = paymentData
        }

    companion object {
        private const val PAYMENT_DATA_KEY = "payment_data"
        private const val NATIVE_REDIRECT_DATA = "native_redirect_data"
    }
}

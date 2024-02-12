/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class AnalyticsSource {
    data class DropIn(val paymentMethodList: List<String>) : AnalyticsSource()
    data class PaymentComponent(val paymentMethodType: String) : AnalyticsSource()

    // TODO: Check if we can rename paymentMethodList and not make it clash with this function
    fun getPaymentMethods(): List<String> = when(this) {
        is DropIn -> paymentMethodList
        is PaymentComponent -> listOf(paymentMethodType)
    }
}

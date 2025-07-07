/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class AnalyticsSource {
    data class DropIn(val paymentMethodList: List<String>) : AnalyticsSource()
    data class PaymentComponent(val paymentMethodType: String) : AnalyticsSource()

    fun getPaymentMethods(): List<String> = when (this) {
        is DropIn -> paymentMethodList
        is PaymentComponent -> listOf(paymentMethodType)
    }
}

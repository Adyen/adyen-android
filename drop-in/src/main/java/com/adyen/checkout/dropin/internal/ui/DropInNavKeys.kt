/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * A key that belongs to the payment flow of a single payment method. All keys of the same payment flow
 * share one [com.adyen.checkout.core.components.CheckoutController].
 */
internal interface PaymentFlowNavKey : NavKey {
    val paymentFlowType: DropInPaymentFlowType
}

@Serializable
internal data object EmptyNavKey : NavKey

@Serializable
internal data class PreselectedPaymentMethodNavKey(
    val storedPaymentMethodId: String,
) : NavKey

@Serializable
internal data object PaymentMethodListNavKey : NavKey

@Serializable
internal data object StoredPaymentMethodsNavKey : NavKey

@Serializable
internal data class PaymentMethodNavKey(
    override val paymentFlowType: DropInPaymentFlowType,
) : PaymentFlowNavKey

@Serializable
internal data class ActionNavKey(
    override val paymentFlowType: DropInPaymentFlowType,
) : PaymentFlowNavKey

@Serializable
internal data class SecondaryNavKey(
    override val paymentFlowType: DropInPaymentFlowType,
    val identifier: String,
) : PaymentFlowNavKey

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/7/2025.
 */

package com.adyen.checkout.core.common

import android.os.Parcelable
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.sessions.CheckoutSession
import kotlinx.parcelize.Parcelize

// TODO - Kdocs
sealed interface CheckoutContext : Parcelable {

    @Parcelize
    data class Sessions internal constructor(
        val checkoutSession: CheckoutSession,
        val checkoutConfiguration: CheckoutConfiguration,
        internal val publicKey: String?,
    ) : CheckoutContext

    @Parcelize
    data class Advanced internal constructor(
        val paymentMethodsApiResponse: PaymentMethodsApiResponse,
        val checkoutConfiguration: CheckoutConfiguration,
        internal val publicKey: String?,
    ) : CheckoutContext
}

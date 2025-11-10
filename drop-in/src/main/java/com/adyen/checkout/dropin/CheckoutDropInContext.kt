/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/11/2025.
 */

package com.adyen.checkout.dropin

import android.os.Parcelable
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.sessions.CheckoutSession
import kotlinx.parcelize.Parcelize

// TODO - KDocs
interface CheckoutDropInContext : Parcelable {

    @Parcelize
    data class Sessions internal constructor(
        val checkoutSession: CheckoutSession,
        val checkoutConfiguration: CheckoutConfiguration,
        internal val publicKey: String?,
    ) : CheckoutDropInContext

    @Parcelize
    data class Advanced internal constructor(
        val paymentMethodsApiResponse: PaymentMethodsApiResponse,
        val checkoutConfiguration: CheckoutConfiguration,
        internal val publicKey: String?,
    ) : CheckoutDropInContext
}

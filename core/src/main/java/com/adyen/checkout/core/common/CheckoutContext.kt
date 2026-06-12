/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/7/2025.
 */

package com.adyen.checkout.core.common

import android.os.Parcelable
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.sessions.CheckoutSession
import kotlinx.parcelize.Parcelize

// TODO - Kdocs
sealed interface CheckoutContext : Parcelable {

    val checkoutConfiguration: CheckoutConfiguration

    @Parcelize
    @ConsistentCopyVisibility
    data class Sessions internal constructor(
        val checkoutSession: CheckoutSession,
        override val checkoutConfiguration: CheckoutConfiguration,
        internal val checkoutAttemptId: String?,
        internal val publicKey: String?,
    ) : CheckoutContext

    @Parcelize
    @ConsistentCopyVisibility
    data class Advanced internal constructor(
        val paymentMethods: PaymentMethods,
        override val checkoutConfiguration: CheckoutConfiguration,
        internal val checkoutAttemptId: String?,
        internal val publicKey: String?,
    ) : CheckoutContext

    @Parcelize
    @ConsistentCopyVisibility
    data class ActionOnly internal constructor(
        internal val action: Action,
        override val checkoutConfiguration: CheckoutConfiguration,
        internal val checkoutAttemptId: String?,
        internal val publicKey: String?,
    ) : CheckoutContext
}

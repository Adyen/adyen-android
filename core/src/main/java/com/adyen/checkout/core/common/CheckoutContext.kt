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
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.sessions.CheckoutSession
import kotlinx.parcelize.Parcelize

/**
 * Holds the initialized state of a checkout flow.
 *
 * An instance is created by one of the [Checkout.setup] functions and then passed to a
 * [CheckoutController] to drive the payment. Each subtype represents a specific integration flow.
 */
sealed interface CheckoutContext : Parcelable {

    /**
     * The [CheckoutConfiguration] used to set up this checkout.
     */
    val checkoutConfiguration: CheckoutConfiguration

    /**
     * Checkout context for the sessions flow, created from a session response returned by the
     * `/sessions` endpoint.
     */
    @Parcelize
    @ConsistentCopyVisibility
    data class Sessions internal constructor(
        val checkoutSession: CheckoutSession,
        override val checkoutConfiguration: CheckoutConfiguration,
        internal val checkoutAttemptId: String,
        internal val publicKey: String?,
    ) : CheckoutContext

    /**
     * Checkout context for the advanced flow, created from the available payment
     * methods returned by the `/paymentMethods` endpoint.
     */
    @Parcelize
    @ConsistentCopyVisibility
    data class Advanced internal constructor(
        val paymentMethods: PaymentMethods,
        override val checkoutConfiguration: CheckoutConfiguration,
        internal val checkoutAttemptId: String,
        internal val publicKey: String?,
    ) : CheckoutContext

    /**
     * Checkout context for the action-only flow, created from a standalone [Action] that needs to
     * be handled.
     */
    @Parcelize
    @ConsistentCopyVisibility
    data class ActionOnly internal constructor(
        internal val action: Action,
        override val checkoutConfiguration: CheckoutConfiguration,
        internal val checkoutAttemptId: String,
        internal val publicKey: String?,
    ) : CheckoutContext
}

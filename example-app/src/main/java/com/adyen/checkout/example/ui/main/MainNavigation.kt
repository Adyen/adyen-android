/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/4/2022.
 */

package com.adyen.checkout.example.ui.main

import com.adyen.checkout.dropin.CheckoutDropInContext
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.components.core.CheckoutConfiguration as OldCheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodsApiResponse as OldPaymentMethodsApiResponse

internal sealed class MainNavigation {

    data object Bacs : MainNavigation()

    data object Blik : MainNavigation()

    data object Card : MainNavigation()

    data class Instant(val paymentMethodType: String) : MainNavigation()

    data object CardWithSession : MainNavigation()

    data object GiftCard : MainNavigation()

    data object GiftCardWithSession : MainNavigation()

    data object CardWithSessionTakenOver : MainNavigation()

    data object GooglePay : MainNavigation()

    data object GooglePayWithSession : MainNavigation()

    data object V6 : MainNavigation()

    data object V6Sessions : MainNavigation()

    data class DropIn(
        val paymentMethodsApiResponse: OldPaymentMethodsApiResponse,
        val checkoutConfiguration: OldCheckoutConfiguration
    ) : MainNavigation()

    data class DropInWithSession(
        val checkoutSession: CheckoutSession,
        val checkoutConfiguration: OldCheckoutConfiguration
    ) : MainNavigation()

    data class DropInWithCustomSession(
        val checkoutSession: CheckoutSession,
        val checkoutConfiguration: OldCheckoutConfiguration
    ) : MainNavigation()

    data class V6DropIn(
        val dropInContext: CheckoutDropInContext.Advanced,
    ) : MainNavigation()

    data class V6DropInWithSession(
        val dropInContext: CheckoutDropInContext.Sessions,
    ) : MainNavigation()
}

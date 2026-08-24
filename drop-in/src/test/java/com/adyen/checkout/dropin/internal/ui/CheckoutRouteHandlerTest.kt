/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.dropin.internal.helper.InMemoryBackStackPersister
import com.adyen.checkout.test.LoggingExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(LoggingExtension::class)
internal class CheckoutRouteHandlerTest {

    private lateinit var navigator: DropInNavigator
    private lateinit var routeHandler: CheckoutRouteHandler

    private val paymentFlowType = DropInPaymentFlowType.RegularPaymentMethod("scheme")
    private val paymentMethodKey = PaymentMethodNavKey(paymentFlowType)

    @BeforeEach
    fun setUp() {
        navigator = DropInNavigator(InMemoryBackStackPersister())
        navigator.navigateTo(paymentMethodKey)
        routeHandler = CheckoutRouteHandler(navigator)
    }

    @Test
    fun `when the action route is handled then the payment method screen is replaced by the action screen`() {
        routeHandler.handle(CheckoutRoute.Action(), paymentFlowType)

        assertEquals(listOf(EmptyNavKey, ActionNavKey(paymentFlowType)), navigator.backStack)
    }

    @Test
    fun `when the action route is handled twice then the action screen is not duplicated`() {
        routeHandler.handle(CheckoutRoute.Action(), paymentFlowType)
        routeHandler.handle(CheckoutRoute.Action(), paymentFlowType)

        assertEquals(listOf(EmptyNavKey, ActionNavKey(paymentFlowType)), navigator.backStack)
    }

    @Test
    fun `when going back from the action screen then the drop-in is finished`() {
        routeHandler.handle(CheckoutRoute.Action(), paymentFlowType)

        navigator.back()

        assertEquals(listOf(EmptyNavKey), navigator.backStack)
        assertEquals(true, navigator.finishFlow.value)
    }

    @Test
    fun `when the secondary route is handled then the secondary screen is pushed on top`() {
        routeHandler.handle(CheckoutRoute.Secondary("INSTALLMENTS"), paymentFlowType)

        assertEquals(
            listOf(EmptyNavKey, paymentMethodKey, SecondaryNavKey(paymentFlowType, "INSTALLMENTS")),
            navigator.backStack,
        )
    }

    @Test
    fun `when the same secondary route is handled twice then the secondary screen is not duplicated`() {
        routeHandler.handle(CheckoutRoute.Secondary("INSTALLMENTS"), paymentFlowType)
        routeHandler.handle(CheckoutRoute.Secondary("INSTALLMENTS"), paymentFlowType)

        assertEquals(
            listOf(EmptyNavKey, paymentMethodKey, SecondaryNavKey(paymentFlowType, "INSTALLMENTS")),
            navigator.backStack,
        )
    }

    @Test
    fun `when the payment method route is handled from a secondary screen then it pops back to it`() {
        routeHandler.handle(CheckoutRoute.Secondary("INSTALLMENTS"), paymentFlowType)

        routeHandler.handle(CheckoutRoute.PaymentMethod(), paymentFlowType)

        assertEquals(listOf(EmptyNavKey, paymentMethodKey), navigator.backStack)
    }

    @Test
    fun `when the payment method route is handled and it is already displayed then nothing changes`() {
        routeHandler.handle(CheckoutRoute.PaymentMethod(), paymentFlowType)

        assertEquals(listOf(EmptyNavKey, paymentMethodKey), navigator.backStack)
    }

    @Test
    fun `when the payment method route is handled and it is not on the back stack then it is displayed again`() {
        routeHandler.handle(CheckoutRoute.Action(), paymentFlowType)

        routeHandler.handle(CheckoutRoute.PaymentMethod(), paymentFlowType)

        assertEquals(listOf(EmptyNavKey, paymentMethodKey), navigator.backStack)
    }
}

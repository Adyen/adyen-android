/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Intent
import com.adyen.checkout.dropin.internal.helper.InMemoryBackStackPersister
import com.adyen.checkout.test.LoggingExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(LoggingExtension::class)
internal class CheckoutReturnHandlerTest {

    private lateinit var navigator: DropInNavigator
    private lateinit var provider: TestCheckoutControllerProvider

    private val cardFlowType = DropInPaymentFlowType.RegularPaymentMethod("scheme")
    private val storedFlowType = DropInPaymentFlowType.StoredPaymentMethod("stored-id-1")

    private val intent = mock<Intent>()

    @BeforeEach
    fun setUp() {
        navigator = DropInNavigator(InMemoryBackStackPersister())
        provider = TestCheckoutControllerProvider()
    }

    private fun CoroutineScope.createHandler(): Pair<CheckoutReturnHandler, CheckoutFlowHolder> {
        val holder = CheckoutFlowHolder(
            parentScope = this,
            controllerProvider = provider,
            routeHandler = CheckoutRouteHandler(navigator),
        )
        return CheckoutReturnHandler(navigator, holder) to holder
    }

    @Test
    fun `when an action is displayed then the intent is handled by the controller of its flow`() = runTest {
        val (returnHandler, holder) = backgroundScope.createHandler()
        holder.getController(cardFlowType)
        navigator.navigateTo(ActionNavKey(cardFlowType))

        returnHandler.handle(intent)

        verify(provider.controllers.getValue(cardFlowType)).handleReturn(intent)
    }

    @Test
    fun `when several flows are alive then only the controller of the displayed action handles the intent`() = runTest {
        val (returnHandler, holder) = backgroundScope.createHandler()
        holder.getController(cardFlowType)
        holder.getController(storedFlowType)
        navigator.navigateTo(ActionNavKey(storedFlowType))

        returnHandler.handle(intent)

        verify(provider.controllers.getValue(storedFlowType)).handleReturn(intent)
        verify(provider.controllers.getValue(cardFlowType), never()).handleReturn(intent)
    }

    @Test
    fun `when the payment method screen is displayed then the intent is ignored`() = runTest {
        val (returnHandler, holder) = backgroundScope.createHandler()
        holder.getController(cardFlowType)
        navigator.navigateTo(PaymentMethodNavKey(cardFlowType))

        returnHandler.handle(intent)

        verify(provider.controllers.getValue(cardFlowType), never()).handleReturn(intent)
    }

    @Test
    fun `when a secondary screen is displayed then the intent is ignored`() = runTest {
        val (returnHandler, holder) = backgroundScope.createHandler()
        holder.getController(cardFlowType)
        navigator.navigateTo(SecondaryNavKey(cardFlowType, "INSTALLMENTS"))

        returnHandler.handle(intent)

        verify(provider.controllers.getValue(cardFlowType), never()).handleReturn(intent)
    }

    @Test
    fun `when the flow of the displayed action has no controller then no controller is created`() = runTest {
        val (returnHandler, _) = backgroundScope.createHandler()
        navigator.navigateTo(ActionNavKey(cardFlowType))

        returnHandler.handle(intent)

        assertEquals(0, provider.invocations)
    }
}

/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.test.LoggingExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock

@ExtendWith(LoggingExtension::class)
internal class CheckoutFlowHolderTest {

    private val cardFlowType = DropInPaymentFlowType.RegularPaymentMethod("scheme")
    private val storedFlowType = DropInPaymentFlowType.StoredPaymentMethod("stored-id-1")

    @Test
    fun `when getting the controller twice for the same flow type then the same instance is returned`() = runTest {
        val provider = TestCheckoutControllerProvider()
        val holder = CheckoutFlowHolder(backgroundScope, provider)

        val first = holder.getController(cardFlowType)
        val second = holder.getController(cardFlowType)

        assertSame(first, second)
        assertEquals(1, provider.invocations)
    }

    @Test
    fun `when getting the controller for different flow types then different instances are returned`() = runTest {
        val provider = TestCheckoutControllerProvider()
        val holder = CheckoutFlowHolder(backgroundScope, provider)

        val card = holder.getController(cardFlowType)
        val stored = holder.getController(storedFlowType)

        assertNotSame(card, stored)
        assertEquals(2, provider.invocations)
    }

    @Test
    fun `when a flow type is retained then its controller is kept`() = runTest {
        val provider = TestCheckoutControllerProvider()
        val holder = CheckoutFlowHolder(backgroundScope, provider)
        val controller = holder.getController(cardFlowType)

        holder.retainOnly(setOf(cardFlowType))

        assertSame(controller, holder.getController(cardFlowType))
        assertEquals(1, provider.invocations)
    }

    @Test
    fun `when a flow type is not retained then a new controller is created for it afterwards`() = runTest {
        val provider = TestCheckoutControllerProvider()
        val holder = CheckoutFlowHolder(backgroundScope, provider)
        val controller = holder.getController(cardFlowType)

        holder.retainOnly(setOf(storedFlowType))

        assertNotSame(controller, holder.getController(cardFlowType))
    }

    @Test
    fun `when a flow type is not retained then its coroutine scope is cancelled`() = runTest {
        val provider = TestCheckoutControllerProvider()
        val holder = CheckoutFlowHolder(backgroundScope, provider)
        holder.getController(cardFlowType)
        holder.getController(storedFlowType)

        holder.retainOnly(setOf(storedFlowType))

        assertFalse(provider.scopes.getValue(cardFlowType).isActive)
        assertTrue(provider.scopes.getValue(storedFlowType).isActive)
    }

    @Test
    fun `when retaining an empty set then all controllers are released`() = runTest {
        val provider = TestCheckoutControllerProvider()
        val holder = CheckoutFlowHolder(backgroundScope, provider)
        holder.getController(cardFlowType)
        holder.getController(storedFlowType)

        holder.retainOnly(emptySet())

        assertFalse(provider.scopes.getValue(cardFlowType).isActive)
        assertFalse(provider.scopes.getValue(storedFlowType).isActive)
    }

    @Test
    fun `when the parent scope is cancelled then the flow scopes are cancelled`() = runTest {
        val provider = TestCheckoutControllerProvider()
        val parentScope = CoroutineScope(backgroundScope.coroutineContext + SupervisorJob())
        val holder = CheckoutFlowHolder(parentScope, provider)
        holder.getController(cardFlowType)

        parentScope.cancel()

        assertFalse(provider.scopes.getValue(cardFlowType).isActive)
    }

    @Test
    fun `when a flow scope is cancelled then the parent scope stays active`() = runTest {
        val provider = TestCheckoutControllerProvider()
        val parentScope = CoroutineScope(backgroundScope.coroutineContext + SupervisorJob())
        val holder = CheckoutFlowHolder(parentScope, provider)
        holder.getController(cardFlowType)

        holder.retainOnly(emptySet())

        assertTrue(parentScope.isActive)
    }

    private class TestCheckoutControllerProvider : CheckoutControllerProvider {

        val scopes = mutableMapOf<DropInPaymentFlowType, CoroutineScope>()
        var invocations = 0
            private set

        override fun provide(
            paymentFlowType: DropInPaymentFlowType,
            coroutineScope: CoroutineScope,
        ): CheckoutController {
            invocations++
            scopes[paymentFlowType] = coroutineScope
            return mock()
        }
    }
}

/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 13/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.dropin.internal.helper.InMemoryBackStackPersister
import com.adyen.checkout.dropin.internal.helper.mockCheckoutController
import com.adyen.checkout.test.LoggingExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class)
internal class PaymentFlowCoordinatorTest {

    private val requestedPaymentFlowTypes = mutableListOf<DropInPaymentFlowType>()
    private val createdFlowScopes = mutableListOf<CoroutineScope>()
    private val navigationFlow = MutableSharedFlow<CheckoutRoute>(extraBufferCapacity = 1)

    private var requiresUserInteraction = true

    private val controllerProvider = DropInControllerProvider { paymentFlowType, flowScope ->
        requestedPaymentFlowTypes += paymentFlowType
        createdFlowScopes += flowScope
        mockCheckoutController(
            requiresUserInteraction = requiresUserInteraction,
            navigationFlow = navigationFlow,
        )
    }

    private lateinit var persister: InMemoryBackStackPersister
    private lateinit var navigator: DropInNavigator
    private lateinit var coroutineScope: CoroutineScope

    @BeforeEach
    fun setUp() {
        persister = InMemoryBackStackPersister()
        navigator = DropInNavigator(persister)
        coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        coroutineScope.cancel()
    }

    @Test
    fun `when starting a flow, then a controller is created for the payment flow type`() {
        val coordinator = createCoordinator()

        coordinator.startFlow(REGULAR_TYPE)

        assertEquals(listOf(REGULAR_TYPE), requestedPaymentFlowTypes)
        assertNotNull(coordinator.activeController)
    }

    @Test
    fun `when starting a flow, then it navigates to the payment method`() {
        navigator.navigateTo(PaymentMethodListNavKey)
        val coordinator = createCoordinator()

        coordinator.startFlow(REGULAR_TYPE)

        val expected = listOf(EmptyNavKey, PaymentMethodListNavKey, PaymentMethodNavKey(REGULAR_TYPE))
        assertEquals(expected, navigator.backStack)
    }

    @Test
    fun `when starting a flow while replacing the back stack, then only the payment method is left`() {
        navigator.navigateTo(PreselectedPaymentMethodNavKey(STORED_ID))
        val coordinator = createCoordinator()

        coordinator.startFlow(STORED_TYPE, replaceBackStack = true)

        assertEquals(listOf(EmptyNavKey, PaymentMethodNavKey(STORED_TYPE)), navigator.backStack)
    }

    @Test
    fun `when starting a second flow, then the first flow is torn down and a new controller is created`() {
        val coordinator = createCoordinator()
        coordinator.startFlow(REGULAR_TYPE)
        val firstController = coordinator.activeController

        coordinator.startFlow(STORED_TYPE, replaceBackStack = true)

        assertFalse(createdFlowScopes[0].isActive)
        assertTrue(createdFlowScopes[1].isActive)
        assertNotSame(firstController, coordinator.activeController)
    }

    @Test
    fun `when re-entering the same payment method, then a new controller is created`() {
        navigator.navigateTo(PaymentMethodListNavKey)
        val coordinator = createCoordinator()
        coordinator.startFlow(REGULAR_TYPE)
        val firstController = coordinator.activeController
        navigator.back()

        coordinator.startFlow(REGULAR_TYPE)

        assertEquals(listOf(REGULAR_TYPE, REGULAR_TYPE), requestedPaymentFlowTypes)
        assertNotSame(firstController, coordinator.activeController)
    }

    @Test
    fun `when no payment flow key is left on the back stack, then the flow is torn down`() {
        navigator.navigateTo(PaymentMethodListNavKey)
        val coordinator = createCoordinator()
        coordinator.startFlow(REGULAR_TYPE)

        navigator.back()

        assertNull(coordinator.activeController)
        assertFalse(createdFlowScopes[0].isActive)
    }

    @Test
    fun `when a payment flow key is still on the back stack, then the flow is kept alive`() {
        val coordinator = createCoordinator()
        coordinator.startFlow(REGULAR_TYPE)

        navigator.navigateTo(StoredPaymentMethodsNavKey)

        assertNotNull(coordinator.activeController)
        assertTrue(createdFlowScopes[0].isActive)
    }

    @Test
    fun `when the parent scope is cancelled, then the flow is torn down`() {
        val coordinator = createCoordinator()
        coordinator.startFlow(REGULAR_TYPE)

        coroutineScope.cancel()

        assertFalse(createdFlowScopes[0].isActive)
    }

    @Test
    fun `when restoring a flow, then a controller is created without navigating`() {
        navigator.navigateTo(PaymentMethodNavKey(REGULAR_TYPE))
        val restoredNavigator = DropInNavigator(persister)
        val coordinator = createCoordinator(restoredNavigator)

        coordinator.restoreFlow()

        assertEquals(listOf(REGULAR_TYPE), requestedPaymentFlowTypes)
        assertNotNull(coordinator.activeController)
        assertEquals(listOf(EmptyNavKey, PaymentMethodNavKey(REGULAR_TYPE)), restoredNavigator.backStack)
    }

    @Test
    fun `when restoring a flow without a payment method on the back stack, then no controller is created`() {
        navigator.navigateTo(PaymentMethodListNavKey)
        val restoredNavigator = DropInNavigator(persister)
        val coordinator = createCoordinator(restoredNavigator)

        coordinator.restoreFlow()

        assertEquals(emptyList<DropInPaymentFlowType>(), requestedPaymentFlowTypes)
        assertNull(coordinator.activeController)
    }

    @Test
    fun `when the payment method needs no user interaction, then it is submitted without navigating`() {
        requiresUserInteraction = false
        navigator.navigateTo(PaymentMethodListNavKey)
        val coordinator = createCoordinator()

        coordinator.startFlow(REGULAR_TYPE)

        verify(requireNotNull(coordinator.activeController)).submit()
        assertEquals(listOf(EmptyNavKey, PaymentMethodListNavKey), navigator.backStack)
    }

    @Test
    fun `when an action is returned, then it navigates to the action screen`() {
        navigator.navigateTo(PaymentMethodListNavKey)
        val coordinator = createCoordinator()
        coordinator.startFlow(REGULAR_TYPE)

        navigationFlow.tryEmit(CheckoutRoute.Action())

        assertEquals(listOf(EmptyNavKey, ActionNavKey), navigator.backStack)
    }

    @Test
    fun `when an action is returned, then the flow is kept alive`() {
        val coordinator = createCoordinator()
        coordinator.startFlow(REGULAR_TYPE)
        val controller = coordinator.activeController

        navigationFlow.tryEmit(CheckoutRoute.Action())

        assertSame(controller, coordinator.activeController)
        assertTrue(createdFlowScopes[0].isActive)
    }

    private fun createCoordinator(navigator: DropInNavigator = this.navigator) = PaymentFlowCoordinator(
        navigator = navigator,
        controllerProvider = controllerProvider,
        coroutineScope = coroutineScope,
    )

    private companion object {
        private const val STORED_ID = "stored-id-1"
        private val REGULAR_TYPE = DropInPaymentFlowType.RegularPaymentMethod("scheme")
        private val STORED_TYPE = DropInPaymentFlowType.StoredPaymentMethod(STORED_ID)
    }
}

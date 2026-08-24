/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.dropin.internal.helper.InMemoryBackStackPersister
import com.adyen.checkout.dropin.internal.helper.mockCheckoutController
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class, TestDispatcherExtension::class)
internal class GooglePayViewModelTest {

    private val requestedPaymentFlowTypes = mutableListOf<DropInPaymentFlowType>()
    private val createdFlowScopes = mutableListOf<CoroutineScope>()
    private val createdControllers = mutableListOf<CheckoutController>()
    private val navigationFlow = MutableSharedFlow<CheckoutRoute>(extraBufferCapacity = 1)

    private val controllerProvider = DropInControllerProvider { paymentFlowType, coroutineScope ->
        requestedPaymentFlowTypes += paymentFlowType
        createdFlowScopes += coroutineScope
        mockCheckoutController(navigationFlow = navigationFlow).also { createdControllers += it }
    }

    private lateinit var navigator: DropInNavigator

    @BeforeEach
    fun setUp() {
        navigator = DropInNavigator(InMemoryBackStackPersister())
    }

    @Test
    fun `when created, then a controller is created for the Google Pay flow type`() {
        val viewModel = createViewModel()

        assertEquals(listOf(GOOGLE_PAY_TYPE), requestedPaymentFlowTypes)
        assertSame(createdControllers.single(), viewModel.controller)
    }

    @Test
    fun `when created, then the controller is scoped to the view model`() {
        val viewModel = createViewModel()

        // The list and the Google Pay action screen share a content key, so this scope outlives the list itself.
        assertSame(viewModel.viewModelScope, createdFlowScopes.single())
    }

    @Test
    fun `when created, then it is not submitted`() {
        // Unlike the other payment methods, Google Pay is submitted by the shopper tapping its own button.
        val viewModel = createViewModel()

        verify(viewModel.controller, never()).submit()
    }

    @Test
    fun `when an action is returned, then it navigates to the Google Pay action screen`() {
        navigator.navigateTo(PaymentMethodListNavKey)
        createViewModel()

        navigationFlow.tryEmit(CheckoutRoute.Action())

        assertEquals(listOf(EmptyNavKey, GooglePayActionNavKey(GOOGLE_PAY_TYPE)), navigator.backStack)
    }

    private fun createViewModel() = GooglePayViewModel(
        paymentFlowType = GOOGLE_PAY_TYPE,
        navigator = navigator,
        controllerProvider = controllerProvider,
    )

    private companion object {
        private val GOOGLE_PAY_TYPE = DropInPaymentFlowType.RegularPaymentMethod("googlepay")
    }
}

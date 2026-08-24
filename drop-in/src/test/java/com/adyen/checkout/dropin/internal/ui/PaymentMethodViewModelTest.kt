/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.TestPaymentMethodRepository
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
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class, TestDispatcherExtension::class)
internal class PaymentMethodViewModelTest {

    private val requestedPaymentFlowTypes = mutableListOf<DropInPaymentFlowType>()
    private val createdFlowScopes = mutableListOf<CoroutineScope>()
    private val createdControllers = mutableListOf<CheckoutController>()
    private val navigationFlow = MutableSharedFlow<CheckoutRoute>(extraBufferCapacity = 1)

    private var requiresUserInteraction = true
    private var onSubmit: () -> Unit = {}

    private val controllerProvider = DropInControllerProvider { paymentFlowType, coroutineScope ->
        requestedPaymentFlowTypes += paymentFlowType
        createdFlowScopes += coroutineScope
        mockCheckoutController(
            requiresUserInteraction = requiresUserInteraction,
            navigationFlow = navigationFlow,
            // Wrapped so the hook is read when submit is called, not when the controller is created.
            onSubmit = { onSubmit() },
        ).also { createdControllers += it }
    }

    private val dropInParams = DropInParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        amount = Amount(currency = "USD", value = 999L),
    )

    private val paymentMethodRepository = TestPaymentMethodRepository(
        storedMethods = listOf(
            StoredCardPaymentMethod(
                type = PaymentMethodTypes.SCHEME,
                name = "Visa",
                id = STORED_ID,
                supportedShopperInteractions = listOf("Ecommerce"),
                brand = "visa",
                lastFour = "1234",
                expiryMonth = "01",
                expiryYear = "2030",
                holderName = null,
                fundingSource = null,
            ),
        ),
        paymentMethods = listOf(
            CardPaymentMethod(
                type = PaymentMethodTypes.SCHEME,
                name = "Cards",
                brands = listOf("visa"),
                fundingSource = null,
            ),
        ),
    )

    private lateinit var navigator: DropInNavigator

    @BeforeEach
    fun setUp() {
        navigator = DropInNavigator(InMemoryBackStackPersister())
    }

    @Test
    fun `when created, then the view state describes the payment method`() {
        val viewModel = createViewModel(REGULAR_TYPE)

        assertEquals("Cards", viewModel.viewState.value.paymentMethodName)
    }

    @Test
    fun `when created, then a controller is created for the payment flow type`() {
        val viewModel = createViewModel(REGULAR_TYPE)

        assertEquals(listOf(REGULAR_TYPE), requestedPaymentFlowTypes)
        assertSame(createdControllers.single(), viewModel.controller)
    }

    @Test
    fun `when the payment method requires user interaction, then it is not submitted`() {
        requiresUserInteraction = true

        val viewModel = createViewModel(REGULAR_TYPE)

        verify(viewModel.controller, never()).submit()
    }

    @Test
    fun `when the payment method needs no user interaction, then it is submitted`() {
        requiresUserInteraction = false

        val viewModel = createViewModel(REGULAR_TYPE)

        verify(viewModel.controller).submit()
    }

    @Test
    fun `when the payment method needs no user interaction, then it is submitted only once its screen is shown`() {
        requiresUserInteraction = false
        val backStackWhenSubmitted = mutableListOf<NavKey>()
        navigator.navigateTo(PaymentMethodNavKey(REGULAR_TYPE))
        onSubmit = { backStackWhenSubmitted += navigator.backStack }

        createViewModel(REGULAR_TYPE)

        // The action replaces the back stack as soon as the payments call returns, so the screen reporting progress
        // has to be on it before the submit happens.
        assertEquals(listOf(EmptyNavKey, PaymentMethodNavKey(REGULAR_TYPE)), backStackWhenSubmitted)
    }

    @Test
    fun `when an action is returned, then it navigates to the action screen`() {
        navigator.navigateTo(PaymentMethodNavKey(REGULAR_TYPE))
        createViewModel(REGULAR_TYPE)

        navigationFlow.tryEmit(CheckoutRoute.Action())

        assertEquals(
            listOf(EmptyNavKey, ActionNavKey(REGULAR_TYPE, ActionFlowOwner.PAYMENT_METHOD)),
            navigator.backStack,
        )
    }

    @Test
    fun `when an action is returned for a stored payment method, then it navigates to its action screen`() {
        navigator.navigateTo(PaymentMethodNavKey(STORED_TYPE))
        createViewModel(STORED_TYPE)

        navigationFlow.tryEmit(CheckoutRoute.Action())

        assertEquals(
            listOf(EmptyNavKey, ActionNavKey(STORED_TYPE, ActionFlowOwner.PAYMENT_METHOD)),
            navigator.backStack,
        )
    }

    @Test
    fun `when created, then the controller is scoped to the view model`() {
        val viewModel = createViewModel(REGULAR_TYPE)

        // The scope the controller runs on is the one cancelled when nav3 clears the store of the flow.
        assertSame(viewModel.viewModelScope, createdFlowScopes.single())
    }

    private fun createViewModel(paymentFlowType: DropInPaymentFlowType) = PaymentMethodViewModel(
        paymentFlowType = paymentFlowType,
        paymentMethodRepository = paymentMethodRepository,
        dropInParams = dropInParams,
        navigator = navigator,
        controllerProvider = controllerProvider,
    )

    private companion object {
        private const val STORED_ID = "stored-id-1"
        private val REGULAR_TYPE = DropInPaymentFlowType.RegularPaymentMethod("scheme")
        private val STORED_TYPE = DropInPaymentFlowType.StoredPaymentMethod(STORED_ID)
    }
}

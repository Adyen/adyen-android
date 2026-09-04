/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.TestPaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.InMemoryBackStackPersister
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class, TestDispatcherExtension::class)
internal class PaymentMethodViewModelTest {

    private val requestedPaymentFlowTypes = mutableListOf<DropInPaymentFlowType>()
    private val createdFlowScopes = mutableListOf<CoroutineScope>()
    private val createdControllers = mutableListOf<CheckoutController>()
    private val navigator = DropInNavigator(InMemoryBackStackPersister())

    private var navigationFlow: Flow<CheckoutRoute> = emptyFlow()
    private var requiresUserInteraction: Boolean = true

    private val controllerProvider = DropInControllerProvider { paymentFlowType, coroutineScope ->
        requestedPaymentFlowTypes += paymentFlowType
        createdFlowScopes += coroutineScope
        mockCheckoutController().also { createdControllers += it }
    }

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

    @Test
    fun `when the component takes input, then the component renders it`() {
        requiresUserInteraction = true

        val viewModel = createViewModel(REGULAR_TYPE)

        val input = assertInstanceOf<PaymentMethodViewState.Regular>(viewModel.paymentMethodViewState)
        assertEquals("Cards", input.paymentMethodName)
        assertEquals(CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_CARD_DESCRIPTION, input.description)
    }

    @Test
    fun `when a stored payment method takes input, then the screen introduces it above the form`() {
        requiresUserInteraction = true

        val viewModel = createViewModel(STORED_TYPE)

        val stored = assertInstanceOf<PaymentMethodViewState.Stored>(viewModel.paymentMethodViewState)
        assertEquals("\u2022\u2022\u2022\u2022 1234", stored.title)
        assertEquals("visa", stored.logoTxVariant)
        assertEquals("Visa", stored.paymentMethodName)
    }

    @Test
    fun `when a stored payment method takes no input, then it is confirmed before being charged`() {
        requiresUserInteraction = false

        val viewModel = createViewModel(STORED_TYPE)

        // Core reports that a stored payment method needs no interaction. Routing it to progress would charge the
        // shopper on arrival, so this must stay a screen with a button.
        val stored = assertInstanceOf<PaymentMethodViewState.Stored>(viewModel.paymentMethodViewState)
        assertEquals("\u2022\u2022\u2022\u2022 1234", stored.title)
        assertEquals("visa", stored.logoTxVariant)
        assertEquals("Visa", stored.paymentMethodName)
    }

    @Test
    fun `when a stored payment method takes no input, then it is not submitted`() {
        requiresUserInteraction = false

        val viewModel = createViewModel(STORED_TYPE)

        verify(viewModel.controller, never()).submit()
    }

    @Test
    fun `when a payment method takes no input, then progress is reported`() {
        requiresUserInteraction = false

        val viewModel = createViewModel(REGULAR_TYPE)

        val progress = assertInstanceOf<PaymentMethodViewState.Progress>(viewModel.paymentMethodViewState)
        assertEquals("card", progress.logoTxVariant)
        assertEquals("Cards", progress.paymentMethodName)
    }

    @Test
    fun `when a payment method takes no input, then it is submitted once`() {
        requiresUserInteraction = false

        val viewModel = createViewModel(REGULAR_TYPE)

        verify(viewModel.controller, times(1)).submit()
    }

    @Test
    fun `when the component takes input, then nothing is submitted`() {
        requiresUserInteraction = true

        val viewModel = createViewModel(REGULAR_TYPE)

        verify(viewModel.controller, never()).submit()
    }

    @Test
    fun `when created, then the action state carries the logo of the payment method`() {
        val viewModel = createViewModel(REGULAR_TYPE)

        assertEquals("card", viewModel.actionViewState.logoTxVariant)
    }

    @Test
    fun `when created for a stored payment method, then the action state carries the logo of its brand`() {
        val viewModel = createViewModel(STORED_TYPE)

        assertEquals("visa", viewModel.actionViewState.logoTxVariant)
    }

    @Test
    fun `when created, then a single controller is created for the payment flow type`() {
        val viewModel = createViewModel(REGULAR_TYPE)

        assertEquals(listOf(REGULAR_TYPE), requestedPaymentFlowTypes)
        assertSame(createdControllers.single(), viewModel.controller)
    }

    @Test
    fun `when created for a stored payment method, then a controller is created for that flow type`() {
        val viewModel = createViewModel(STORED_TYPE)

        assertEquals(listOf(STORED_TYPE), requestedPaymentFlowTypes)
        assertSame(createdControllers.single(), viewModel.controller)
    }

    @Test
    fun `when created, then the controller is scoped to the view model`() {
        val viewModel = createViewModel(REGULAR_TYPE)

        // The scope the controller runs on is the one cancelled when nav3 clears the store of the flow.
        assertSame(viewModel.viewModelScope, createdFlowScopes.single())
    }

    @Test
    fun `when the controller routes to an action, then the action replaces the back stack`() {
        navigationFlow = flowOf(CheckoutRoute.Action())

        createViewModel(REGULAR_TYPE)

        // Replacing the back stack is what makes going back from the action cancel Drop-in.
        assertEquals(listOf(EmptyNavKey, ActionNavKey(REGULAR_TYPE)), navigator.backStack)
    }

    @Test
    fun `when the controller of a stored payment method routes to an action, then the action replaces the back stack`() {
        navigationFlow = flowOf(CheckoutRoute.Action())

        createViewModel(STORED_TYPE)

        assertEquals(listOf(EmptyNavKey, ActionNavKey(STORED_TYPE)), navigator.backStack)
    }

    @Test
    fun `when the controller routes nowhere, then the back stack is untouched`() {
        createViewModel(REGULAR_TYPE)

        assertEquals(listOf(EmptyNavKey), navigator.backStack)
    }

    @Test
    fun `when a payment method takes no input, then the back stack is kept`() {
        requiresUserInteraction = false
        navigateToPaymentMethod(REGULAR_TYPE)

        createViewModel(REGULAR_TYPE)

        assertEquals(
            listOf(EmptyNavKey, StoredPaymentMethodsNavKey, PaymentMethodNavKey(REGULAR_TYPE)),
            navigator.backStack,
        )
    }

    @Test
    fun `when a stored payment method takes no input, then the back stack is kept`() {
        requiresUserInteraction = false
        navigateToPaymentMethod(STORED_TYPE)

        createViewModel(STORED_TYPE)

        assertEquals(
            listOf(EmptyNavKey, StoredPaymentMethodsNavKey, PaymentMethodNavKey(STORED_TYPE)),
            navigator.backStack,
        )
    }

    @Test
    fun `when the component takes input, then the back stack is kept`() {
        requiresUserInteraction = true
        navigateToPaymentMethod(REGULAR_TYPE)

        createViewModel(REGULAR_TYPE)

        assertEquals(
            listOf(EmptyNavKey, StoredPaymentMethodsNavKey, PaymentMethodNavKey(REGULAR_TYPE)),
            navigator.backStack,
        )
    }

    /** Puts the screen under test on the back stack the way the payment method list does, with the list below it. */
    private fun navigateToPaymentMethod(paymentFlowType: DropInPaymentFlowType) {
        navigator.navigateTo(StoredPaymentMethodsNavKey)
        navigator.navigateTo(PaymentMethodNavKey(paymentFlowType))
    }

    private fun createViewModel(paymentFlowType: DropInPaymentFlowType) = PaymentMethodViewModel(
        paymentFlowType = paymentFlowType,
        paymentMethodRepository = paymentMethodRepository,
        navigator = navigator,
        controllerProvider = controllerProvider,
    )

    /**
     * [CheckoutController] is final with an internal constructor, so it can only be mocked rather than faked.
     * [CheckoutController.navigation] has to be stubbed: left alone the mock returns `null`, which throws as soon as
     * the view model collects it.
     */
    private fun mockCheckoutController(): CheckoutController = mock {
        on { navigation } doReturn navigationFlow
        on { requiresUserInteraction() } doReturn requiresUserInteraction
    }

    private companion object {
        private const val STORED_ID = "stored-id-1"
        private val REGULAR_TYPE = DropInPaymentFlowType.RegularPaymentMethod(PaymentMethodTypes.SCHEME)
        private val STORED_TYPE = DropInPaymentFlowType.StoredPaymentMethod(STORED_ID)
    }
}

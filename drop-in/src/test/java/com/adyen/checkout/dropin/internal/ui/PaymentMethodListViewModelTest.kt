/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/9/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.GooglePayPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.TestPaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.InMemoryBackStackPersister
import com.adyen.checkout.dropin.internal.helper.PaymentMethodSupportCheck
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class, TestDispatcherExtension::class)
internal class PaymentMethodListViewModelTest {

    private val requestedPaymentFlowTypes = mutableListOf<DropInPaymentFlowType>()
    private val createdFlowScopes = mutableListOf<CoroutineScope>()
    private val createdControllers = mutableListOf<CheckoutController>()
    private val navigator = DropInNavigator(InMemoryBackStackPersister())

    private val dropInParams = DropInParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        amount = Amount(currency = "USD", value = 999L),
    )

    private var navigationFlow: Flow<CheckoutRoute> = emptyFlow()

    private val controllerProvider = DropInControllerProvider { paymentFlowType, coroutineScope ->
        requestedPaymentFlowTypes += paymentFlowType
        createdFlowScopes += coroutineScope
        mockCheckoutController().also { createdControllers += it }
    }

    @Test
    fun `when an instant payment method is offered, then it is not a list item too`() {
        val viewModel = createViewModel(listOf(CARD, GOOGLE_PAY))

        assertEquals(listOf(PaymentMethodTypes.SCHEME), viewModel.listedPaymentMethodIds())
    }

    @Test
    fun `when both Google Pay types are offered, then neither is a list item`() {
        val viewModel = createViewModel(listOf(CARD, GOOGLE_PAY, GOOGLE_PAY_LEGACY))

        // Only one of the two drives the button, so filtering by that one type alone would leave the other listed.
        assertEquals(listOf(PaymentMethodTypes.SCHEME), viewModel.listedPaymentMethodIds())
    }

    @Test
    fun `when both Google Pay types are offered, then one controller is created for the first of them`() {
        val viewModel = createViewModel(listOf(CARD, GOOGLE_PAY, GOOGLE_PAY_LEGACY))

        assertEquals(listOf(GOOGLE_PAY_FLOW), requestedPaymentFlowTypes)
        assertSame(createdControllers.single(), viewModel.instantPaymentMethod?.controller)
    }

    @Test
    fun `when only the legacy Google Pay type is offered, then that type drives the instant payment method`() {
        val viewModel = createViewModel(listOf(CARD, GOOGLE_PAY_LEGACY))

        assertEquals(GOOGLE_PAY_LEGACY_FLOW, viewModel.instantPaymentMethod?.paymentFlowType)
    }

    @Test
    fun `when an instant payment method is offered, then its action state carries its logo and name`() {
        val viewModel = createViewModel(listOf(CARD, GOOGLE_PAY))

        assertEquals(
            ActionViewState(logoTxVariant = PaymentMethodTypes.GOOGLE_PAY, paymentMethodName = "Google Pay"),
            viewModel.instantPaymentMethod?.actionViewState,
        )
    }

    @Test
    fun `when an instant payment method is offered, then it is not submitted`() {
        createViewModel(listOf(CARD, GOOGLE_PAY))

        // Only the component's own button may submit. Its content is disposed while the list scrolls it out of view,
        // and the view event carrying a submit is buffered, so submitting from here would open the Google Pay sheet
        // whenever the button is next composed.
        verify(createdControllers.single(), never()).submit()
    }

    @Test
    fun `when an instant payment method is offered, then its controller is scoped to the view model`() {
        val viewModel = createViewModel(listOf(CARD, GOOGLE_PAY))

        // The scope the controller runs on is the one cancelled when nav3 clears the store of the list.
        assertSame(viewModel.viewModelScope, createdFlowScopes.single())
    }

    @Test
    fun `when no instant payment method is offered, then no controller is created`() {
        val viewModel = createViewModel(listOf(CARD))

        assertNull(viewModel.instantPaymentMethod)
        assertEquals(emptyList<DropInPaymentFlowType>(), requestedPaymentFlowTypes)
    }

    @Test
    fun `when no instant payment method is offered, then the list is not filtered`() {
        val viewModel = createViewModel(listOf(CARD))

        assertEquals(listOf(PaymentMethodTypes.SCHEME), viewModel.listedPaymentMethodIds())
    }

    @Test
    fun `when the instant payment method is looked up by its own flow, then it is found`() {
        val viewModel = createViewModel(listOf(CARD, GOOGLE_PAY))

        assertSame(viewModel.instantPaymentMethod, viewModel.findInstantPaymentMethod(GOOGLE_PAY_FLOW))
    }

    @Test
    fun `when another payment flow is looked up, then no instant payment method is found`() {
        val viewModel = createViewModel(listOf(CARD, GOOGLE_PAY))

        // The action screen resolves its controller through this lookup, so a wrong answer pays the wrong thing.
        assertNull(viewModel.findInstantPaymentMethod(CARD_FLOW))
    }

    @Test
    fun `when the instant controller routes to an action, then the action replaces the back stack`() {
        navigationFlow = flowOf(CheckoutRoute.Action())

        createViewModel(listOf(CARD, GOOGLE_PAY))

        // The owner is what points the action screen back at this view model's controller.
        assertEquals(
            listOf(EmptyNavKey, ActionNavKey(GOOGLE_PAY_FLOW, ActionFlowOwner.PAYMENT_METHOD_LIST)),
            navigator.backStack,
        )
    }

    @Test
    fun `when the instant controller routes nowhere, then the back stack is untouched`() {
        createViewModel(listOf(CARD, GOOGLE_PAY))

        assertEquals(listOf(EmptyNavKey), navigator.backStack)
    }

    private fun PaymentMethodListViewModel.listedPaymentMethodIds(): List<String> =
        viewState.value.paymentOptionsSection?.options.orEmpty().map { it.id }

    private fun createViewModel(paymentMethods: List<PaymentMethod>) = PaymentMethodListViewModel(
        dropInParams = dropInParams,
        paymentMethodRepository = TestPaymentMethodRepository(paymentMethods = paymentMethods),
        paymentMethodSupportCheck = PaymentMethodSupportCheck(),
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
    }

    private companion object {
        private val CARD = CardPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            name = "Cards",
            brands = listOf("visa"),
            fundingSource = null,
        )

        private val GOOGLE_PAY = GooglePayPaymentMethod(
            type = PaymentMethodTypes.GOOGLE_PAY,
            name = "Google Pay",
            brands = emptyList(),
            configuration = null,
        )

        private val GOOGLE_PAY_LEGACY = GooglePayPaymentMethod(
            type = PaymentMethodTypes.GOOGLE_PAY_LEGACY,
            name = "Pay with Google",
            brands = emptyList(),
            configuration = null,
        )

        private val CARD_FLOW = DropInPaymentFlowType.RegularPaymentMethod(PaymentMethodTypes.SCHEME)
        private val GOOGLE_PAY_FLOW = DropInPaymentFlowType.RegularPaymentMethod(PaymentMethodTypes.GOOGLE_PAY)
        private val GOOGLE_PAY_LEGACY_FLOW =
            DropInPaymentFlowType.RegularPaymentMethod(PaymentMethodTypes.GOOGLE_PAY_LEGACY)
    }
}

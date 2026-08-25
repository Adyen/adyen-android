/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.TestPaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.InMemoryBackStackPersister
import com.adyen.checkout.dropin.internal.helper.PaymentMethodSupportCheck
import com.adyen.checkout.dropin.internal.helper.mockCheckoutController
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class, TestDispatcherExtension::class)
internal class PaymentMethodListViewModelTest {

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
    fun `when an express payment method is offered, then a controller is created for its flow type`() {
        val viewModel = createViewModel()

        assertEquals(listOf(GOOGLE_PAY_FLOW_TYPE), requestedPaymentFlowTypes)
        assertSame(createdControllers.single(), viewModel.expressPaymentMethodController)
    }

    @Test
    fun `when no express payment method is offered, then no controller is created`() {
        val viewModel = createViewModel(
            paymentMethods = listOf(GenericPaymentMethod(type = "scheme", name = "Cards")),
        )

        assertNull(viewModel.expressPaymentMethodController)
        assertEquals(emptyList<DropInPaymentFlowType>(), requestedPaymentFlowTypes)
    }

    @Test
    fun `when an express payment method is offered, then its controller is scoped to the view model`() {
        val viewModel = createViewModel()

        // The list and the action screen share a flow key, so this scope outlives the list itself.
        assertSame(viewModel.viewModelScope, createdFlowScopes.single())
    }

    @Test
    fun `when created, then the express payment method is not submitted`() {
        // Unlike the other payment methods, it is submitted by the shopper tapping its own button.
        val viewModel = createViewModel()

        verify(requireNotNull(viewModel.expressPaymentMethodController), never()).submit()
    }

    @Test
    fun `when the flow type is an express payment method, then its controller is found`() {
        val viewModel = createViewModel()

        assertSame(
            viewModel.expressPaymentMethodController,
            viewModel.findExpressPaymentMethodController(GOOGLE_PAY_FLOW_TYPE),
        )
    }

    @Test
    fun `when the flow type is not an express payment method, then no controller is found`() {
        val viewModel = createViewModel()

        assertNull(viewModel.findExpressPaymentMethodController(DropInPaymentFlowType.RegularPaymentMethod("scheme")))
    }

    @Test
    fun `when an action is returned, then it navigates to the express payment method action screen`() {
        navigator.navigateTo(PaymentMethodListNavKey)
        createViewModel()

        navigationFlow.tryEmit(CheckoutRoute.Action())

        assertEquals(
            listOf(EmptyNavKey, ActionNavKey(GOOGLE_PAY_FLOW_TYPE, ActionFlowOwner.PAYMENT_METHOD_LIST)),
            navigator.backStack,
        )
    }

    private fun createViewModel(
        paymentMethods: List<PaymentMethod> = listOf(
            GenericPaymentMethod(type = PaymentMethodTypes.GOOGLE_PAY, name = "Google Pay"),
        ),
    ) = PaymentMethodListViewModel(
        dropInParams = DropInParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            amount = Amount(currency = "USD", value = 999L),
        ),
        paymentMethodRepository = TestPaymentMethodRepository(paymentMethods = paymentMethods),
        paymentMethodSupportCheck = PaymentMethodSupportCheck(),
        navigator = navigator,
        controllerProvider = controllerProvider,
    )

    private companion object {
        private val GOOGLE_PAY_FLOW_TYPE =
            DropInPaymentFlowType.RegularPaymentMethod(PaymentMethodTypes.GOOGLE_PAY)
    }
}

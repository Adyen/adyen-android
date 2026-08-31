/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.TestPaymentMethodRepository
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class, TestDispatcherExtension::class)
internal class PaymentMethodViewModelTest {

    private val requestedPaymentFlowTypes = mutableListOf<DropInPaymentFlowType>()
    private val createdFlowScopes = mutableListOf<CoroutineScope>()
    private val createdControllers = mutableListOf<CheckoutController>()

    private val controllerProvider = DropInControllerProvider { paymentFlowType, coroutineScope ->
        requestedPaymentFlowTypes += paymentFlowType
        createdFlowScopes += coroutineScope
        mock<CheckoutController>().also { createdControllers += it }
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
    fun `when created, then the view state describes the payment method`() {
        val viewModel = createViewModel(REGULAR_TYPE)

        assertEquals("Cards", viewModel.viewState.value.paymentMethodName)
    }

    @Test
    fun `when created for a stored payment method, then the view state describes that payment method`() {
        val viewModel = createViewModel(STORED_TYPE)

        assertEquals("Visa", viewModel.viewState.value.paymentMethodName)
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

    private fun createViewModel(paymentFlowType: DropInPaymentFlowType) = PaymentMethodViewModel(
        paymentFlowType = paymentFlowType,
        paymentMethodRepository = paymentMethodRepository,
        controllerProvider = controllerProvider,
    )

    private companion object {
        private const val STORED_ID = "stored-id-1"
        private val REGULAR_TYPE = DropInPaymentFlowType.RegularPaymentMethod(PaymentMethodTypes.SCHEME)
        private val STORED_TYPE = DropInPaymentFlowType.StoredPaymentMethod(STORED_ID)
    }
}

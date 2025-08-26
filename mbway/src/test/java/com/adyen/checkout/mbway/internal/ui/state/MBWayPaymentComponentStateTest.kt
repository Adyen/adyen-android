/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/6/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.data.OrderRequest
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.ComponentFieldState
import com.adyen.checkout.core.components.internal.ui.state.model.Validation
import com.adyen.checkout.core.components.internal.ui.state.transformer.TestFieldTransformerRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MBWayPaymentComponentStateTest {

    private lateinit var fieldTransformerRegistry: TestFieldTransformerRegistry<MBWayFieldId>
    private lateinit var initialState: MBWayComponentState
    private lateinit var orderRequest: OrderRequest
    private lateinit var amount: Amount

    @BeforeEach
    fun setup() {
        fieldTransformerRegistry = TestFieldTransformerRegistry()

        val countryModel =
            CountryModel(isoCode = "NL", countryName = "Netherlands", callingCode = "+31")
        val countryCodeFieldState =
            ComponentFieldState(value = countryModel, validation = Validation.Valid)
        val localPhoneNumberFieldState =
            ComponentFieldState(value = "123456789", validation = Validation.Valid)

        initialState = MBWayComponentState(
            countries = listOf(countryModel),
            isLoading = false,
            countryCodeFieldState = countryCodeFieldState,
            localPhoneNumberFieldState = localPhoneNumberFieldState,
        )

        orderRequest = OrderRequest(pspReference = "ref123", orderData = "data123")
        amount = Amount(currency = "EUR", value = 1000L)
    }

    @Test
    fun `when toPaymentComponentState is called with valid inputs, then payment component state should be created correctly`() {
        val updatedState = initialState.copy(
            countryCodeFieldState = initialState.countryCodeFieldState.copy(validation = Validation.Valid),
            localPhoneNumberFieldState = initialState.localPhoneNumberFieldState.copy(validation = Validation.Valid),
        )

        val componentState = updatedState.toPaymentComponentState(
            checkoutAttemptId = "",
            fieldTransformerRegistry = fieldTransformerRegistry,
            order = orderRequest,
            amount = amount,
        )

        assertEquals("+31123456789", componentState.data.paymentMethod?.telephoneNumber)
        assertEquals(orderRequest, componentState.data.order)
        assertEquals(amount, componentState.data.amount)
        assertEquals(true, componentState.isValid)
    }

    @Test
    fun `when toPaymentComponentState is called with isValid false, then payment component state should be created correctly`() {
        val updatedState = initialState.copy(
            countryCodeFieldState = initialState.countryCodeFieldState.copy(
                validation = Validation.Invalid(0),
            ),
            localPhoneNumberFieldState = initialState.localPhoneNumberFieldState.copy(
                validation = Validation.Invalid(0),
            ),
        )

        val componentState = updatedState.toPaymentComponentState(
            checkoutAttemptId = "",
            fieldTransformerRegistry = fieldTransformerRegistry,
            order = orderRequest,
            amount = amount,
        )

        assertEquals("+31123456789", componentState.data.paymentMethod?.telephoneNumber)
        assertEquals(orderRequest, componentState.data.order)
        assertEquals(amount, componentState.data.amount)
        assertEquals(false, componentState.isValid)
    }

    @Test
    fun `when transform function changes phone number, then payment component state should reflect the change`() {
        fieldTransformerRegistry.setTransformation(
            MBWayFieldId.PHONE_NUMBER,
            "123456789",
            "987654321",
        )

        val componentState = initialState.toPaymentComponentState(
            checkoutAttemptId = "",
            fieldTransformerRegistry = fieldTransformerRegistry,
            order = orderRequest,
            amount = amount,
        )

        assertEquals("+31987654321", componentState.data.paymentMethod?.telephoneNumber)
    }

    @Test
    fun `when toPaymentComponentState is called with null order and amount, then payment component state should be created correctly`() {
        val componentState = initialState.toPaymentComponentState(
            checkoutAttemptId = "",
            fieldTransformerRegistry = fieldTransformerRegistry,
            order = null,
            amount = null,
        )

        assertEquals("+31123456789", componentState.data.paymentMethod?.telephoneNumber)
        assertEquals(null, componentState.data.order)
        assertEquals(null, componentState.data.amount)
        assertEquals(true, componentState.isValid)
    }
}

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/3/2025.
 */

package com.adyen.checkout.mbway

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.internal.ui.model.TestFieldTransformerRegistry
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.mbway.internal.ui.model.MBWayFieldId
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MBWayComponentStateTest {

    private lateinit var analyticsManager: AnalyticsManager
    private lateinit var fieldTransformerRegistry: TestFieldTransformerRegistry<MBWayFieldId>
    private lateinit var initialState: MBWayDelegateState
    private lateinit var orderRequest: OrderRequest
    private lateinit var amount: Amount

    @BeforeEach
    fun setup() {
        analyticsManager = TestAnalyticsManager()
        fieldTransformerRegistry = TestFieldTransformerRegistry()

        val countryModel = CountryModel(isoCode = "NL", countryName = "Netherlands", callingCode = "+31")
        val countryCodeFieldState = ComponentFieldDelegateState(value = countryModel, validation = Validation.Valid)
        val localPhoneNumberFieldState = ComponentFieldDelegateState(value = "123456789", validation = Validation.Valid)

        initialState = MBWayDelegateState(
            countries = listOf(countryModel),
            countryCodeFieldState = countryCodeFieldState,
            localPhoneNumberFieldState = localPhoneNumberFieldState,
        )

        orderRequest = OrderRequest(pspReference = "ref123", orderData = "data123")
        amount = Amount(currency = "EUR", value = 1000L)
    }

    @Test
    fun `when toComponentState is called with valid inputs, then MBWayComponentState should be created correctly`() {
        val updatedState = initialState.copy(
            countryCodeFieldState = initialState.countryCodeFieldState.copy(validation = Validation.Valid),
            localPhoneNumberFieldState = initialState.localPhoneNumberFieldState.copy(validation = Validation.Valid),
        )

        val componentState = updatedState.toComponentState(
            analyticsManager = analyticsManager,
            fieldTransformerRegistry = fieldTransformerRegistry,
            order = orderRequest,
            amount = amount,
        )

        assertEquals("+31123456789", componentState.data.paymentMethod?.telephoneNumber)
        assertEquals(orderRequest, componentState.data.order)
        assertEquals(amount, componentState.data.amount)
        assertEquals(true, componentState.isInputValid)
        assertEquals(true, componentState.isReady)
    }

    @Test
    fun `when toComponentState is called with isValid false, then MBWayComponentState should be created correctly`() {
        val updatedState = initialState.copy(
            countryCodeFieldState = initialState.countryCodeFieldState.copy(
                validation = Validation.Invalid(0)
            ),
            localPhoneNumberFieldState = initialState.localPhoneNumberFieldState.copy(
                validation = Validation.Invalid(0)
            ),
        )

        val componentState = updatedState.toComponentState(
            analyticsManager = analyticsManager,
            fieldTransformerRegistry = fieldTransformerRegistry,
            order = orderRequest,
            amount = amount,
        )

        assertEquals("+31123456789", componentState.data.paymentMethod?.telephoneNumber)
        assertEquals(orderRequest, componentState.data.order)
        assertEquals(amount, componentState.data.amount)
        assertEquals(false, componentState.isInputValid)
        assertEquals(true, componentState.isReady)
    }

    @Test
    fun `when transform function changes phone number, then component state should reflect the change`() {
        fieldTransformerRegistry.setTransformation(MBWayFieldId.LOCAL_PHONE_NUMBER, "123456789", "987654321")

        val componentState = initialState.toComponentState(
            analyticsManager = analyticsManager,
            fieldTransformerRegistry = fieldTransformerRegistry,
            order = orderRequest,
            amount = amount,
        )

        assertEquals("+31987654321", componentState.data.paymentMethod?.telephoneNumber)
    }

    @Test
    fun `when toComponentState is called with null order and amount, then MBWayComponentState should be created correctly`() {
        val componentState = initialState.toComponentState(
            analyticsManager = analyticsManager,
            fieldTransformerRegistry = fieldTransformerRegistry,
            order = null,
            amount = null,
        )

        assertEquals("+31123456789", componentState.data.paymentMethod?.telephoneNumber)
        assertEquals(null, componentState.data.order)
        assertEquals(null, componentState.data.amount)
        assertEquals(true, componentState.isInputValid)
        assertEquals(true, componentState.isReady)
    }
}

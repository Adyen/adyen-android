/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package com.adyen.checkout.core.mbway.internal.model

import com.adyen.checkout.core.internal.ui.state.model.DelegateFieldState
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayStateUpdaterRegistry
import com.adyen.checkout.core.mbway.internal.ui.state.MBWayFieldId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MBWayStateUpdaterRegistryTest {

    private lateinit var stateUpdaterRegistry: MBWayStateUpdaterRegistry
    private lateinit var initialState: MBWayDelegateState
    private lateinit var localPhoneNumberFieldState: DelegateFieldState<String>
    private lateinit var countryCodeFieldState: DelegateFieldState<String>

    @BeforeEach
    fun setup() {
        localPhoneNumberFieldState = DelegateFieldState(value = "123456789")
        countryCodeFieldState = DelegateFieldState(value = "PT")

        initialState = MBWayDelegateState(
            countries = listOf("PT", "ES"),
            countryCodeFieldState = countryCodeFieldState,
            localPhoneNumberFieldState = localPhoneNumberFieldState,
        )

        stateUpdaterRegistry = MBWayStateUpdaterRegistry()
    }

    @Test
    fun `when getFieldState is called for phone number, then correct field state should be returned`() {
        val fieldState = stateUpdaterRegistry.getFieldState<String>(initialState, MBWayFieldId.PHONE_NUMBER)

        assertEquals(localPhoneNumberFieldState, fieldState)
    }

    @Test
    fun `when getFieldState is called for country code, then correct field state should be returned`() {
        val fieldState = stateUpdaterRegistry.getFieldState<String>(initialState, MBWayFieldId.COUNTRY_CODE)

        assertEquals(countryCodeFieldState, fieldState)
    }

    @Test
    fun `when updateFieldState is called for phone number, then field state should update correctly`() {
        val newPhoneNumberState = DelegateFieldState(value = "987654321")

        val updatedState = stateUpdaterRegistry.updateFieldState(
            initialState,
            MBWayFieldId.PHONE_NUMBER,
            newPhoneNumberState,
        )

        assertEquals(newPhoneNumberState, updatedState.localPhoneNumberFieldState)
    }

    @Test
    fun `when updateFieldState is called for country code, then field state should update correctly`() {
        val newCountryCodeState = DelegateFieldState(value = "PT")

        val updatedState = stateUpdaterRegistry.updateFieldState(
            initialState,
            MBWayFieldId.COUNTRY_CODE,
            newCountryCodeState,
        )

        assertEquals(newCountryCodeState, updatedState.countryCodeFieldState)
    }
}

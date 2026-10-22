/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AddressComponentStateTest {

    @Test
    fun `when the country is US then the form elements follow the US order`() {
        // GIVEN
        val state = createState(countryCode = "US", states = US_STATES)

        // WHEN
        val ids = state.form.elements.map { it.id }

        // THEN
        val expected = listOf(
            AddressFormElementId.COUNTRY,
            AddressFormElementId.STREET,
            AddressFormElementId.HOUSE_NUMBER_OR_NAME,
            AddressFormElementId.CITY,
            AddressFormElementId.STATE_OR_PROVINCE,
            AddressFormElementId.POSTAL_CODE,
        )
        assertEquals(expected, ids)
    }

    @Test
    fun `when the states are loading then the state element is in the form`() {
        // GIVEN
        val state = createState(countryCode = "US", states = emptyList())

        // WHEN
        val ids = state.form.elements.map { it.id }

        // THEN
        assertTrue(ids.contains(AddressFormElementId.STATE_OR_PROVINCE))
    }

    @Test
    fun `when the country has no states then there is no state element`() {
        // GIVEN
        val state = createState(countryCode = "NL", states = emptyList())

        // WHEN
        val ids = state.form.elements.map { it.id }

        // THEN
        assertFalse(ids.contains(AddressFormElementId.STATE_OR_PROVINCE))
    }

    private fun createState(countryCode: String, states: List<AddressRegion>) = AddressComponentState(
        countries = emptyList(),
        country = PickerInputComponentState(selected = countryCode),
        street = TextInputComponentState(),
        houseNumberOrName = TextInputComponentState(requirementPolicy = RequirementPolicy.Optional),
        postalCode = TextInputComponentState(),
        city = TextInputComponentState(),
        states = states,
        stateOrProvince = PickerInputComponentState(),
    )

    companion object {
        private val US_STATES = listOf(AddressRegion(code = "CA", name = "California"))
    }
}

/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.address.internal.ui.model.AddressSpec
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
        val state = createState(stateOrProvince = STATE_OPTIONS)

        // WHEN
        val ids = state.form.elements.map { it.id }

        // THEN
        val expected = listOf(
            AddressFormElementId.COUNTRY,
            AddressFormElementId.STREET,
            AddressFormElementId.HOUSE_NUMBER_OR_NAME,
            AddressFormElementId.CITY,
            AddressFormElementId.STATE_OR_PROVINCE_PICKER,
            AddressFormElementId.POSTAL_CODE,
        )
        assertEquals(expected, ids)
    }

    @Test
    fun `when the states are loading then the form has no state element`() {
        // GIVEN
        val state = createState(stateOrProvince = StateOrProvinceState.Loading(pendingCode = "CA"))

        // WHEN
        val ids = state.form.elements.map { it.id }

        // THEN
        assertFalse(ids.contains(AddressFormElementId.STATE_OR_PROVINCE_PICKER))
        assertFalse(ids.contains(AddressFormElementId.STATE_OR_PROVINCE_TEXT))
    }

    @Test
    fun `when the state dataset is empty then the form has no state element`() {
        // GIVEN
        val state = createState(stateOrProvince = StateOrProvinceState.Unavailable)

        // WHEN
        val ids = state.form.elements.map { it.id }

        // THEN
        assertFalse(ids.contains(AddressFormElementId.STATE_OR_PROVINCE_PICKER))
        assertFalse(ids.contains(AddressFormElementId.STATE_OR_PROVINCE_TEXT))
    }

    @Test
    fun `when the state dataset has options then the form has a state element`() {
        // GIVEN
        val state = createState(stateOrProvince = STATE_OPTIONS)

        // WHEN
        val ids = state.form.elements.map { it.id }

        // THEN
        assertTrue(ids.contains(AddressFormElementId.STATE_OR_PROVINCE_PICKER))
        assertFalse(ids.contains(AddressFormElementId.STATE_OR_PROVINCE_TEXT))
    }

    @Test
    fun `when the state request failed then the form has a free text state element`() {
        // GIVEN
        val state = createState(
            stateOrProvince = StateOrProvinceState.FreeText(input = TextInputComponentState()),
        )

        // WHEN
        val ids = state.form.elements.map { it.id }

        // THEN
        assertTrue(ids.contains(AddressFormElementId.STATE_OR_PROVINCE_TEXT))
        assertFalse(ids.contains(AddressFormElementId.STATE_OR_PROVINCE_PICKER))
    }

    private fun createState(stateOrProvince: StateOrProvinceState) = AddressComponentState(
        spec = AddressSpec.US,
        countries = emptyList(),
        isLoadingCountries = false,
        country = PickerInputComponentState(selected = "US"),
        street = TextInputComponentState(),
        houseNumberOrName = TextInputComponentState(requirementPolicy = RequirementPolicy.Optional),
        postalCode = TextInputComponentState(),
        city = TextInputComponentState(),
        stateOrProvince = stateOrProvince,
    )

    companion object {
        private val STATE_OPTIONS = StateOrProvinceState.Options(
            regions = listOf(AddressRegion(code = "CA", name = "California")),
            picker = PickerInputComponentState(),
        )
    }
}

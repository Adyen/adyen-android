/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.address.internal.ui.model

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource

internal class AddressSpecTest {

    @Test
    fun `when the country is BR then the fields are ordered street house number postal code city state`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("BR")

        // THEN
        val expected = listOf(
            AddressField.STREET,
            AddressField.HOUSE_NUMBER_OR_NAME,
            AddressField.POSTAL_CODE,
            AddressField.CITY,
            AddressField.STATE_OR_PROVINCE,
        )
        assertEquals(expected, spec.order)
    }

    @Test
    fun `when the country is CA then the fields are ordered street house number city postal code state`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("CA")

        // THEN
        val expected = listOf(
            AddressField.STREET,
            AddressField.HOUSE_NUMBER_OR_NAME,
            AddressField.CITY,
            AddressField.POSTAL_CODE,
            AddressField.STATE_OR_PROVINCE,
        )
        assertEquals(expected, spec.order)
    }

    @Test
    fun `when the country is GB then the fields are ordered house number street city postal code state`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("GB")

        // THEN
        val expected = listOf(
            AddressField.HOUSE_NUMBER_OR_NAME,
            AddressField.STREET,
            AddressField.CITY,
            AddressField.POSTAL_CODE,
            AddressField.STATE_OR_PROVINCE,
        )
        assertEquals(expected, spec.order)
    }

    @Test
    fun `when the country is US then the fields are ordered street house number city state postal code`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("US")

        // THEN
        val expected = listOf(
            AddressField.STREET,
            AddressField.HOUSE_NUMBER_OR_NAME,
            AddressField.CITY,
            AddressField.STATE_OR_PROVINCE,
            AddressField.POSTAL_CODE,
        )
        assertEquals(expected, spec.order)
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = ["NL", "AU", "DE"])
    fun `when the country is not supported then the default spec is used`(countryCode: String?) {
        // WHEN
        val spec = AddressSpec.fromCountryCode(countryCode)

        // THEN
        assertEquals(AddressSpec.DEFAULT, spec)
        val expectedOrder = listOf(
            AddressField.STREET,
            AddressField.HOUSE_NUMBER_OR_NAME,
            AddressField.POSTAL_CODE,
            AddressField.CITY,
            AddressField.STATE_OR_PROVINCE,
        )
        assertEquals(expectedOrder, spec.order)
    }

    @Test
    fun `when the country is CA then the house number is optional`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("CA")

        // THEN
        assertEquals(RequirementPolicy.Optional, spec.getFieldSpec(AddressField.HOUSE_NUMBER_OR_NAME).requirementPolicy)
    }

    @Test
    fun `when the country is US then the house number is optional`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("US")

        // THEN
        assertEquals(RequirementPolicy.Optional, spec.getFieldSpec(AddressField.HOUSE_NUMBER_OR_NAME).requirementPolicy)
    }

    @Test
    fun `when the country is BR then every field is required`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("BR")

        // THEN
        AddressField.entries.forEach { field ->
            assertEquals(
                RequirementPolicy.Required,
                spec.getFieldSpec(field).requirementPolicy,
                "$field should be required",
            )
        }
    }

    @Test
    fun `when the country is US then the postal code is labelled zip code`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("US")

        // THEN
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL_ZIP_CODE,
            spec.getFieldSpec(AddressField.POSTAL_CODE).label,
        )
    }

    @Test
    fun `when the country is CA then the street is labelled address and the house number apartment suite`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("CA")

        // THEN
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_STREET_LABEL_ADDRESS,
            spec.getFieldSpec(AddressField.STREET).label,
        )
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL_APARTMENT_SUITE,
            spec.getFieldSpec(AddressField.HOUSE_NUMBER_OR_NAME).label,
        )
    }

    @Test
    fun `when the country is GB then the city is labelled city and town`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("GB")

        // THEN
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_CITY_LABEL_CITY_TOWN,
            spec.getFieldSpec(AddressField.CITY).label,
        )
    }

    @Test
    fun `when the country is BR then the state is labelled state`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("BR")

        // THEN
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE,
            spec.getFieldSpec(AddressField.STATE_OR_PROVINCE).label,
        )
    }

    @Test
    fun `when the country is CA then the state is labelled province or territory`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("CA")

        // THEN
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_PROVINCE_OR_TERRITORY,
            spec.getFieldSpec(AddressField.STATE_OR_PROVINCE).label,
        )
    }
}

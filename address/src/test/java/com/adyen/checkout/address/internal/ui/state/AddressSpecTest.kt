/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource

internal class AddressSpecTest {

    @Test
    fun `when the country is BR then the fields are ordered country street house number postal code city state`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("BR")

        // THEN
        val expected = listOf(
            AddressFormElementId.COUNTRY,
            AddressFormElementId.STREET,
            AddressFormElementId.HOUSE_NUMBER_OR_NAME,
            AddressFormElementId.POSTAL_CODE,
            AddressFormElementId.CITY,
            AddressFormElementId.STATE_OR_PROVINCE,
        )
        assertEquals(expected, spec.order)
    }

    @Test
    fun `when the country is CA then the fields are ordered country street house number city postal code state`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("CA")

        // THEN
        val expected = listOf(
            AddressFormElementId.COUNTRY,
            AddressFormElementId.STREET,
            AddressFormElementId.HOUSE_NUMBER_OR_NAME,
            AddressFormElementId.CITY,
            AddressFormElementId.POSTAL_CODE,
            AddressFormElementId.STATE_OR_PROVINCE,
        )
        assertEquals(expected, spec.order)
    }

    @Test
    fun `when the country is GB then the fields are ordered country house number street city postal code state`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("GB")

        // THEN
        val expected = listOf(
            AddressFormElementId.COUNTRY,
            AddressFormElementId.HOUSE_NUMBER_OR_NAME,
            AddressFormElementId.STREET,
            AddressFormElementId.CITY,
            AddressFormElementId.POSTAL_CODE,
            AddressFormElementId.STATE_OR_PROVINCE,
        )
        assertEquals(expected, spec.order)
    }

    @Test
    fun `when the country is US then the fields are ordered country street house number city state postal code`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("US")

        // THEN
        val expected = listOf(
            AddressFormElementId.COUNTRY,
            AddressFormElementId.STREET,
            AddressFormElementId.HOUSE_NUMBER_OR_NAME,
            AddressFormElementId.CITY,
            AddressFormElementId.STATE_OR_PROVINCE,
            AddressFormElementId.POSTAL_CODE,
        )
        assertEquals(expected, spec.order)
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = ["NL", "DE"])
    fun `when the country is not supported then the default spec is used`(countryCode: String?) {
        // WHEN
        val spec = AddressSpec.fromCountryCode(countryCode)

        // THEN
        assertEquals(AddressSpec.DEFAULT, spec)
        val expectedOrder = listOf(
            AddressFormElementId.COUNTRY,
            AddressFormElementId.STREET,
            AddressFormElementId.HOUSE_NUMBER_OR_NAME,
            AddressFormElementId.POSTAL_CODE,
            AddressFormElementId.CITY,
            AddressFormElementId.STATE_OR_PROVINCE,
        )
        assertEquals(expectedOrder, spec.order)
    }

    @Test
    fun `when the country is AU then the fields are ordered country street house number city state postal code`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("AU")

        // THEN
        val expected = listOf(
            AddressFormElementId.COUNTRY,
            AddressFormElementId.STREET,
            AddressFormElementId.HOUSE_NUMBER_OR_NAME,
            AddressFormElementId.CITY,
            AddressFormElementId.STATE_OR_PROVINCE,
            AddressFormElementId.POSTAL_CODE,
        )
        assertEquals(expected, spec.order)
    }

    @Test
    fun `when the country is JP then the fields are ordered country postal code state city street house number`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("JP")

        // THEN
        val expected = listOf(
            AddressFormElementId.COUNTRY,
            AddressFormElementId.POSTAL_CODE,
            AddressFormElementId.STATE_OR_PROVINCE,
            AddressFormElementId.CITY,
            AddressFormElementId.STREET,
            AddressFormElementId.HOUSE_NUMBER_OR_NAME,
        )
        assertEquals(expected, spec.order)
    }

    @ParameterizedTest
    @ValueSource(strings = ["AU", "BR", "CA", "JP", "US"])
    fun `when the country has states then its state is required`(countryCode: String) {
        // WHEN
        val spec = AddressSpec.fromCountryCode(countryCode)

        // THEN
        assertTrue(spec.hasStates)
        assertEquals(
            RequirementPolicy.Required,
            spec.getFieldSpec(AddressFormElementId.STATE_OR_PROVINCE).requirementPolicy,
        )
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["NL", "GB", "DE"])
    fun `when the country has no states then its state is hidden`(countryCode: String?) {
        // WHEN
        val spec = AddressSpec.fromCountryCode(countryCode)

        // THEN
        assertFalse(spec.hasStates)
        assertEquals(
            RequirementPolicy.Hidden,
            spec.getFieldSpec(AddressFormElementId.STATE_OR_PROVINCE).requirementPolicy,
        )
    }

    @Test
    fun `when any country is selected then the country comes first and is required`() {
        AddressSpec.entries.forEach { spec ->
            assertEquals(AddressFormElementId.COUNTRY, spec.order.first(), "$spec should start with the country")
            assertEquals(
                CheckoutLocalizationKey.ADDRESS_COUNTRY_LABEL,
                spec.getFieldSpec(AddressFormElementId.COUNTRY).label,
                "$spec should label the country",
            )
            assertEquals(
                RequirementPolicy.Required,
                spec.getFieldSpec(AddressFormElementId.COUNTRY).requirementPolicy,
                "$spec should require the country",
            )
        }
    }

    @Test
    fun `when any country is selected then its spec lists every address field`() {
        AddressSpec.entries.forEach { spec ->
            assertEquals(
                AddressFormElementId.entries.toSet(),
                spec.order.toSet(),
                "$spec should list every address field",
            )
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["AU", "CA", "JP", "US"])
    fun `when the country is AU CA JP or US then the house number is optional`(countryCode: String) {
        // WHEN
        val spec = AddressSpec.fromCountryCode(countryCode)

        // THEN
        assertEquals(
            RequirementPolicy.Optional,
            spec.getFieldSpec(AddressFormElementId.HOUSE_NUMBER_OR_NAME).requirementPolicy,
        )
    }

    @Test
    fun `when the country is BR then every field is required`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("BR")

        // THEN
        AddressFormElementId.entries.forEach { id ->
            assertEquals(
                RequirementPolicy.Required,
                spec.getFieldSpec(id).requirementPolicy,
                "$id should be required",
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
            spec.getFieldSpec(AddressFormElementId.POSTAL_CODE).label,
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["AU", "CA"])
    fun `when the country is AU or CA then the street is labelled address and the house number apartment suite`(
        countryCode: String,
    ) {
        // WHEN
        val spec = AddressSpec.fromCountryCode(countryCode)

        // THEN
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_STREET_LABEL_ADDRESS,
            spec.getFieldSpec(AddressFormElementId.STREET).label,
        )
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL_APARTMENT_SUITE,
            spec.getFieldSpec(AddressFormElementId.HOUSE_NUMBER_OR_NAME).label,
        )
    }

    @Test
    fun `when the country is GB then the city is labelled city and town`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("GB")

        // THEN
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_CITY_LABEL_CITY_TOWN,
            spec.getFieldSpec(AddressFormElementId.CITY).label,
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["AU", "BR"])
    fun `when the country is AU or BR then the state is labelled state`(countryCode: String) {
        // WHEN
        val spec = AddressSpec.fromCountryCode(countryCode)

        // THEN
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE,
            spec.getFieldSpec(AddressFormElementId.STATE_OR_PROVINCE).label,
        )
    }

    @Test
    fun `when the country is CA then the state is labelled province or territory`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("CA")

        // THEN
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_PROVINCE_OR_TERRITORY,
            spec.getFieldSpec(AddressFormElementId.STATE_OR_PROVINCE).label,
        )
    }

    @Test
    fun `when the country is JP then the fields use the Japanese labels`() {
        // WHEN
        val spec = AddressSpec.fromCountryCode("JP")

        // THEN
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_PREFECTURE,
            spec.getFieldSpec(AddressFormElementId.STATE_OR_PROVINCE).label,
        )
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_CITY_LABEL_CITY_TOWN,
            spec.getFieldSpec(AddressFormElementId.CITY).label,
        )
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_STREET_LABEL_NAME_AND_BLOCK_NUMBER,
            spec.getFieldSpec(AddressFormElementId.STREET).label,
        )
        assertEquals(
            CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL_BUILDING_NAME_ROOM_NUMBER,
            spec.getFieldSpec(AddressFormElementId.HOUSE_NUMBER_OR_NAME).label,
        )
    }
}

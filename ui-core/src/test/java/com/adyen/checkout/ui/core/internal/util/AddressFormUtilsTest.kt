/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 13/1/2023.
 */

package com.adyen.checkout.ui.core.internal.util

import com.adyen.checkout.components.core.Address
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.ui.core.internal.data.model.AddressItem
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.model.AddressListItem
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import com.adyen.checkout.ui.core.internal.ui.model.Required
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

@Suppress("MaxLineLength")
internal class AddressFormUtilsTest {

    @Test
    fun markAddressListItemSelected_CodeProvided_ExpectItemWithCodeSelected() {
        val input = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false,
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false,
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false,
            ),
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false,
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = true,
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false,
            ),
        )
        assertEquals(expected, AddressFormUtils.markAddressListItemSelected(input, "US"))
    }

    @Test
    fun `when there's no item selected on address list and code not provided expect nothing selected`() {
        val input = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false,
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false,
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false,
            ),
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false,
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false,
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false,
            ),
        )
        assertEquals(expected, AddressFormUtils.markAddressListItemSelected(input))
    }

    @Test
    fun `when there's no item selected on address list and list not containing item with given code expect nothing selected`() {
        val input = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false,
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false,
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false,
            ),
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false,
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false,
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false,
            ),
        )
        assertEquals(expected, AddressFormUtils.markAddressListItemSelected(input, "TR"))
    }

    @Test
    fun `when Country Options are initialized that a Address Params is null ,Expect Empty List`() {
        val addressParams = AddressParams.None
        val inputCountryList = listOf(
            AddressItem(
                id = "CA",
                name = "Canada",
            ),
            AddressItem(
                id = "US",
                name = "United States",
            ),
            AddressItem(
                id = "GB",
                name = "United Kingdom",
            ),
        )
        val expected = emptyList<AddressListItem>()
        assertEquals(
            expected,
            AddressFormUtils.initializeCountryOptions(Locale.getDefault(), addressParams, inputCountryList),
        )
    }

    @Test
    fun `when Country Options are initialized that a Address Params is PostalCode, Expect Empty List`() {
        val addressParams = AddressParams.PostalCode(Required())
        val inputCountryList = listOf(
            AddressItem(
                id = "CA",
                name = "Canada",
            ),
            AddressItem(
                id = "US",
                name = "United States",
            ),
            AddressItem(
                id = "GB",
                name = "United Kingdom",
            ),
        )
        val expected = emptyList<AddressListItem>()
        assertEquals(
            expected,
            AddressFormUtils.initializeCountryOptions(Locale.getDefault(), addressParams, inputCountryList),
        )
    }

    @Test
    fun `initialize country options, address configuration is full address without default country code and locale with country that is not supported expect list with nothing selected`() {
        val addressParams = AddressParams.FullAddress(addressFieldPolicy = Required())
        val inputCountryList = listOf(
            AddressItem(
                id = "CA",
                name = "Canada",
            ),
            AddressItem(
                id = "US",
                name = "United States",
            ),
            AddressItem(
                id = "GB",
                name = "United Kingdom",
            ),
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false,
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false,
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false,
            ),
        )
        assertEquals(
            expected,
            AddressFormUtils.initializeCountryOptions(Locale.GERMANY, addressParams, inputCountryList),
        )
    }

    @Test
    fun `when country options address configuration is full, address without default country code and locale with country that is supported expect list with locale country selected`() {
        val addressParams = AddressParams.FullAddress(addressFieldPolicy = Required())
        val inputCountryList = listOf(
            AddressItem(
                id = "CA",
                name = "Canada",
            ),
            AddressItem(
                id = "US",
                name = "United States",
            ),
            AddressItem(
                id = "GB",
                name = "United Kingdom",
            ),
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false,
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = true,
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false,
            ),
        )
        assertEquals(
            expected,
            AddressFormUtils.initializeCountryOptions(Locale.US, addressParams, inputCountryList),
        )
    }

    @Test
    fun `initializeCountryOptions_AddressConfigurationIsFullAddressWithDefaultCountryCode_ExpectListWithItemHavingDefaultCountryCodeSelected`() {
        val addressParams = AddressParams.FullAddress(defaultCountryCode = "GB", addressFieldPolicy = Required())
        val inputCountryList = listOf(
            AddressItem(
                id = "CA",
                name = "Canada",
            ),
            AddressItem(
                id = "US",
                name = "United States",
            ),
            AddressItem(
                id = "GB",
                name = "United Kingdom",
            ),
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false,
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false,
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = true,
            ),
        )
        assertEquals(
            expected,
            AddressFormUtils.initializeCountryOptions(Locale.getDefault(), addressParams, inputCountryList),
        )
    }

    /**
     * Assumes [initializeCountryOptions_AddressConfigurationIsFullAddressWithoutDefaultCountryCode_ExpectListWithFirstItemSelected].
     */
    @Test
    fun `initializeCountryOptions_AddressConfigurationIsFullAddressWithSupportedCountryCodes_ExpectListFilteredBySupportedCountryCodes`() {
        val addressParams =
            AddressParams.FullAddress(supportedCountryCodes = listOf("CA", "GB"), addressFieldPolicy = Required())
        val inputCountryList = listOf(
            AddressItem(
                id = "CA",
                name = "Canada",
            ),
            AddressItem(
                id = "US",
                name = "United States",
            ),
            AddressItem(
                id = "GB",
                name = "United Kingdom",
            ),
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false,
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false,
            ),
        )
        assertEquals(
            expected,
            AddressFormUtils.initializeCountryOptions(Locale.getDefault(), addressParams, inputCountryList),
        )
    }

    @Test
    fun `initialize state options expect list with nothing selected`() {
        val input = listOf(
            AddressItem(
                id = "AL",
                name = "Alabama",
            ),
            AddressItem(
                id = "MA",
                name = "Massachusetts",
            ),
            AddressItem(
                id = "NY",
                name = "New York",
            ),
        )
        val expected = listOf(
            AddressListItem(
                code = "AL",
                name = "Alabama",
                selected = false,
            ),
            AddressListItem(
                code = "MA",
                name = "Massachusetts",
                selected = false,
            ),
            AddressListItem(
                code = "NY",
                name = "New York",
                selected = false,
            ),
        )
        assertEquals(expected, AddressFormUtils.initializeStateOptions(input))
    }

    @Test
    fun isAddressRequired_AddressFormUIStateIsNONE_ExpectFalse() {
        val addressFormUIState = AddressFormUIState.NONE
        val expected = false
        assertEquals(expected, AddressFormUtils.isAddressRequired(addressFormUIState))
    }

    @Test
    fun isAddressRequired_AddressFormUIStateIsPOSTAL_CODE_ExpectTrue() {
        val addressFormUIState = AddressFormUIState.POSTAL_CODE
        val expected = true
        assertEquals(expected, AddressFormUtils.isAddressRequired(addressFormUIState))
    }

    @Test
    fun isAddressRequired_AddressFormUIStateIsFULL_ADDRESS_ExpectTrue() {
        val addressFormUIState = AddressFormUIState.FULL_ADDRESS
        val expected = true
        assertEquals(expected, AddressFormUtils.isAddressRequired(addressFormUIState))
    }

    @Test
    fun makeHouseNumberOrName_HouseNumberAndApartmentSuiteNotEmpty_ExpectStringsJoinedByEmptySpace() {
        val houseNumber = "12"
        val apartmentSuite = "3b"
        assertEquals("12 3b", AddressFormUtils.makeHouseNumberOrName(houseNumber, apartmentSuite))
    }

    @Test
    fun makeHouseNumberOrName_HouseNumberIsNotEmptyAndApartmentSuiteEmpty_ExpectHouseNumber() {
        val houseNumber = "12"
        val apartmentSuite = ""
        assertEquals("12", AddressFormUtils.makeHouseNumberOrName(houseNumber, apartmentSuite))
    }

    @Test
    fun makeHouseNumberOrName_HouseNumberIsEmptyAndApartmentSuiteIsNotEmpty_ExpectApartmentSuite() {
        val houseNumber = ""
        val apartmentSuite = "3b"
        assertEquals("3b", AddressFormUtils.makeHouseNumberOrName(houseNumber, apartmentSuite))
    }

    @ParameterizedTest
    @MethodSource("makeAddressDataSource")
    fun `when makeAddressData is called it returns the correct Address object`(
        addressOutputData: AddressOutputData,
        addressFormUIState: AddressFormUIState,
        expectedAddress: Address?,
    ) {
        val actual = AddressFormUtils.makeAddressData(addressOutputData, addressFormUIState)
        assertEquals(expectedAddress, actual)
    }

    companion object {
        private val TEST_ADDRESS_OUTPUT_DATA = AddressOutputData(
            postalCode = FieldState("postalCode", Validation.Valid),
            houseNumberOrName = FieldState("houseNumberOrName", Validation.Valid),
            apartmentSuite = FieldState("", Validation.Valid),
            street = FieldState("street", Validation.Valid),
            city = FieldState("city", Validation.Valid),
            stateOrProvince = FieldState("stateOrProvince", Validation.Valid),
            country = FieldState("country", Validation.Valid),
            isOptional = false,
            countryOptions = emptyList(),
            stateOptions = emptyList(),
            countryDisplayName = "",
        )

        @JvmStatic
        fun makeAddressDataSource() = listOf(
            // addressOutputData, addressFormUIState, expected Address object
            arguments(TEST_ADDRESS_OUTPUT_DATA, AddressFormUIState.NONE, null),
            arguments(
                TEST_ADDRESS_OUTPUT_DATA,
                AddressFormUIState.POSTAL_CODE,
                Address(
                    postalCode = "postalCode",
                    street = Address.ADDRESS_NULL_PLACEHOLDER,
                    stateOrProvince = Address.ADDRESS_NULL_PLACEHOLDER,
                    houseNumberOrName = Address.ADDRESS_NULL_PLACEHOLDER,
                    city = Address.ADDRESS_NULL_PLACEHOLDER,
                    country = Address.ADDRESS_COUNTRY_NULL_PLACEHOLDER,
                ),
            ),
            arguments(
                TEST_ADDRESS_OUTPUT_DATA,
                AddressFormUIState.FULL_ADDRESS,
                Address(
                    postalCode = "postalCode",
                    street = "street",
                    stateOrProvince = "stateOrProvince",
                    houseNumberOrName = "houseNumberOrName",
                    city = "city",
                    country = "country",
                ),
            ),
            arguments(
                TEST_ADDRESS_OUTPUT_DATA,
                AddressFormUIState.LOOKUP,
                Address(
                    postalCode = "postalCode",
                    street = "street",
                    stateOrProvince = "stateOrProvince",
                    houseNumberOrName = "houseNumberOrName",
                    city = "city",
                    country = "country",
                ),
            ),
        )
    }
}

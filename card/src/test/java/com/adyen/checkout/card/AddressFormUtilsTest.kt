package com.adyen.checkout.card

import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.card.util.AddressFormUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength")
internal class AddressFormUtilsTest {

    @Test
    fun markAddressListItemSelected_CodeProvided_ExpectItemWithCodeSelected() {
        val input = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false
            )
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = true
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false
            )
        )
        assertEquals(expected, AddressFormUtils.markAddressListItemSelected(input, "US"))
    }

    @Test
    fun markAddressListItemSelected_CodeNotProvided_ExpectFirstItemSelected() {
        val input = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false
            )
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = true
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false
            )
        )
        assertEquals(expected, AddressFormUtils.markAddressListItemSelected(input))
    }

    @Test
    fun markAddressListItemSelected_ListNotContainingItemWithGivenCode_ExpectFirstItemSelected() {
        val input = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false
            )
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = true
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false
            )
        )
        assertEquals(expected, AddressFormUtils.markAddressListItemSelected(input, "TR"))
    }

    @Test
    fun `when Country Options are initialized that a Address Params is null ,Expect Empty List`() {
        val addressParams = AddressParams.None
        val inputCountryList = listOf(
            AddressItem(
                id = "CA",
                name = "Canada"
            ),
            AddressItem(
                id = "US",
                name = "United States"
            ),
            AddressItem(
                id = "GB",
                name = "United Kingdom"
            )
        )
        val expected = emptyList<AddressListItem>()
        assertEquals(
            expected,
            AddressFormUtils.initializeCountryOptions(addressParams, inputCountryList)
        )
    }

    @Test
    fun `when Country Options are initialized that a Address Params is PostalCode, Expect Empty List`() {
        val addressParams = AddressParams.PostalCode(Required())
        val inputCountryList = listOf(
            AddressItem(
                id = "CA",
                name = "Canada"
            ),
            AddressItem(
                id = "US",
                name = "United States"
            ),
            AddressItem(
                id = "GB",
                name = "United Kingdom"
            )
        )
        val expected = emptyList<AddressListItem>()
        assertEquals(
            expected,
            AddressFormUtils.initializeCountryOptions(addressParams, inputCountryList)
        )
    }

    @Test
    fun `initializeCountryOptions_AddressConfigurationIsFullAddressWithoutDefaultCountryCode_ExpectListWithFirstItemSelected`() {
        val addressParams = AddressParams.FullAddress(addressFieldPolicy = Required())
        val inputCountryList = listOf(
            AddressItem(
                id = "CA",
                name = "Canada"
            ),
            AddressItem(
                id = "US",
                name = "United States"
            ),
            AddressItem(
                id = "GB",
                name = "United Kingdom"
            )
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = true
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false
            )
        )
        assertEquals(
            expected,
            AddressFormUtils.initializeCountryOptions(addressParams, inputCountryList)
        )
    }

    @Test
    fun `initializeCountryOptions_AddressConfigurationIsFullAddressWithDefaultCountryCode_ExpectListWithItemHavingDefaultCountryCodeSelected`() {
        val addressParams = AddressParams.FullAddress(defaultCountryCode = "GB", addressFieldPolicy = Required())
        val inputCountryList = listOf(
            AddressItem(
                id = "CA",
                name = "Canada"
            ),
            AddressItem(
                id = "US",
                name = "United States"
            ),
            AddressItem(
                id = "GB",
                name = "United Kingdom"
            )
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = false
            ),
            AddressListItem(
                name = "United States",
                code = "US",
                selected = false
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = true
            )
        )
        assertEquals(
            expected,
            AddressFormUtils.initializeCountryOptions(addressParams, inputCountryList)
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
                name = "Canada"
            ),
            AddressItem(
                id = "US",
                name = "United States"
            ),
            AddressItem(
                id = "GB",
                name = "United Kingdom"
            )
        )
        val expected = listOf(
            AddressListItem(
                name = "Canada",
                code = "CA",
                selected = true
            ),
            AddressListItem(
                name = "United Kingdom",
                code = "GB",
                selected = false
            )
        )
        assertEquals(
            expected,
            AddressFormUtils.initializeCountryOptions(addressParams, inputCountryList)
        )
    }

    @Test
    fun initializeStateOptions_ExpectListWithFirstItemSelected() {
        val input = listOf(
            AddressItem(
                id = "AL",
                name = "Alabama"
            ),
            AddressItem(
                id = "MA",
                name = "Massachusetts"
            ),
            AddressItem(
                id = "NY",
                name = "New York"
            )
        )
        val expected = listOf(
            AddressListItem(
                code = "AL",
                name = "Alabama",
                selected = true
            ),
            AddressListItem(
                code = "MA",
                name = "Massachusetts",
                selected = false
            ),
            AddressListItem(
                code = "NY",
                name = "New York",
                selected = false
            )
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
}

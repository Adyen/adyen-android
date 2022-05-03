package com.adyen.checkout.card.util

import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.AddressFormUIState
import com.adyen.checkout.card.AddressOutputData
import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.components.base.AddressVisibility
import com.adyen.checkout.components.model.payments.request.Address

internal object AddressFormUtils {

    /**
     * Map a list of [AddressItem] to a list of [AddressListItem].
     *
     * @param list Input list.
     * @param shouldSelectFirstItem Whether or not the first item should be marked as selected.
     *
     * @return Mapped list of [AddressListItem].
     */
    fun mapToListItem(list: List<AddressItem>, shouldSelectFirstItem: Boolean): List<AddressListItem> {
        return list.map {
            val isFirstItem = it.id == list.firstOrNull()?.id
            AddressListItem(
                name = it.name.orEmpty(),
                code = it.id.orEmpty(),
                selected = shouldSelectFirstItem && isFirstItem
            )
        }
    }

    /**
     * Mark the item that matches the given code as selected in the given input list.
     *
     * @param list Input list of [AddressListItem].
     * @param code Country or state code to be marked as selected.
     *
     * @return List of [AddressListItem] with the item in the list having given code marked as selected.
     */
    fun markAddressListItemSelected(list: List<AddressListItem>, code: String): List<AddressListItem> {
        return list.map {
            it.copy(selected = it.code == code)
        }
    }

    /**
     * Get visibility state of the address form.
     *
     * @param addressConfiguration Configuration object for address form.
     * @param addressVisibility Visibility modifier for address form.
     *
     * @return Visibility state of the address form.
     */
    fun getAddressFormUIState(
        addressConfiguration: AddressConfiguration?,
        addressVisibility: AddressVisibility
    ): AddressFormUIState {
        return when {
            addressConfiguration != null -> AddressFormUIState.fromAddressConfiguration(addressConfiguration)
            else -> AddressFormUIState.fromAddressVisibility(addressVisibility)
        }
    }

    /**
     * Initialize country options using [AddressConfiguration].
     *
     * First filter if there's [AddressConfiguration.FullAddress.supportedCountryCodes] defined in the
     * configuration. Then mark [AddressConfiguration.FullAddress.defaultCountryCode] as selected if it
     * is defined in configuration and exists in the filtered country list. Otherwise mark first item
     * in the list as selected.
     *
     * @param addressConfiguration Configuration object.
     * @param countryList List of countries from API.
     *
     * @return Country options.
     */
    fun initializeCountryOptions(addressConfiguration: AddressConfiguration?, countryList: List<AddressItem>): List<AddressListItem> {
        return when (addressConfiguration) {
            is AddressConfiguration.FullAddress -> {
                val filteredCountryList = if (addressConfiguration.supportedCountryCodes.isNotEmpty()) {
                    countryList.filter { countryItem -> addressConfiguration.supportedCountryCodes.any { it == countryItem.id } }
                } else {
                    countryList
                }

                val defaultCountryCode = addressConfiguration.defaultCountryCode
                if (defaultCountryCode != null && filteredCountryList.any { it.id == defaultCountryCode }) {
                    markAddressListItemSelected(mapToListItem(filteredCountryList, false), defaultCountryCode)
                } else {
                    mapToListItem(filteredCountryList, true)
                }
            }
            else -> emptyList()
        }
    }

    /**
     * Check whether the address is required for creation of Payment Component Data.
     *
     * @param addressFormUIState UI state of the form.
     *
     * @return Whether if address data is required or not.
     */
    fun isAddressRequired(addressFormUIState: AddressFormUIState): Boolean {
        return addressFormUIState != AddressFormUIState.NONE
    }

    /**
     * Make [Address] object from the output data.
     *
     * @param addressOutputData Output data object storing the data from the form.
     * @param addressFormUIState UI state of the form.
     *
     * @return [Address] object to be passed to the merchant as part of Payment Component Data.
     */
    fun makeAddressData(addressOutputData: AddressOutputData, addressFormUIState: AddressFormUIState): Address? {
        return when (addressFormUIState) {
            AddressFormUIState.FULL_ADDRESS -> Address().apply {
                postalCode = addressOutputData.postalCode.value
                street = addressOutputData.street.value
                stateOrProvince = addressOutputData.stateOrProvince.value
                houseNumberOrName = makeHouseNumberOrName(
                    addressOutputData.houseNumberOrName.value,
                    addressOutputData.apartmentSuite.value
                )
                city = addressOutputData.city.value
                country = addressOutputData.country.value
            }
            AddressFormUIState.POSTAL_CODE -> Address().apply {
                postalCode = addressOutputData.postalCode.value
                street = Address.ADDRESS_NULL_PLACEHOLDER
                stateOrProvince = Address.ADDRESS_NULL_PLACEHOLDER
                houseNumberOrName = Address.ADDRESS_NULL_PLACEHOLDER
                city = Address.ADDRESS_NULL_PLACEHOLDER
                country = Address.ADDRESS_COUNTRY_NULL_PLACEHOLDER
            }
            else -> null
        }
    }

    /**
     * Make [Address.houseNumberOrName] field using houseNumberOrName and apartmentSuite fields.
     *
     * Concat two fields and separate them with an empty space if both exists.
     *
     * @param houseNumberOrName
     * @param apartmentSuite
     *
     * @return Resulting houseNumberOrName string.
     */
    fun makeHouseNumberOrName(houseNumberOrName: String, apartmentSuite: String): String {
        return listOf(houseNumberOrName, apartmentSuite).filter { it.isNotEmpty() }
            .joinToString(" ")
    }
}

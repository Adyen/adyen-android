package com.adyen.checkout.card.util

import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.AddressFormUIState
import com.adyen.checkout.card.AddressOutputData
import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.components.base.AddressVisibility
import com.adyen.checkout.components.model.payments.request.Address

// TODO docs
internal object AddressFormUtils {

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

    fun markAddressListItemSelected(countryList: List<AddressListItem>, countryCode: String): List<AddressListItem> {
        return countryList.map {
            it.copy(selected = it.code == countryCode)
        }
    }

    fun getAddressFormUIState(addressConfiguration: AddressConfiguration?, addressVisibility: AddressVisibility, isStoredCard: Boolean): AddressFormUIState {
        return when {
            isStoredCard -> AddressFormUIState.NONE
            addressConfiguration != null -> AddressFormUIState.fromAddressConfiguration(addressConfiguration)
            else -> AddressFormUIState.fromAddressVisibility(addressVisibility)
        }
    }

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

    fun isAddressRequired(addressFormUIState: AddressFormUIState): Boolean {
        return addressFormUIState != AddressFormUIState.NONE
    }

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

    fun makeHouseNumberOrName(houseNumberOrName: String, apartmentSuite: String): String {
        return listOf(houseNumberOrName, apartmentSuite).filter { it.isNotEmpty() }
            .joinToString(" ")
    }
}

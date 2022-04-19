package com.adyen.checkout.card.util

import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.AddressFormUIState
import com.adyen.checkout.card.AddressOutputData
import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.components.base.AddressVisibility

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

    fun getAddressFormUIState(addressConfiguration: AddressConfiguration?, addressVisibility: AddressVisibility): AddressFormUIState {
        return addressConfiguration?.let { AddressFormUIState.fromAddressConfiguration(addressConfiguration) }
            ?: AddressFormUIState.fromAddressVisibility(addressVisibility)
    }

    fun initializeCountryOptions(addressConfiguration: AddressConfiguration?, countryList: List<AddressItem>) : List<AddressListItem> {
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
}
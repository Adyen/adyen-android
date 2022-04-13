package com.adyen.checkout.card.util

import com.adyen.checkout.card.AddressOutputData
import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.card.ui.model.AddressListItem

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

//    fun makeInitialCountryList(countries: List<AddressItem>, addressConfiguration: AddressConfiguration): List<AddressListItem> {
//        when (addressConfiguration) {
//            is AddressConfiguration.FullAddress -> {
//                addressConfiguration.defaultCountryCode?.let {
//                    countries.mapToListItem1(false).markCountrySelected1(it)
//                } ?: countries.mapToListItem1(true)
//            }
//            else -> {}
//        }
//    }
//
//    fun List<AddressListItem>.markCountrySelected1(countryCode: String): List<AddressListItem> {
//        return map {
//            it.copy(selected = it.code == countryCode)
//        }
//    }
//
//    fun List<AddressItem>.mapToListItem1(shouldSelectFirstItem: Boolean): List<AddressListItem> {
//        return map {
//            val isFirstItem = it.id == firstOrNull()?.id
//            AddressListItem(
//                name = it.name.orEmpty(),
//                code = it.id.orEmpty(),
//                selected = shouldSelectFirstItem && isFirstItem
//            )
//        }
//    }

}
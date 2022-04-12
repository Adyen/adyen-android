package com.adyen.checkout.card.util

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

    fun markCountrySelected(countryList: List<AddressListItem>, countryCode: String): List<AddressListItem> {
        return countryList.map {
            it.copy(selected = it.code == countryCode)
        }
    }

}
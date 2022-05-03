/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/3/2022.
 */

package com.adyen.checkout.card

import android.util.LruCache
import com.adyen.checkout.card.api.AddressDataType
import com.adyen.checkout.card.api.makeUrl
import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.card.repository.AddressRepository
import com.adyen.checkout.card.ui.AddressFormInput
import com.adyen.checkout.components.base.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AddressDelegate(
    private val addressRepository: AddressRepository
) {

    private val _statesFlow: MutableStateFlow<List<AddressItem>> = MutableStateFlow(emptyList())
    internal val statesFlow: Flow<List<AddressItem>> = _statesFlow

    private val cache: LruCache<String, List<AddressItem>> = LruCache<String, List<AddressItem>>(CACHE_ENTRY_SIZE)

    fun getStateList(
        configuration: Configuration,
        countryCode: String?,
        coroutineScope: CoroutineScope
    ) {
        val addressSpecification = AddressFormInput.AddressSpecification.fromString(countryCode)
        val needsStates = addressSpecification == AddressFormInput.AddressSpecification.BR ||
            addressSpecification == AddressFormInput.AddressSpecification.US ||
            addressSpecification == AddressFormInput.AddressSpecification.CA
        if (!countryCode.isNullOrEmpty() && needsStates) {
            val url = makeUrl(
                configuration.environment,
                AddressDataType.STATE,
                configuration.shopperLocale.toLanguageTag(),
                countryCode
            )
            cache[url]?.let {
                _statesFlow.tryEmit(it)
            } ?: coroutineScope.launch {
                val states = addressRepository.getAddressData(
                    environment = configuration.environment,
                    dataType = AddressDataType.STATE,
                    localeString = configuration.shopperLocale.toLanguageTag(),
                    countryCode = countryCode
                )
                if (states.isNotEmpty()) {
                    cache.put(url, states)
                }
                _statesFlow.tryEmit(states)
            }
        }
    }

    suspend fun getCountryList(configuration: Configuration): List<AddressItem> {
        val url = makeUrl(
            environment = configuration.environment,
            dataType = AddressDataType.COUNRTY,
            localeString = configuration.shopperLocale.toLanguageTag()
        )
        return cache[url] ?: run {
            val countries = addressRepository.getAddressData(
                environment = configuration.environment,
                dataType = AddressDataType.COUNRTY,
                localeString = configuration.shopperLocale.toLanguageTag()
            )
            if (countries.isNotEmpty()) {
                cache.put(url, countries)
            }
            countries
        }
    }

    companion object {
        // Only US, CA and BR has states and there's only one countries list.
        private const val CACHE_ENTRY_SIZE = 4
    }
}

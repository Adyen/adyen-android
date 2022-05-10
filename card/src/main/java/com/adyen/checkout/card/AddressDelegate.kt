/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/3/2022.
 */

package com.adyen.checkout.card

import android.util.LruCache
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
        val needsStates = COUNTRIES_WITH_STATES.contains(addressSpecification)
        if (!countryCode.isNullOrEmpty() && needsStates) {
            cache[countryCode]?.let {
                _statesFlow.tryEmit(it)
            } ?: coroutineScope.launch {
                addressRepository.getStates(
                    environment = configuration.environment,
                    shopperLocale = configuration.shopperLocale,
                    countryCode = countryCode
                ).fold(
                    onSuccess = { states ->
                        if (states.isNotEmpty()) {
                            cache.put(countryCode, states)
                        }
                        _statesFlow.tryEmit(states)
                    },
                    onFailure = { _statesFlow.tryEmit(emptyList()) }
                )

            }
        } else {
            _statesFlow.tryEmit(emptyList())
        }
    }

    suspend fun getCountryList(configuration: Configuration): List<AddressItem> {
        return cache[COUNTRIES_CACHE_KEY] ?: run {
            addressRepository.getCountries(
                environment = configuration.environment,
                shopperLocale = configuration.shopperLocale
            ).fold(
                onSuccess = { countries ->
                    if (countries.isNotEmpty()) {
                        cache.put(COUNTRIES_CACHE_KEY, countries)
                    }
                    countries
                },
                onFailure = {
                    emptyList()
                }
            )
        }
    }

    companion object {
        private val COUNTRIES_WITH_STATES = listOf(
            AddressFormInput.AddressSpecification.BR,
            AddressFormInput.AddressSpecification.CA,
            AddressFormInput.AddressSpecification.US
        )
        // Only US, CA and BR has states and there's only one countries list.
        private val CACHE_ENTRY_SIZE = COUNTRIES_WITH_STATES.size + 1
        private const val COUNTRIES_CACHE_KEY = "countries"
    }
}

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/8/2022.
 */

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/3/2022.
 */

package com.adyen.checkout.card.delegate

import android.util.LruCache
import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.card.repository.AddressRepository
import com.adyen.checkout.card.ui.AddressFormInput
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class AddressDelegate(
    private val addressRepository: AddressRepository
) {

    private val _statesFlow: MutableStateFlow<List<AddressItem>> = MutableStateFlow(emptyList())
    internal val statesFlow: Flow<List<AddressItem>> = _statesFlow

    private val _countriesFlow: MutableStateFlow<List<AddressItem>> = MutableStateFlow(emptyList())
    internal val countriesFlow: Flow<List<AddressItem>> = _countriesFlow

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
            } ?: run {
                fetchStateList(
                    configuration.environment,
                    configuration.shopperLocale,
                    countryCode,
                    coroutineScope
                )
            }
        } else {
            _statesFlow.tryEmit(emptyList())
        }
    }

    private fun fetchStateList(
        environment: Environment,
        shopperLocale: Locale,
        countryCode: String,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch {
            val states = addressRepository.getStates(
                environment = environment,
                shopperLocale = shopperLocale,
                countryCode = countryCode
            ).fold(
                onSuccess = { states ->
                    if (states.isNotEmpty()) {
                        cache.put(countryCode, states)
                    }
                    states
                },
                onFailure = { emptyList() }
            )
            _statesFlow.tryEmit(states)
        }
    }

    fun getCountryList(configuration: Configuration, coroutineScope: CoroutineScope) {
        cache[COUNTRIES_CACHE_KEY]?.let {
            _countriesFlow.tryEmit(it)
        } ?: run {
            fetchCountryList(
                configuration.environment,
                configuration.shopperLocale,
                coroutineScope
            )
        }
    }

    private fun fetchCountryList(environment: Environment, shopperLocale: Locale, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            val countries = addressRepository.getCountries(
                environment = environment,
                shopperLocale = shopperLocale
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
            _countriesFlow.tryEmit(countries)
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

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
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.repository.AddressRepository
import com.adyen.checkout.components.base.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class AddressDelegate(
    private val addressRepository: AddressRepository
) {

    private val _statesFlow: MutableSharedFlow<List<AddressItem>> = MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST)
    internal val statesFlow: Flow<List<AddressItem>> = _statesFlow

    private val cache: LruCache<String, List<AddressItem>> = LruCache<String, List<AddressItem>>(20)

    fun getStateList(configuration: Configuration, countryCode: String, coroutineScope: CoroutineScope): List<AddressItem> {
        val url = makeUrl(
            configuration.environment,
            AddressDataType.STATE,
            configuration.shopperLocale.toLanguageTag(),
            countryCode
        )
        coroutineScope.launch {
            val states = addressRepository.getAddressData(
                environment = configuration.environment,
                dataType = AddressDataType.STATE,
                localeString = configuration.shopperLocale.toLanguageTag(),
                countryCode = countryCode)
            _statesFlow.tryEmit(states)
            if (states.isNotEmpty()) {
                cache.put(url, states)
            }
        }
        return cache[url].orEmpty()
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
                localeString = configuration.shopperLocale.toLanguageTag())
            if (countries.isNotEmpty()) {
                cache.put(url, countries)
            }
            countries
        }
    }

}
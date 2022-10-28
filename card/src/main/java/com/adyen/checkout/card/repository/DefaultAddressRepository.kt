/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/8/2022.
 */

package com.adyen.checkout.card.repository

import com.adyen.checkout.card.api.AddressService
import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.card.ui.AddressFormInput
import com.adyen.checkout.card.ui.AddressSpecification
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.runSuspendCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Locale

internal class DefaultAddressRepository : AddressRepository {

    private val statesChannel: Channel<List<AddressItem>> = bufferedChannel()
    override val statesFlow: Flow<List<AddressItem>> = statesChannel.receiveAsFlow()

    private val countriesChannel: Channel<List<AddressItem>> = bufferedChannel()
    override val countriesFlow: Flow<List<AddressItem>> = countriesChannel.receiveAsFlow()

    private val cache: HashMap<String, List<AddressItem>> = hashMapOf()

    override fun getStateList(
        configuration: Configuration,
        countryCode: String?,
        coroutineScope: CoroutineScope
    ) {
        val addressSpecification = AddressSpecification.fromString(countryCode)
        val needsStates = COUNTRIES_WITH_STATES.contains(addressSpecification)
        if (!countryCode.isNullOrEmpty() && needsStates) {
            cache[countryCode]?.let {
                statesChannel.trySend(it)
            } ?: run {
                fetchStateList(
                    configuration.environment,
                    configuration.shopperLocale,
                    countryCode,
                    coroutineScope
                )
            }
        } else {
            statesChannel.trySend(emptyList())
        }
    }

    private fun fetchStateList(
        environment: Environment,
        shopperLocale: Locale,
        countryCode: String,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val states = getStates(
                environment = environment,
                shopperLocale = shopperLocale,
                countryCode = countryCode
            ).fold(
                onSuccess = { states ->
                    if (states.isNotEmpty()) {
                        cache[countryCode] = states
                    }
                    states
                },
                onFailure = { emptyList() }
            )
            statesChannel.trySend(states)
        }
    }

    override fun getCountryList(configuration: Configuration, coroutineScope: CoroutineScope) {
        cache[COUNTRIES_CACHE_KEY]?.let {
            countriesChannel.trySend(it)
        } ?: run {
            fetchCountryList(
                configuration.environment,
                configuration.shopperLocale,
                coroutineScope
            )
        }
    }

    private fun fetchCountryList(environment: Environment, shopperLocale: Locale, coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            val countries = getCountries(
                environment = environment,
                shopperLocale = shopperLocale
            ).fold(
                onSuccess = { countries ->
                    if (countries.isNotEmpty()) {
                        cache[COUNTRIES_CACHE_KEY] = countries
                    }
                    countries
                },
                onFailure = {
                    emptyList()
                }
            )
            countriesChannel.trySend(countries)
        }
    }

    private suspend fun getCountries(
        environment: Environment,
        shopperLocale: Locale
    ): Result<List<AddressItem>> = runSuspendCatching {
        Logger.d(TAG, "getting country list")
        return@runSuspendCatching AddressService(environment.baseUrl).getCountries(shopperLocale.toLanguageTag())
    }

    private suspend fun getStates(
        environment: Environment,
        shopperLocale: Locale,
        countryCode: String
    ): Result<List<AddressItem>> = runSuspendCatching {
        Logger.d(TAG, "getting state list for $countryCode")
        return@runSuspendCatching AddressService(environment.baseUrl)
            .getStates(shopperLocale.toLanguageTag(), countryCode)
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private val COUNTRIES_WITH_STATES = listOf(
            AddressSpecification.BR,
            AddressSpecification.CA,
            AddressSpecification.US
        )
        private const val COUNTRIES_CACHE_KEY = "countries"
    }
}

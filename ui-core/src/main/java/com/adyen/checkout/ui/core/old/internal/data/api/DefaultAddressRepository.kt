/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.DispatcherProvider
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.core.old.internal.util.runSuspendCatching
import com.adyen.checkout.ui.core.internal.ui.AddressSpecification
import com.adyen.checkout.ui.core.old.internal.data.model.AddressItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultAddressRepository(
    private val addressService: AddressService,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) : AddressRepository {

    private val statesChannel: Channel<List<AddressItem>> = bufferedChannel()
    override val statesFlow: Flow<List<AddressItem>> = statesChannel.receiveAsFlow()

    private val countriesChannel: Channel<List<AddressItem>> = bufferedChannel()
    override val countriesFlow: Flow<List<AddressItem>> = countriesChannel.receiveAsFlow()

    private val cache: HashMap<String, List<AddressItem>> = hashMapOf()

    override fun getStateList(
        shopperLocale: Locale,
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
                    shopperLocale,
                    countryCode,
                    coroutineScope,
                )
            }
        } else {
            statesChannel.trySend(emptyList())
        }
    }

    private fun fetchStateList(
        shopperLocale: Locale,
        countryCode: String,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch(coroutineDispatcher) {
            val states = getStates(
                shopperLocale = shopperLocale,
                countryCode = countryCode,
            ).fold(
                onSuccess = { states ->
                    if (states.isNotEmpty()) {
                        cache[countryCode] = states
                    }
                    states
                },
                onFailure = { emptyList() },
            )
            statesChannel.trySend(states)
        }
    }

    override fun getCountryList(shopperLocale: Locale, coroutineScope: CoroutineScope) {
        cache[COUNTRIES_CACHE_KEY]?.let {
            countriesChannel.trySend(it)
        } ?: run {
            fetchCountryList(
                shopperLocale,
                coroutineScope,
            )
        }
    }

    private fun fetchCountryList(shopperLocale: Locale, coroutineScope: CoroutineScope) {
        coroutineScope.launch(coroutineDispatcher) {
            val countries = getCountries(
                shopperLocale = shopperLocale,
            ).fold(
                onSuccess = { countries ->
                    if (countries.isNotEmpty()) {
                        cache[COUNTRIES_CACHE_KEY] = countries
                    }
                    countries
                },
                onFailure = {
                    emptyList()
                },
            )
            countriesChannel.trySend(countries)
        }
    }

    private suspend fun getCountries(
        shopperLocale: Locale
    ): Result<List<AddressItem>> = runSuspendCatching {
        adyenLog(AdyenLogLevel.DEBUG) { "getting country list" }
        return@runSuspendCatching addressService.getCountries(shopperLocale.toLanguageTag())
    }

    private suspend fun getStates(
        shopperLocale: Locale,
        countryCode: String
    ): Result<List<AddressItem>> = runSuspendCatching {
        adyenLog(AdyenLogLevel.DEBUG) { "getting state list for $countryCode" }
        return@runSuspendCatching addressService.getStates(shopperLocale.toLanguageTag(), countryCode)
    }

    companion object {

        private val COUNTRIES_WITH_STATES = listOf(
            AddressSpecification.BR,
            AddressSpecification.CA,
            AddressSpecification.US,
        )
        private const val COUNTRIES_CACHE_KEY = "countries"
    }
}

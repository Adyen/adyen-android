/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/7/2024.
 */

package com.adyen.checkout.ui.core.internal.data.api

import com.adyen.checkout.ui.core.internal.data.model.AddressItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.Locale

/**
 * Test implementation of [AddressRepository].
 */
class TestAddressRepository : AddressRepository {

    // will emit an empty list
    var shouldReturnError = false

    private val _statesFlow: MutableSharedFlow<List<AddressItem>> =
        MutableSharedFlow(extraBufferCapacity = 1)
    override val statesFlow: Flow<List<AddressItem>> = _statesFlow

    private val _countriesFlow: MutableSharedFlow<List<AddressItem>> =
        MutableSharedFlow(extraBufferCapacity = 1)
    override val countriesFlow: Flow<List<AddressItem>> = _countriesFlow

    override fun getStateList(
        shopperLocale: Locale,
        countryCode: String?,
        coroutineScope: CoroutineScope
    ) {
        val states = if (shouldReturnError) emptyList() else STATES
        _statesFlow.tryEmit(states)
    }

    override fun getCountryList(shopperLocale: Locale, coroutineScope: CoroutineScope) {
        val countries = if (shouldReturnError) emptyList() else COUNTRIES
        _countriesFlow.tryEmit(countries)
    }

    companion object {
        val COUNTRIES = listOf(
            AddressItem(
                id = "AU",
                name = "Australia",
            ),
            AddressItem(
                id = "BH",
                name = "Bahrain",
            ),
            AddressItem(
                id = "BE",
                name = "Belgium",
            ),
            AddressItem(
                id = "BR",
                name = "Brazil",
            ),
            AddressItem(
                id = "NL",
                name = "Netherlands",
            ),
            AddressItem(
                id = "US",
                name = "The United States of America",
            ),
        )

        val STATES = listOf(
            AddressItem(
                id = "CA",
                name = "California",
            ),
            AddressItem(
                id = "FL",
                name = "Florida",
            ),
            AddressItem(
                id = "MS",
                name = "Mississippi",
            ),
            AddressItem(
                id = "BR",
                name = "Brazil",
            ),
            AddressItem(
                id = "NY",
                name = "New York",
            ),
        )
    }
}

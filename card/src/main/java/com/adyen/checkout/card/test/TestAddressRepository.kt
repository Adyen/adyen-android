/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/8/2022.
 */

package com.adyen.checkout.card.test

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.card.repository.AddressRepository
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Test implementation of [AddressRepository]. This class should never be used except in test code.
 */
// TODO move to test fixtures once it becomes supported on Android
@RestrictTo(RestrictTo.Scope.TESTS)
class TestAddressRepository : AddressRepository {

    // will emit an empty list
    var shouldReturnError = false

    private val _statesFlow: MutableSharedFlow<List<AddressItem>> = MutableSingleEventSharedFlow()
    override val statesFlow: Flow<List<AddressItem>> = _statesFlow

    private val _countriesFlow: MutableSharedFlow<List<AddressItem>> = MutableSingleEventSharedFlow()
    override val countriesFlow: Flow<List<AddressItem>> = _countriesFlow

    override fun getStateList(configuration: Configuration, countryCode: String?, coroutineScope: CoroutineScope) {
        val states = if (shouldReturnError) emptyList() else STATES
        _statesFlow.tryEmit(states)
    }

    override fun getCountryList(configuration: Configuration, coroutineScope: CoroutineScope) {
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

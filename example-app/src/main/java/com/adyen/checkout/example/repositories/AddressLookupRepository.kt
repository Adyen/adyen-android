/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/1/2024.
 */

package com.adyen.checkout.example.repositories

import android.content.res.AssetManager
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.example.data.mock.MockDataService
import com.adyen.checkout.example.data.mock.model.MockAddressLookupResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AddressLookupRepository @Inject constructor(
    assetManager: AssetManager
) {
    private val mockAddressLookupOptions: List<LookupAddress>

    init {
        val mockDataService = MockDataService(assetManager)
        val json = mockDataService.readJsonFile("lookup_options.json")
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter: JsonAdapter<MockAddressLookupResponse> = moshi.adapter(MockAddressLookupResponse::class.java)
        mockAddressLookupOptions = adapter.fromJson(json)?.options.orEmpty()
    }

    private val addressLookupQueryFlow = MutableStateFlow<String?>(null)

    @OptIn(FlowPreview::class)
    val addressLookupOptionsFlow: Flow<List<LookupAddress>> = addressLookupQueryFlow
        .filterNotNull()
        .debounce(ADDRESS_LOOKUP_QUERY_DEBOUNCE_DURATION)
        .map { query ->
            queryAddressLookupOptions(query)
        }

    fun onQuery(query: String) {
        addressLookupQueryFlow.tryEmit(query)
    }

    suspend fun onAddressLookupCompleted(lookupAddress: LookupAddress): AddressLookupCompletionResult {
        delay(ADDRESS_LOOKUP_COMPLETION_DELAY)
        return if (lookupAddress.id == ADDRESS_LOOKUP_ERROR_ITEM_ID) {
            AddressLookupCompletionResult.Error()
        } else {
            AddressLookupCompletionResult.Address(lookupAddress)
        }
    }

    private fun queryAddressLookupOptions(query: String): List<LookupAddress> {
        return if (query == "empty") {
            emptyList()
        } else {
            mockAddressLookupOptions
        }
    }

    companion object {
        private const val ADDRESS_LOOKUP_QUERY_DEBOUNCE_DURATION = 300L
        private const val ADDRESS_LOOKUP_COMPLETION_DELAY = 400L
        private const val ADDRESS_LOOKUP_ERROR_ITEM_ID = "error"
    }
}

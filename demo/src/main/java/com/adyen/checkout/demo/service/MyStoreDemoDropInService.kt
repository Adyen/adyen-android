/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/2/2024.
 */

package com.adyen.checkout.demo.service

import com.adyen.checkout.demo.data.repositories.AddressLookupRepository
import com.adyen.checkout.dropin.AddressLookupDropInServiceResult
import com.adyen.checkout.dropin.SessionDropInService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(FlowPreview::class)
@AndroidEntryPoint
class MyStoreDemoDropInService : SessionDropInService() {

    @Inject
    lateinit var addressLookupRepository: AddressLookupRepository

    private val addressLookupQueryFlow = MutableStateFlow<String?>(null)

    init {
        addressLookupQueryFlow
            .debounce(ADDRESS_LOOKUP_QUERY_DEBOUNCE_DURATION)
            .filterNotNull()
            .onEach { query ->
                val options = if (query == "empty") {
                    emptyList()
                } else {
                    addressLookupRepository.getAddressLookupOptions()
                }
                sendAddressLookupResult(AddressLookupDropInServiceResult.LookupResult(options))
            }.launchIn(this)
    }

    override fun onAddressLookupQueryChanged(query: String) {
        addressLookupQueryFlow.tryEmit(query)
    }

    companion object {
        private const val ADDRESS_LOOKUP_QUERY_DEBOUNCE_DURATION = 300L
    }
}

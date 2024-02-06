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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressLookupRepository @Inject constructor(
    private val assetManager: AssetManager
) {

    fun getAddressLookupOptions(): List<LookupAddress> {
        val mockDataService = MockDataService(assetManager)
        val json = mockDataService.readJsonFile("lookup_options.json")
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter: JsonAdapter<MockAddressLookupResponse> = moshi.adapter(MockAddressLookupResponse::class.java)
        return adapter.fromJson(json)?.options.orEmpty()
    }
}

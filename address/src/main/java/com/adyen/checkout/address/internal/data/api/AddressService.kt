/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.address.internal.data.api

import com.adyen.checkout.address.internal.data.model.AddressItem
import com.adyen.checkout.core.common.internal.api.DispatcherProvider
import com.adyen.checkout.core.common.internal.api.HttpClient
import com.adyen.checkout.core.common.internal.api.getList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Locale

internal class AddressService(
    private val httpClient: HttpClient,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) {

    suspend fun getCountries(
        shopperLocale: Locale,
    ): List<AddressItem> = withContext(coroutineDispatcher) {
        httpClient.getList(
            path = "datasets/countries/${shopperLocale.toLanguageTag()}.json",
            responseSerializer = AddressItem.SERIALIZER,
        )
    }

    suspend fun getStates(
        shopperLocale: Locale,
        countryCode: String,
    ): List<AddressItem> = withContext(coroutineDispatcher) {
        httpClient.getList(
            path = "datasets/states/$countryCode/${shopperLocale.toLanguageTag()}.json",
            responseSerializer = AddressItem.SERIALIZER,
        )
    }
}

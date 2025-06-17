/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 13/1/2023.
 */

package com.adyen.checkout.ui.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.DispatcherProvider
import com.adyen.checkout.core.old.internal.data.api.HttpClient
import com.adyen.checkout.core.old.internal.data.api.getList
import com.adyen.checkout.ui.core.internal.data.model.AddressItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AddressService(
    private val httpClient: HttpClient,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) {
    suspend fun getCountries(
        shopperLocale: String
    ): List<AddressItem> = withContext(coroutineDispatcher) {
        httpClient.getList(
            path = "datasets/countries/$shopperLocale.json",
            responseSerializer = AddressItem.SERIALIZER,
        )
    }

    suspend fun getStates(
        shopperLocale: String,
        countryCode: String
    ): List<AddressItem> = withContext(coroutineDispatcher) {
        httpClient.getList(
            path = "datasets/states/$countryCode/$shopperLocale.json",
            responseSerializer = AddressItem.SERIALIZER,
        )
    }
}

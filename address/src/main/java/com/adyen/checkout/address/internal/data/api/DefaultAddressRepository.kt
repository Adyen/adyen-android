/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.address.internal.data.api

import com.adyen.checkout.address.internal.data.model.AddressItem
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.core.error.internal.HttpError
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

internal class DefaultAddressRepository(
    private val addressService: AddressService,
    private val shopperLocale: Locale,
) : AddressRepository {

    @Volatile
    private var cachedCountries: List<AddressItem>? = null
    private val cachedStates = ConcurrentHashMap<String, List<AddressItem>>()

    override suspend fun getCountries(): Result<List<AddressItem>> {
        cachedCountries?.let { return Result.success(it) }

        return runSuspendCatching {
            adyenLog(AdyenLogLevel.DEBUG) { "getting country list" }
            addressService.getCountries(shopperLocale)
        }.onSuccess { cachedCountries = it }
    }

    override suspend fun getStates(countryCode: String): Result<List<AddressItem>> {
        if (countryCode.isBlank()) return Result.success(emptyList())

        val cached = cachedStates[countryCode]
        return if (cached != null) Result.success(cached) else fetchStates(countryCode)
    }

    private suspend fun fetchStates(countryCode: String) = runSuspendCatching {
        adyenLog(AdyenLogLevel.DEBUG) { "getting state list for $countryCode" }
        try {
            addressService.getStates(shopperLocale, countryCode)
        } catch (e: HttpError) {
            // The datasets API has no states file for countries without states, so a 404 means "no states".
            if (e.code == HTTP_NOT_FOUND) emptyList() else throw e
        }
    }.onSuccess { cachedStates[countryCode] = it }

    companion object {
        private const val HTTP_NOT_FOUND = 404
    }
}

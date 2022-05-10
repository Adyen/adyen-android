/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/3/2022.
 */

package com.adyen.checkout.card.repository

import com.adyen.checkout.card.api.AddressService
import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.runSuspendCatching
import java.util.Locale

class AddressRepository {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    suspend fun getCountries(
        environment: Environment,
        shopperLocale: Locale
    ): Result<List<AddressItem>> = runSuspendCatching {
        Logger.d(TAG, "getting country list")
        return@runSuspendCatching AddressService(environment.baseUrl).getCountries(shopperLocale.toLanguageTag())
    }

    suspend fun getStates(
        environment: Environment,
        shopperLocale: Locale,
        countryCode: String
    ): Result<List<AddressItem>> = runSuspendCatching {
        Logger.d(TAG, "getting state list for $countryCode")
        return@runSuspendCatching AddressService(environment.baseUrl).getStates(shopperLocale.toLanguageTag(), countryCode)
    }

}

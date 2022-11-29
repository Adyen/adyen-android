/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/8/2022.
 */

package com.adyen.checkout.card.repository

import com.adyen.checkout.card.api.model.AddressItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.util.Locale

interface AddressRepository {

    val statesFlow: Flow<List<AddressItem>>

    val countriesFlow: Flow<List<AddressItem>>

    fun getStateList(
        shopperLocale: Locale,
        countryCode: String?,
        coroutineScope: CoroutineScope
    )

    fun getCountryList(
        shopperLocale: Locale,
        coroutineScope: CoroutineScope
    )
}

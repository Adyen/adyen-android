/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 13/1/2023.
 */

package com.adyen.checkout.ui.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.internal.data.model.AddressItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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

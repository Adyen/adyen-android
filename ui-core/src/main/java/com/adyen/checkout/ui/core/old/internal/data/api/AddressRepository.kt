/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.old.internal.data.model.AddressItem
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

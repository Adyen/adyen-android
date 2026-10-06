/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.address.internal.data.api

import com.adyen.checkout.address.internal.data.model.AddressItem

internal interface AddressRepository {

    suspend fun getCountries(): Result<List<AddressItem>>

    /**
     * Returns the states of [countryCode]. An empty list means the country has no states; a failure means they could
     * not be loaded.
     */
    suspend fun getStates(countryCode: String): Result<List<AddressItem>>
}

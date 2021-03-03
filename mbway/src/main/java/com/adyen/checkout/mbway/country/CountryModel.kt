/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/12/2020.
 */

package com.adyen.checkout.mbway.country

data class CountryModel(
    val isoCode: String,
    val countryName: String,
    val callingCode: String,
    val emoji: String
) {
    fun toShortString(): String {
        return "$emoji $callingCode"
    }
}

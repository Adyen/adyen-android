/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */
package com.adyen.checkout.core.old.ui.model

import androidx.annotation.RestrictTo

/**
 * Expiry date.
 *
 * @param expiryMonth 1 based month value. Valid values are [1-12], 1 being January and 12 being December.
 * @param expiryYear 4 digit year (i.e. 2024)
 */
data class ExpiryDate(
    val expiryMonth: Int,
    val expiryYear: Int,
) {

    /**
     * Convert this instance to a date string with the "MM/yy" format.
     */
    fun toMMyyString(): String {
        val monthDigits = expiryMonth.toString().padStart(2, '0')
        val yearDigits = expiryYear.toString().takeLast(2).padStart(2, '0')
        return "$monthDigits/$yearDigits"
    }

    companion object {

        /**
         * Create an [ExpiryDate] from a string, expecting the `MM/yy` date format
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        fun from(date: String): ExpiryDate {
            val split = date.split("/")
            val month = split.getOrNull(0)?.toIntOrNull() ?: -1
            val year = split.getOrNull(1)?.toIntOrNull() ?: -1
            return ExpiryDate(month, year)
        }
    }
}

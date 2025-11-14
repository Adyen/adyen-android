/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/9/2025.
 */

package com.adyen.checkout.core.common.localization.internal

import android.content.Context
import com.adyen.checkout.core.R
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import java.util.Locale

internal class DefaultLocalizationSource {

    @Suppress("CyclomaticComplexMethod", "UnusedParameter")
    fun getString(context: Context, locale: Locale, key: CheckoutLocalizationKey): String {
        return when (key) {
            CheckoutLocalizationKey.AWAIT_LOADING -> context.getString(
                R.string.checkout_await_loading
            )
            // TODO - Card localization
            CheckoutLocalizationKey.CARD_NUMBER -> "Card number"
            CheckoutLocalizationKey.CARD_EXPIRY_DATE -> "Expiry Date"
            CheckoutLocalizationKey.CARD_EXPIRY_DATE_INVALID -> "Invalid Expiry Date"
            CheckoutLocalizationKey.CARD_EXPIRY_DATE_INVALID_TOO_OLD -> "Card too old"
            CheckoutLocalizationKey.CARD_EXPIRY_DATE_INVALID_TOO_FAR_IN_THE_FUTURE ->
                "Expiry date too far in the future"
            CheckoutLocalizationKey.CARD_NUMBER_INVALID -> "Invalid card number"
            CheckoutLocalizationKey.CARD_NUMBER_INVALID_UNSUPPORTED_BRAND -> "The entered card brand isn't supported"
            CheckoutLocalizationKey.MBWAY_PHONE_NUMBER -> context.getString(
                R.string.checkout_mbway_phone_number
            )
            CheckoutLocalizationKey.MBWAY_INVALID_PHONE_NUMBER -> context.getString(
                R.string.checkout_mbway_invalid_phone_number
            )
            CheckoutLocalizationKey.MBWAY_COUNTRY_CODE -> context.getString(
                R.string.checkout_mbway_country_code
            )
        }
    }
}

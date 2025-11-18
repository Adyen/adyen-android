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
        val resId = when (key) {
            // Await
            CheckoutLocalizationKey.AWAIT_LOADING -> R.string.checkout_await_loading
            // Card
            CheckoutLocalizationKey.CARD_NUMBER -> R.string.checkout_card_number
            CheckoutLocalizationKey.CARD_NUMBER_INVALID -> R.string.checkout_card_number_invalid
            CheckoutLocalizationKey.CARD_NUMBER_INVALID_UNSUPPORTED_BRAND ->
                R.string.checkout_card_number_invalid_unsupported_brand

            CheckoutLocalizationKey.CARD_EXPIRY_DATE -> R.string.checkout_card_expiry_date
            CheckoutLocalizationKey.CARD_EXPIRY_DATE_INVALID -> R.string.checkout_card_expiry_date_invalid
            CheckoutLocalizationKey.CARD_EXPIRY_DATE_INVALID_TOO_OLD ->
                R.string.checkout_card_expiry_date_invalid_too_old

            CheckoutLocalizationKey.CARD_EXPIRY_DATE_INVALID_TOO_FAR_IN_THE_FUTURE ->
                R.string.checkout_card_expiry_date_invalid_too_far_in_the_future

            CheckoutLocalizationKey.CARD_EXPIRY_DATE_HINT -> R.string.checkout_card_expiry_date_hint
            CheckoutLocalizationKey.CARD_SECURITY_CODE -> R.string.checkout_card_security_code
            CheckoutLocalizationKey.CARD_SECURITY_CODE_INVALID -> R.string.checkout_card_security_code_invalid
            CheckoutLocalizationKey.CARD_SECURITY_CODE_HINT_3_DIGITS ->
                R.string.checkout_card_security_code_hint_3_digits

            CheckoutLocalizationKey.CARD_SECURITY_CODE_HINT_4_DIGITS ->
                R.string.checkout_card_security_code_hint_4_digits

            CheckoutLocalizationKey.CARD_DUAL_BRAND_SELECTOR_TITLE ->
                R.string.checkout_card_dual_brand_selector_title

            CheckoutLocalizationKey.CARD_DUAL_BRAND_SELECTOR_DESCRIPTION ->
                R.string.checkout_card_dual_brand_selector_description

            // MBWay
            CheckoutLocalizationKey.MBWAY_PHONE_NUMBER -> R.string.checkout_mbway_phone_number
            CheckoutLocalizationKey.MBWAY_INVALID_PHONE_NUMBER -> R.string.checkout_mbway_invalid_phone_number
            CheckoutLocalizationKey.MBWAY_COUNTRY_CODE -> R.string.checkout_mbway_country_code
        }

        return context.getString(resId)
    }
}

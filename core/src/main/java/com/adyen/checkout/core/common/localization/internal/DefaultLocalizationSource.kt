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

            CheckoutLocalizationKey.CARD_HOLDER_NAME -> R.string.checkout_card_holder_name
            CheckoutLocalizationKey.CARD_HOLDER_NAME_INVALID -> R.string.checkout_card_holder_name_invalid
            CheckoutLocalizationKey.CARD_STORE_PAYMENT_METHOD -> R.string.checkout_card_store_payment_method
            CheckoutLocalizationKey.CARD_DUAL_BRAND_SELECTOR_TITLE ->
                R.string.checkout_card_dual_brand_selector_title

            CheckoutLocalizationKey.CARD_DUAL_BRAND_SELECTOR_DESCRIPTION ->
                R.string.checkout_card_dual_brand_selector_description

            // Drop-in
            CheckoutLocalizationKey.DROP_IN_MANAGE_FAVORITES_TITLE -> R.string.checkout_drop_in_manage_favorites_title
            CheckoutLocalizationKey.DROP_IN_MANAGE_FAVORITES_CARDS_SECTION_TITLE ->
                R.string.checkout_drop_in_manage_favorites_cards_section_title

            CheckoutLocalizationKey.DROP_IN_MANAGE_FAVORITES_OTHERS_SECTION_TITLE ->
                R.string.checkout_drop_in_manage_favorites_others_section_title

            CheckoutLocalizationKey.DROP_IN_MANAGE_FAVORITES_REMOVE ->
                R.string.checkout_drop_in_manage_favorites_remove

            CheckoutLocalizationKey.DROP_IN_MANAGE_FAVORITES_REMOVE_CONFIRMATION ->
                R.string.checkout_drop_in_manage_favorites_remove_confirmation

            CheckoutLocalizationKey.DROP_IN_OTHER_PAYMENT_METHODS -> R.string.checkout_drop_in_other_payment_methods
            CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_DESCRIPTION ->
                R.string.checkout_drop_in_payment_method_list_description

            CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_FAVORITES_SECTION_ACTION ->
                R.string.checkout_drop_in_payment_method_list_favorites_section_action

            CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_FAVORITES_SECTION_TITLE ->
                R.string.checkout_drop_in_payment_method_list_favorites_section_title

            CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_PAYMENT_OPTIONS_SECTION_TITLE ->
                R.string.checkout_drop_in_payment_method_list_payment_options_title

            CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_PAYMENT_OPTIONS_SECTION_TITLE_WITH_FAVORITES ->
                R.string.checkout_drop_in_payment_method_list_payment_options_title_with_favorites

            // General
            CheckoutLocalizationKey.GENERAL_BACK -> R.string.checkout_general_back
            CheckoutLocalizationKey.GENERAL_CANCEL -> R.string.checkout_general_cancel
            CheckoutLocalizationKey.GENERAL_CLOSE -> R.string.checkout_general_close
            CheckoutLocalizationKey.GENERAL_SEARCH_HINT -> R.string.checkout_general_search_hint

            // MBWay
            CheckoutLocalizationKey.MBWAY_PHONE_NUMBER -> R.string.checkout_mbway_phone_number
            CheckoutLocalizationKey.MBWAY_INVALID_PHONE_NUMBER -> R.string.checkout_mbway_invalid_phone_number
            CheckoutLocalizationKey.MBWAY_COUNTRY_CODE -> R.string.checkout_mbway_country_code
        }

        return context.getString(resId)
    }
}

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
import com.adyen.checkout.core.common.internal.helper.createLocalizedContext
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import java.util.Locale

internal class DefaultLocalizationSource {

    fun getString(context: Context, locale: Locale, key: CheckoutLocalizationKey): String {
        val localizedContext = context.createLocalizedContext(locale)

        return when (key) {
            CheckoutLocalizationKey.MBWAY_PHONE_NUMBER -> localizedContext.getString(
                R.string.checkout_mbway_phone_number
            )
            CheckoutLocalizationKey.MBWAY_COUNTRY_CODE -> localizedContext.getString(
                R.string.checkout_mbway_country_code
            )
        }
    }
}

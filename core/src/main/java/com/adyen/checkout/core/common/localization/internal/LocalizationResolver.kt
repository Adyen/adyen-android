/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/9/2025.
 */

package com.adyen.checkout.core.common.localization.internal

import android.content.Context
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import java.util.Locale

internal class LocalizationResolver(
    private val localizationProvider: CheckoutLocalizationProvider?
) {
    private val defaultLocalizationSource = DefaultLocalizationSource()

    fun getLocalizedStringFor(context: Context, locale: Locale, key: CheckoutLocalizationKey): String {
        return localizationProvider?.getString(context, locale, key)
            ?: defaultLocalizationSource.getString(context, locale, key)
    }
}

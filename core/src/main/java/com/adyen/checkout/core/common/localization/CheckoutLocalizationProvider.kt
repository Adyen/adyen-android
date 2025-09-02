/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/9/2025.
 */

package com.adyen.checkout.core.common.localization

import android.content.Context
import java.util.Locale

interface CheckoutLocalizationProvider {
    fun getString(context: Context, locale: Locale, key: CheckoutLocalizationKey): String?
}

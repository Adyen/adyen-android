/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/4/2025.
 */

package com.adyen.checkout.ui.core

import android.content.Context
import java.util.Locale

interface LocalizationProvider {

    fun get(key: LocalizationKey, locale: Locale, context: Context, vararg arguments: String): String
}

class DefaultLocalizationProvider() : LocalizationProvider {

    private val map = hashMapOf(
        LocalizationKey.PAY_BUTTON to R.string.pay_button,
    )

    override fun get(key: LocalizationKey, locale: Locale, context: Context, vararg arguments: String): String {
        return context.getString(map[key]!!, arguments)
    }
}

enum class LocalizationKey {
    PAY_BUTTON,
}

/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 2/11/2021.
 */

package com.adyen.checkout.dropin.internal.util

import android.content.Context
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.LocaleUtil
import com.adyen.checkout.core.old.internal.util.adyenLog
import java.util.Locale

internal object DropInPrefs {

    private const val DROP_IN_PREFS = "drop-in-shared-prefs"
    private const val LOCALE_PREF = "drop-in-locale"

    fun setShopperLocale(context: Context, shopperLocale: Locale?) {
        adyenLog(AdyenLogLevel.VERBOSE) { "setShopperLocale: $shopperLocale" }
        val localeTag = shopperLocale?.let { LocaleUtil.toLanguageTag(shopperLocale) }
        adyenLog(AdyenLogLevel.DEBUG) { "Storing shopper locale tag: $localeTag" }
        return context
            .getSharedPreferences(DROP_IN_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(LOCALE_PREF, localeTag)
            .apply()
    }

    fun getShopperLocale(context: Context): Locale? {
        adyenLog(AdyenLogLevel.VERBOSE) { "getShopperLocale" }
        val localeTag = context
            .getSharedPreferences(DROP_IN_PREFS, Context.MODE_PRIVATE)
            .getString(LOCALE_PREF, null)
        adyenLog(AdyenLogLevel.DEBUG) { "Fetched shopper locale tag: $localeTag" }
        if (localeTag == null) return null
        val locale = LocaleUtil.fromLanguageTag(localeTag)
        adyenLog(AdyenLogLevel.DEBUG) { "Parsed locale: $locale" }
        return locale
    }
}

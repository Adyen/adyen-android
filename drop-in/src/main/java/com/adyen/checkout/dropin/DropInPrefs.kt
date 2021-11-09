/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 2/11/2021.
 */

package com.adyen.checkout.dropin

import android.content.Context
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.LocaleUtil
import java.util.Locale

internal object DropInPrefs {
    private val TAG = LogUtil.getTag()

    private const val DROP_IN_PREFS = "drop-in-shared-prefs"
    private const val LOCALE_PREF = "drop-in-locale"

    fun setShopperLocale(context: Context, shopperLocale: Locale) {
        Logger.v(TAG, "setShopperLocale: $shopperLocale")
        val localeTag = LocaleUtil.toLanguageTag(shopperLocale)
        Logger.d(TAG, "Storing shopper locale tag: $localeTag")
        return context
            .getSharedPreferences(DROP_IN_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(LOCALE_PREF, localeTag)
            .apply()
    }

    fun getShopperLocale(context: Context): Locale {
        Logger.v(TAG, "getShopperLocale")
        val localeTag = context
            .getSharedPreferences(DROP_IN_PREFS, Context.MODE_PRIVATE)
            .getString(LOCALE_PREF, null)
            .orEmpty()
        Logger.d(TAG, "Fetching shopper locale tag: $localeTag")
        val locale = LocaleUtil.fromLanguageTag(localeTag)
        Logger.d(TAG, "Parsed locale: $locale")
        return locale
    }
}

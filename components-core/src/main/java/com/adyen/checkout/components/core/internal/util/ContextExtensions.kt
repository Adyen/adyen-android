/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/4/2021.
 */

package com.adyen.checkout.components.core.internal.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.widget.Toast
import androidx.annotation.RestrictTo
import androidx.core.content.getSystemService
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.copyTextToClipboard(label: String, text: String) {
    val clipboardManager = getSystemService<ClipboardManager>() ?: return
    val clipData = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clipData)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.createLocalizedContext(locale: Locale): Context {
    val configuration = resources.configuration
    val newConfig = Configuration(configuration)
    newConfig.setLocale(locale)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        newConfig.setLocales(localeList)
    }

    return createConfigurationContext(newConfig) ?: this
}

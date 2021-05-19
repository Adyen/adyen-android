/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/4/2021.
 */

package com.adyen.checkout.components.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.getSystemService

fun Context.copyTextToClipboard(label: String, text: String, toastText: String? = null) {
    val clipboardManager = getSystemService<ClipboardManager>() ?: return
    val clipData = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clipData)
    if (toastText == null) return
    toast(toastText)
}

fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

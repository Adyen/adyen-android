/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PdfOpener {

    @Throws(IllegalStateException::class)
    fun open(context: Context, url: String) {
        val uri = Uri.parse(url)
        if (open(context, uri)) return
        if (openInBrowser(context, uri)) return

        adyenLog(AdyenLogLevel.ERROR) { "Couldn't open pdf with url: $uri" }

        error("Couldn't open pdf with url: $uri")
    }

    private fun open(context: Context, uri: Uri): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (openInExternalApp(context, uri)) {
                true
            } else {
                openInCustomTab(context, uri)
            }
        } else {
            openInCustomTab(context, uri)
        }
    }

    private fun openInExternalApp(context: Context, uri: Uri): Boolean {
        val nativeAppIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, "application/pdf")
        }

        return try {
            context.startActivity(nativeAppIntent)
            adyenLog(AdyenLogLevel.DEBUG) { "Successfully opened pdf in external app" }
            true
        } catch (e: ActivityNotFoundException) {
            adyenLog(AdyenLogLevel.DEBUG, e) { "Couldn't open pdf in external app" }
            false
        }
    }

    private fun openInCustomTab(context: Context, uri: Uri): Boolean {
        // open in custom tabs if there's no native app for the target uri
        val isLaunched = CustomTabsLauncher.launchCustomTab(context, uri)
        if (isLaunched) {
            adyenLog(AdyenLogLevel.DEBUG) { "Successfully opened pdf in custom tab" }
        } else {
            adyenLog(AdyenLogLevel.DEBUG) { "Couldn't open pdf in custom tab" }
        }
        return isLaunched
    }

    private fun openInBrowser(context: Context, uri: Uri): Boolean {
        return try {
            val browserActivityIntent = Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(uri)

            context.startActivity(browserActivityIntent)
            adyenLog(AdyenLogLevel.DEBUG) { "Successfully opened pdf in browser" }
            true
        } catch (e: ActivityNotFoundException) {
            adyenLog(AdyenLogLevel.DEBUG, e) { "Couldn't open pdf in browser" }
            false
        }
    }
}

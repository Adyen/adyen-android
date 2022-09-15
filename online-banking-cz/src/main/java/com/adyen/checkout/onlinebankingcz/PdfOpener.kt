/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 13/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.adyen.checkout.components.ui.util.ThemeUtil
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

internal class PdfOpener {

    @Throws(IllegalStateException::class)
    fun open(context: Context, url: String) {
        val uri = Uri.parse(url)
        if (open(context, uri)) return
        if (openInBrowser(context, uri)) return

        Logger.e(TAG, "Couldn't open pdf with url: $uri")

        throw IllegalStateException("Couldn't open pdf with url: $uri")
    }

    private fun open(context: Context, uri: Uri): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) openInExternalApp(context, uri)
        // because custom tabs pdf viewer is working before api 30 on chrome
        else openInCustomTab(context, uri)
    }

    private fun openInExternalApp(context: Context, uri: Uri): Boolean {
        val nativeAppIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, "application/pdf")
        }

        return try {
            context.startActivity(nativeAppIntent)
            Logger.d(TAG, "Successfully opened pdf in external app")
            true
        } catch (ex: ActivityNotFoundException) {
            Logger.d(TAG, "Couldn't open pdf in external app", ex)
            false
        }
    }

    private fun openInCustomTab(context: Context, uri: Uri): Boolean {
        // open in custom tabs if there's no native app for the target uri
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ThemeUtil.getPrimaryThemeColor(context))
            .build()

        return try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setDefaultColorSchemeParams(defaultColors)
                .build()
                .launchUrl(context, uri)

            Logger.d(TAG, "Successfully opened pdf in custom tab")
            true
        } catch (e: ActivityNotFoundException) {
            Logger.d(TAG, "Couldn't open pdf in custom tab", e)
            false
        }
    }

    private fun openInBrowser(context: Context, uri: Uri): Boolean {
        return try {
            val browserActivityIntent = Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(uri)

            context.startActivity(browserActivityIntent)
            Logger.d(TAG, "Successfully opened pdf in browser")
            true
        } catch (e: ActivityNotFoundException) {
            Logger.d(TAG, "Couldn't open pdf in browser", e)
            false
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}

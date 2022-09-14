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
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.adyen.checkout.components.ui.util.ThemeUtil
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

internal object PdfOpener {

    fun open(context: Context, url: String) {
        val uri = Uri.parse(url)
        if (open(context, uri)) return
        if (launchBrowser(context, uri)) return
        Logger.e(TAG, "openPdf - Could not launch url")
        throw ComponentException("failed to open terms and conditions pdf.")
    }

    // TODO: needs to be checked again in the future,
    //  as we're not sure about the next chrome updates regarding custom tabs!!
    private fun open(context: Context, uri: Uri): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) launchNativeApi30(context, uri)
        // because custom tabs pdf viewer is working before api 30 on chrome
        else launchWithCustomTabs(context, uri)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun launchNativeApi30(context: Context, uri: Uri): Boolean {
        val nativeAppIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, "application/pdf")
        }
        return try {
            context.startActivity(nativeAppIntent)
            Logger.d(TAG, "launchNativeApi30 - open terms and conditions pdf successful with native app")
            true
        } catch (ex: ActivityNotFoundException) {
            Logger.d(TAG, "launchNativeApi30 - could not find native app to terms and conditions pdf with", ex)
            false
        }
    }

    private fun launchWithCustomTabs(context: Context, uri: Uri): Boolean {
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
            Logger.d(TAG, "launchWithCustomTabs - open terms and conditions pdf successful with custom tabs")
            true
        } catch (e: ActivityNotFoundException) {
            Logger.d(TAG, "launchWithCustomTabs - device doesn't support custom tabs or chrome is disabled", e)
            false
        }
    }

    /**
     * in case the device doesn't support custom tabs or doesn't support google services (Huawei device).
     */
    private fun launchBrowser(context: Context, uri: Uri): Boolean {
        return try {
            val browserActivityIntent = Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(uri)
            context.startActivity(browserActivityIntent)
            Logger.d(TAG, "launchBrowser - open terms and conditions pdf successful with browser")
            true
        } catch (e: ActivityNotFoundException) {
            Logger.d(TAG, "launchBrowser - could not open pdf on browser or there's no browser!", e)
            false
        }
    }

    private val TAG = LogUtil.getTag()
}

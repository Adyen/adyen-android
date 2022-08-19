/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 1/4/2019.
 */
package com.adyen.checkout.redirect.handler

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.adyen.checkout.components.ui.util.ThemeUtil
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import org.json.JSONException
import org.json.JSONObject

class DefaultRedirectHandler : RedirectHandler {

    override fun parseRedirectResult(data: Uri?): JSONObject {
        Logger.d(TAG, "parseRedirectResult - $data")

        data ?: throw CheckoutException("Received a null redirect Uri")

        val extractedParams = HashMap<String, String>().apply {
            data.getQueryParameter(PAYLOAD_PARAMETER)?.let { put(PAYLOAD_PARAMETER, it) }
            data.getQueryParameter(REDIRECT_RESULT_PARAMETER)?.let { put(REDIRECT_RESULT_PARAMETER, it) }
            data.getQueryParameter(PAYMENT_RESULT_PARAMETER)?.let { paymentResult ->
                data.getQueryParameter(MD_PARAMETER)?.let { md ->
                    put(PAYMENT_RESULT_PARAMETER, paymentResult)
                    put(MD_PARAMETER, md)
                }
            }
        }

        if (extractedParams.isEmpty()) {
            data.encodedQuery?.let { extractedParams.put(QUERY_STRING_RESULT, it) }
        }

        if (extractedParams.isEmpty()) {
            throw CheckoutException("Error parsing redirect result, could not any query parameters")
        }

        try {
            return JSONObject().apply {
                extractedParams.forEach { put(it.key, it.value) }
            }
        } catch (e: JSONException) {
            throw CheckoutException("Error creating redirect result.", e)
        }
    }

    @Suppress("ReturnCount")
    override fun launchUriRedirect(context: Context, url: String?) {
        if (url.isNullOrEmpty()) throw ComponentException("Redirect URL is empty.")
        val uri = Uri.parse(url)
        if (launchNative(context, uri)) return
        if (launchWithCustomTabs(context, uri)) return
        if (launchBrowser(context, uri)) return
        Logger.e(TAG, "Could not launch url")
        throw ComponentException("Redirect to app failed.")
    }

    private fun launchNative(context: Context, uri: Uri): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) launchNativeApi30(context, uri)
        else launchNativeBeforeApi30(context, uri)
    }

    private fun launchNativeBeforeApi30(context: Context, uri: Uri): Boolean {
        val pm = context.packageManager

        // Get all Apps that resolve a generic url
        val browserActivityIntent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.fromParts("http", "", null))
        val genericResolvedSet = pm.queryIntentActivities(browserActivityIntent, 0).map {
            it.resolvePackageName
        }.toSet()

        // Get all apps that resolve the specific Url
        val specializedActivityIntent = Intent(Intent.ACTION_VIEW, uri)
            .addCategory(Intent.CATEGORY_BROWSABLE)
        val resolvedSpecializedSet = pm.queryIntentActivities(specializedActivityIntent, 0).map {
            it.resolvePackageName
        }.toMutableSet()

        // Keep only the Urls that resolve the specific, but not the generic
        // urls.
        resolvedSpecializedSet.removeAll(genericResolvedSet)

        // If the list is empty, no native app handlers were found.
        if (resolvedSpecializedSet.isEmpty()) {
            Logger.d(TAG, "launchNativeBeforeApi30 - could not find native app to redirect with")
            return false
        }

        // We found native handlers. Launch the Intent.
        specializedActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(specializedActivityIntent)
        Logger.d(TAG, "launchNativeBeforeApi30 - redirect successful with native app")
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun launchNativeApi30(context: Context, uri: Uri?): Boolean {
        val nativeAppIntent = Intent(Intent.ACTION_VIEW, uri)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
            )
        return try {
            context.startActivity(nativeAppIntent)
            Logger.d(TAG, "launchNativeApi30 - redirect successful with native app")
            true
        } catch (ex: ActivityNotFoundException) {
            Logger.d(TAG, "launchNativeApi30 - could not find native app to redirect with", ex)
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
            Logger.d(TAG, "launchWithCustomTabs - redirect successful with custom tabs")
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
            Logger.d(TAG, "launchBrowser - redirect successful with browser")
            true
        } catch (e: ActivityNotFoundException) {
            Logger.d(TAG, "launchBrowser - could not do redirect on browser or there's no browser!", e)
            false
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val PAYLOAD_PARAMETER = "payload"
        private const val REDIRECT_RESULT_PARAMETER = "redirectResult"
        private const val PAYMENT_RESULT_PARAMETER = "PaRes"
        private const val MD_PARAMETER = "MD"
        private const val QUERY_STRING_RESULT = "returnUrlQueryString"
    }
}

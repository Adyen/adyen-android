/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 1/4/2019.
 */
package com.adyen.checkout.ui.core.internal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.util.ThemeUtil
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultRedirectHandler : RedirectHandler {

    private var onRedirectListener: WeakReference<(() -> Unit)>? = null

    override fun parseRedirectResult(data: Uri?): JSONObject {
        adyenLog(AdyenLogLevel.DEBUG) { "parseRedirectResult - $data" }

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

        if (
            launchNative(context, uri) ||
            launchWithCustomTabs(context, uri) ||
            launchBrowser(context, uri)
        ) {
            onRedirectListener?.get()?.invoke()
            return
        }

        adyenLog(AdyenLogLevel.ERROR) { "Could not launch url" }
        throw ComponentException("Launching redirect failed.")
    }

    private fun launchNative(context: Context, uri: Uri): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            launchNativeApi30(context, uri)
        } else {
            launchNativeBeforeApi30(context, uri)
        }
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
            adyenLog(AdyenLogLevel.DEBUG) { "launchNativeBeforeApi30 - could not find native app to redirect with" }
            return false
        }

        // We found native handlers. Launch the Intent.
        specializedActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        @Suppress("SwallowedException")
        return try {
            context.startActivity(specializedActivityIntent)
            adyenLog(AdyenLogLevel.DEBUG) { "launchNativeBeforeApi30 - redirect successful with native app" }
            true
        } catch (e: ActivityNotFoundException) {
            adyenLog(AdyenLogLevel.DEBUG) { "launchNativeBeforeApi30 - could not find native app to redirect with" }
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun launchNativeApi30(context: Context, uri: Uri?): Boolean {
        val nativeAppIntent = Intent(Intent.ACTION_VIEW, uri)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER,
            )
        @Suppress("SwallowedException")
        return try {
            context.startActivity(nativeAppIntent)
            adyenLog(AdyenLogLevel.DEBUG) { "launchNativeApi30 - redirect successful with native app" }
            true
        } catch (e: ActivityNotFoundException) {
            adyenLog(AdyenLogLevel.DEBUG) { "launchNativeApi30 - could not find native app to redirect with" }
            false
        }
    }

    private fun launchWithCustomTabs(context: Context, uri: Uri): Boolean {
        // open in custom tabs if there's no native app for the target uri
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ThemeUtil.getPrimaryThemeColor(context))
            .build()

        @Suppress("SwallowedException")
        return try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setDefaultColorSchemeParams(defaultColors)
                .build()
                .launchUrl(context, uri)
            adyenLog(AdyenLogLevel.DEBUG) { "launchWithCustomTabs - redirect successful with custom tabs" }
            true
        } catch (e: ActivityNotFoundException) {
            adyenLog(AdyenLogLevel.DEBUG) {
                "launchWithCustomTabs - device doesn't support custom tabs or chrome is disabled"
            }
            false
        }
    }

    /**
     * in case the device doesn't support custom tabs or doesn't support google services (Huawei device).
     */
    private fun launchBrowser(context: Context, uri: Uri): Boolean {
        @Suppress("SwallowedException")
        return try {
            val browserActivityIntent = Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(uri)
            context.startActivity(browserActivityIntent)
            adyenLog(AdyenLogLevel.DEBUG) { "launchBrowser - redirect successful with browser" }
            true
        } catch (e: ActivityNotFoundException) {
            adyenLog(AdyenLogLevel.DEBUG) { "launchBrowser - could not do redirect on browser or there's no browser" }
            false
        }
    }

    override fun setOnRedirectListener(listener: () -> Unit) {
        onRedirectListener = WeakReference(listener)
    }

    override fun removeOnRedirectListener() {
        onRedirectListener?.clear()
        onRedirectListener = null
    }

    companion object {
        private const val PAYLOAD_PARAMETER = "payload"
        private const val REDIRECT_RESULT_PARAMETER = "redirectResult"
        private const val PAYMENT_RESULT_PARAMETER = "PaRes"
        private const val MD_PARAMETER = "MD"
        private const val QUERY_STRING_RESULT = "returnUrlQueryString"
    }
}

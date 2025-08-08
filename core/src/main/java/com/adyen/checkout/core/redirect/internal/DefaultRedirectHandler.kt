/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */
package com.adyen.checkout.core.redirect.internal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.core.net.toUri
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.CustomTabsLauncher
import com.adyen.checkout.core.common.internal.helper.adyenLog
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

@Suppress("TooGenericExceptionThrown")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultRedirectHandler : RedirectHandler {

    private var onRedirectListener: WeakReference<(() -> Unit)>? = null

    override fun parseRedirectResult(data: Uri?): JSONObject {
        adyenLog(AdyenLogLevel.DEBUG) { "parseRedirectResult - $data" }

        // TODO - Error Propagation
//        data ?: throw CheckoutException("Received a null redirect Uri")
        data ?: throw RuntimeException("Received a null redirect Uri")

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
            // TODO - Error Propagation
//            throw CheckoutException("Error parsing redirect result, could not find any query parameters")
            throw RuntimeException("Error parsing redirect result, could not find any query parameters")
        }

        try {
            return JSONObject().apply {
                extractedParams.forEach { put(it.key, it.value) }
            }
        } catch (e: JSONException) {
            // TODO - Error Propagation
//            throw CheckoutException("Error creating redirect result.", e)
            throw RuntimeException("Error creating redirect result.", e)
        }
    }

    @Suppress("ReturnCount")
    override fun launchUriRedirect(context: Context, url: String) {
        // TODO - Error Propagation
        // if (url.isEmpty()) throw ComponentException("Redirect URL is empty.")
        if (url.isEmpty()) throw RuntimeException("Redirect URL is empty.")
        val uri = url.toUri()

        if (
            launchNative(context, uri) ||
            launchWithCustomTabs(context, uri) ||
            launchBrowser(context, uri)
        ) {
            onRedirectListener?.get()?.invoke()
            return
        }

        adyenLog(AdyenLogLevel.ERROR) { "Could not launch url" }

        // TODO - Error Propagation
//        throw ComponentException("Launching redirect failed.")
        throw RuntimeException("Launching redirect failed.")
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
        val isLaunched = CustomTabsLauncher.launchCustomTab(context, uri)
        if (isLaunched) {
            adyenLog(AdyenLogLevel.DEBUG) { "launchWithCustomTabs - redirect successful with custom tabs" }
        } else {
            adyenLog(AdyenLogLevel.DEBUG) {
                "launchWithCustomTabs - device doesn't support custom tabs or chrome is disabled"
            }
        }
        return isLaunched
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

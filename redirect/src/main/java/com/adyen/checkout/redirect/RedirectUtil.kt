/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 1/4/2019.
 */
package com.adyen.checkout.redirect

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.adyen.checkout.components.ui.util.ThemeUtil
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import org.json.JSONException
import org.json.JSONObject

object RedirectUtil {
    private val TAG = LogUtil.getTag()

    /**
     * The suggested scheme to be used in the intent filter to receive the redirect result.
     * This value should be the beginning of the `returnUrl` sent on the payments/ call.
     */
    const val REDIRECT_RESULT_SCHEME = BuildConfig.checkoutRedirectScheme + "://"

    private const val PAYLOAD_PARAMETER = "payload"
    private const val REDIRECT_RESULT_PARAMETER = "redirectResult"
    private const val PAYMENT_RESULT_PARAMETER = "PaRes"
    private const val MD_PARAMETER = "MD"
    private const val QUERY_STRING_RESULT = "returnUrlQueryString"

    private const val RESOLVER_ACTIVITY_PACKAGE_NAME = "android"

    @SuppressWarnings("TooGenericExceptionCaught")
    private fun determineResolveResult(context: Context, uri: Uri): ResolveResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"))
            val packageManager = context.packageManager
            val resolveInfo = packageManager.resolveActivity(intent, 0)
            val browserInfo = packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
            val resolvedPackageName = resolveInfo?.activityInfo?.packageName
            val browserPackageName = browserInfo?.activityInfo?.packageName
            when (resolvedPackageName) {
                RESOLVER_ACTIVITY_PACKAGE_NAME -> ResolveResult(ResolveResult.Type.RESOLVER_ACTIVITY, resolveInfo)
                browserPackageName -> ResolveResult(ResolveResult.Type.DEFAULT_BROWSER, resolveInfo)
                null -> ResolveResult(ResolveResult.Type.UNKNOWN, null)
                else -> ResolveResult(ResolveResult.Type.APPLICATION, resolveInfo)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "determineResolveResult exception", e)
            ResolveResult(ResolveResult.Type.UNKNOWN, null)
        }
    }

    /**
     * A redirect may return to the application using the ReturnUrl when properly setup in an Intent Filter. Is usually contains result information
     * as parameters on that returnUrl. This method parses those results and returns a [JSONObject] to be used in the details call.
     *
     * @param data The returned Uri
     * @return The parsed value to be passed on the payments/details call, on the details parameter.
     */
    @JvmStatic
    @Throws(CheckoutException::class)
    fun parseRedirectResult(data: Uri): JSONObject {
        Logger.d(TAG, "parseRedirectResult - $data")

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

    /**
     * Creates the intent that will start the redirect.
     * @param context Any context.
     * @param uri The Uri to redirect to.
     * @return And intent that targets either another app or a Web page.
     */
    @JvmStatic
    fun createRedirectIntent(context: Context, uri: Uri): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            createCustomTabIntent(context, uri)
        } else {
            if (determineResolveResult(context, uri).type == ResolveResult.Type.APPLICATION) {
                Intent(Intent.ACTION_VIEW, uri)
            } else {
                createCustomTabIntent(context, uri)
            }
        }
    }

    private fun createCustomTabIntent(context: Context, uri: Uri): Intent {
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ThemeUtil.getPrimaryThemeColor(context))
            .build()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setDefaultColorSchemeParams(defaultColors)
            .build()
        customTabsIntent.intent.data = uri
        return customTabsIntent.intent
    }
}

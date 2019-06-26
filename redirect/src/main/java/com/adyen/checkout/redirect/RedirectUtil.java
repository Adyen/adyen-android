/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 1/4/2019.
 */

package com.adyen.checkout.redirect;

import static com.adyen.checkout.redirect.ResolveResult.Type.APPLICATION;
import static com.adyen.checkout.redirect.ResolveResult.Type.DEFAULT_BROWSER;
import static com.adyen.checkout.redirect.ResolveResult.Type.RESOLVER_ACTIVITY;
import static com.adyen.checkout.redirect.ResolveResult.Type.UNKNOWN;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;

import com.adyen.checkout.base.ui.util.ThemeUtil;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.exeption.CheckoutException;
import com.adyen.checkout.core.exeption.NoConstructorException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public final class RedirectUtil {
    private static final String TAG = LogUtil.getTag();

    /**
     * The suggested scheme to be used in the intent filter to receive the redirect result.
     * This value should be the beginning of the `returnUr` sent on the payments/ call.
     */
    @NonNull
    public static final String REDIRECT_RESULT_SCHEME = BuildConfig.checkoutRedirectScheme + "://";

    private static final String PAYLOAD_PARAMETER = "payload";
    private static final String REDIRECT_RESULT_PARAMETER = "redirectResult";
    private static final String RETURN_URL_QUERY_PARAMETER = "returnUrlQueryString";
    private static final String RESOLVER_ACTIVITY_PACKAGE_NAME = "android";

    @NonNull
    static ResolveResult determineResolveResult(@NonNull Context context, @NonNull Uri uri) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));

        try {
            final PackageManager packageManager = context.getPackageManager();
            final ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);
            final ResolveInfo browserInfo = packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
            final String resolvedPackageName = resolveInfo != null ? resolveInfo.activityInfo.packageName : null;
            final String browserPackageName = browserInfo != null ? browserInfo.activityInfo.packageName : null;

            if (resolvedPackageName != null) {
                if (resolvedPackageName.equals(RESOLVER_ACTIVITY_PACKAGE_NAME)) {
                    return new ResolveResult(RESOLVER_ACTIVITY, resolveInfo);
                } else if (resolvedPackageName.equals(browserPackageName)) {
                    return new ResolveResult(DEFAULT_BROWSER, resolveInfo);
                } else {
                    return new ResolveResult(APPLICATION, resolveInfo);
                }
            }
        } catch (Exception e) {
            return new ResolveResult(UNKNOWN, null);
        }

        return new ResolveResult(UNKNOWN, null);
    }

    /**
     * A redirect may return to the application using the ReturnUrl when properly setup in an Intent Filter. Is usually contains result information
     * as parameters on that returnUrl. This method parses those results and returns a {@link JSONObject} to be used in the details call.
     *
     * @param data The returned Uri
     * @return The parsed value to be passed on the payments/details call, on the details parameter.
     */
    @NonNull
    public static JSONObject parseRedirectResult(@NonNull Uri data) throws CheckoutException {
        Logger.d(TAG, "parseRedirectResult - " + data.toString());

        final JSONObject result = new JSONObject();

        for (String parameter : data.getQueryParameterNames()) {
            if (PAYLOAD_PARAMETER.equals(parameter)) {
                try {
                    // getQueryParameter already does HTML decoding
                    result.put(PAYLOAD_PARAMETER, data.getQueryParameter(parameter));
                    return result;
                } catch (JSONException e) {
                    throw new CheckoutException("Error creating Redirect payload.", e);
                }
            }
            if (REDIRECT_RESULT_PARAMETER.equals(parameter)) {
                try {
                    // getQueryParameter already does HTML decoding
                    result.put(REDIRECT_RESULT_PARAMETER, data.getQueryParameter(parameter));
                    return result;
                } catch (JSONException e) {
                    throw new CheckoutException("Error creating Redirect result parameter.", e);
                }
            }
        }

        Logger.d(TAG, "Fallback to returnUrlQueryString");
        final String returnUrlQueryString = data.getQuery();
        try {
            result.put(RETURN_URL_QUERY_PARAMETER, returnUrlQueryString);
            return result;
        } catch (JSONException e) {
            throw new CheckoutException("Error creating Redirect returnUrlQueryString.", e);
        }
    }

    /**
     * Creates the intent that will start the redirect.
     * @param context Any context.
     * @param uri The Uri to redirect to.
     * @return And intent that targets either another app or a Web page.
     */
    @SuppressWarnings(Lint.MERCHANT_VISIBLE)
    @NonNull
    public static Intent createRedirectIntent(@NonNull Context context, @NonNull Uri uri) {
        if (RedirectUtil.determineResolveResult(context, uri).getType() == APPLICATION) {
            return new Intent(Intent.ACTION_VIEW, uri);
        } else {
            final CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setToolbarColor(ThemeUtil.getPrimaryThemeColor(context))
                    .build();
            customTabsIntent.intent.setData(uri);

            return customTabsIntent.intent;
        }
    }

    private RedirectUtil() {
        throw new NoConstructorException();
    }
}

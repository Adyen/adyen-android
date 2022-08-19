/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/8/2022.
 */
package com.adyen.checkout.redirect.handler

import android.content.Context
import android.net.Uri
import com.adyen.checkout.core.exception.CheckoutException
import org.json.JSONObject

interface RedirectHandler {
    /**
     * A redirect may return to the application using the ReturnUrl when properly setup in an Intent Filter. Is usually contains result information
     * as parameters on that returnUrl. This method parses those results and returns a [JSONObject] to be used in the details call.
     *
     * @param data The returned Uri
     * @return The parsed value to be passed on the payments/details call, on the details parameter.
     */
    @Throws(CheckoutException::class)
    fun parseRedirectResult(data: Uri?): JSONObject

    fun launchUriRedirect(context: Context, url: String?)
}

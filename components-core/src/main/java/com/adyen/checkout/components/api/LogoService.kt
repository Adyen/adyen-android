/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/3/2019.
 */
package com.adyen.checkout.components.api

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

private val TAG = LogUtil.getTag()

internal class LogoService {

    fun getLogo(logoUrl: String): BitmapDrawable {
        Logger.v(TAG, "call - " + logoUrl.hashCode())

        val httpClient = HttpClientFactory.getHttpClient(logoUrl)
        val bytes = httpClient.get("")
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        return BitmapDrawable(Resources.getSystem(), bitmap)
    }
}

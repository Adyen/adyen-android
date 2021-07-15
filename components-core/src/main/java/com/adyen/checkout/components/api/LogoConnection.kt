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
import com.adyen.checkout.core.api.Connection
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import java.io.IOException

private val TAG = LogUtil.getTag()

/**
 * Connection that gets a Logo [BitmapDrawable] from a URL.
 */
class LogoConnection(logoUrl: String) : Connection<BitmapDrawable>(logoUrl) {
    @Throws(IOException::class)
    override fun call(): BitmapDrawable {
        Logger.v(TAG, "call - " + url.hashCode())
        val bytes = get()
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return BitmapDrawable(Resources.getSystem(), bitmap)
    }
}

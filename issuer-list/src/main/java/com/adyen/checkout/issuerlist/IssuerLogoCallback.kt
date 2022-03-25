/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/4/2019.
 */
package com.adyen.checkout.issuerlist

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.adyen.checkout.components.api.LogoConnectionTask.LogoCallback

class IssuerLogoCallback internal constructor(private val mIssuerId: String, private val mDrawableFetchedCallback: DrawableFetchedCallback) :
    LogoCallback {
    override fun onLogoReceived(drawable: BitmapDrawable) {
        mDrawableFetchedCallback.onDrawableFetched(mIssuerId, drawable)
    }

    override fun onReceiveFailed() {
        mDrawableFetchedCallback.onDrawableFetched(mIssuerId, null)
    }

    /**
     * The callback for when the IssuerModel Drawable was fetched.
     * Drawable can be null if fetching failed.
     */
    internal interface DrawableFetchedCallback {
        fun onDrawableFetched(id: String, drawable: Drawable?)
    }
}

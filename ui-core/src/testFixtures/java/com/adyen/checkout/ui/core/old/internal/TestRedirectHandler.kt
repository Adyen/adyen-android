/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal

import android.content.Context
import android.net.Uri
import com.adyen.checkout.core.old.exception.ComponentException
import com.adyen.checkout.ui.core.internal.RedirectHandler
import org.json.JSONObject

/**
 * Test implementation of [RedirectHandler].
 */
class TestRedirectHandler : RedirectHandler {

    var exception: ComponentException? = null

    private var timesLaunchRedirectCalled = 0
    private var timesRemoveOnRedirectListenerCalled = 0

    override fun parseRedirectResult(data: Uri?): JSONObject {
        exception?.let { throw it }
        return REDIRECT_RESULT
    }

    override fun launchUriRedirect(context: Context, url: String?) {
        timesLaunchRedirectCalled++
        exception?.let { throw it }
    }

    fun assertLaunchRedirectCalled() =
        assert(timesLaunchRedirectCalled > 0)

    fun assertLaunchRedirectNotCalled() =
        assert(timesLaunchRedirectCalled <= 0)

    override fun setOnRedirectListener(listener: () -> Unit) = Unit

    override fun removeOnRedirectListener() {
        timesRemoveOnRedirectListenerCalled++
    }

    fun assertRemoveOnRedirectListenerCalled() =
        assert(timesRemoveOnRedirectListenerCalled > 0)

    companion object {
        val REDIRECT_RESULT = JSONObject().apply {
            put("redirect", "successful")
            put("returnUrlQueryString", "gpid=ajfbasljbfaljfe")
        }
    }
}

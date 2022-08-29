/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/8/2022.
 */

package com.adyen.checkout.redirect.test

import android.content.Context
import android.net.Uri
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.redirect.handler.RedirectHandler
import org.json.JSONObject

/**
 * Test implementation of [RedirectHandler]. This class should never be used except in test code.
 */
// TODO move to test fixtures once it becomes supported on Android
@RestrictTo(RestrictTo.Scope.TESTS)
class TestRedirectHandler : RedirectHandler {

    var exception: ComponentException? = null

    private var timesLaunchRedirectCalled = 0

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

    companion object {
        val REDIRECT_RESULT = JSONObject().apply { put("redirect", "successful") }
    }
}

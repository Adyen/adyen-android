/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 21/2/2023.
 */

package com.adyen.checkout.ui.core.internal.test

import android.content.Context
import android.net.Uri
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.RedirectMethod
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.ui.core.internal.RedirectHandler
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

    override fun setOnRedirectListener(listener: (RedirectMethod) -> Unit) = Unit

    override fun removeOnRedirectListener() = Unit

    companion object {
        val REDIRECT_RESULT = JSONObject().apply { put("redirect", "successful") }
    }
}

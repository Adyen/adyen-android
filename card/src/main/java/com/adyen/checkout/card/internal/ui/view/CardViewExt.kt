/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/2/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.WindowManager
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.BuildUtils
import com.adyen.checkout.core.internal.util.adyenLog

internal fun View.setFlagSecureOnRootView(enable: Boolean) {
    if (BuildUtils.isDebugBuild(context)) return

    val rootView = rootView
    val window = getActivity(context)?.window
    if (window != null && rootView == window.decorView) {
        if (enable) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    } else {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val params = rootView.layoutParams
        if (windowManager != null && params is WindowManager.LayoutParams) {
            if (enable) {
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_SECURE
            } else {
                params.flags = params.flags and WindowManager.LayoutParams.FLAG_SECURE.inv()
            }
            try {
                windowManager.updateViewLayout(rootView, params)
            } catch (e: IllegalArgumentException) {
                adyenLog(AdyenLogLevel.WARN, e) { "Failed to update view layout with secure flag" }
            }
        }
    }
}

private fun getActivity(context: Context): Activity? {
    return when (context) {
        is Activity -> context
        is ContextWrapper -> getActivity(context.baseContext)
        else -> null
    }
}

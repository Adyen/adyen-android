/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/10/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

@Composable
internal fun SecureWindowEffect() {
    val view = LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window ?: LocalActivity.current?.window

    DisposableEffect(window) {
        if (window == null) return@DisposableEffect onDispose {}

        val wasSecure = window.attributes.flags and FLAG_SECURE != 0
        if (!wasSecure) window.addFlags(FLAG_SECURE)

        onDispose {
            if (!wasSecure) window.clearFlags(FLAG_SECURE)
        }
    }
}

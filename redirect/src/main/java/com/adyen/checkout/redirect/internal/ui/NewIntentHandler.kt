/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/6/2026.
 */

package com.adyen.checkout.redirect.internal.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.util.Consumer

@Composable
internal fun NewIntentHandler(onNewIntent: (Intent) -> Unit) {
    val activity = LocalActivity.current as ComponentActivity
    val currentOnNewIntent by rememberUpdatedState(onNewIntent)
    DisposableEffect(activity) {
        val listener = Consumer(currentOnNewIntent)
        activity.addOnNewIntentListener(listener)

        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }
}

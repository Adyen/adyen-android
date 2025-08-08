/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/8/2025.
 */

package com.adyen.checkout.core.redirect.internal.ui

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.redirect.internal.RedirectHandler
import kotlinx.coroutines.flow.Flow

@Suppress("TooGenericExceptionCaught")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun RedirectEvent(
    redirectHandler: RedirectHandler,
    viewEventFlow: Flow<RedirectViewEvent>,
    onError: (RuntimeException) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(redirectHandler, viewEventFlow, onError) {
        viewEventFlow.collect { event ->
            when (event) {
                is RedirectViewEvent.Redirect -> {
                    try {
                        adyenLog(AdyenLogLevel.DEBUG) { "Attempting to launch redirect." }
                        redirectHandler.launchUriRedirect(context, event.url)
                    } catch (e: RuntimeException) {
                        // TODO - Error propagation
                        adyenLog(AdyenLogLevel.ERROR, e) { "Redirect failed." }
                        onError(e)
                    }
                }
            }
        }
    }
}

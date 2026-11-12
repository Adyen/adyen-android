/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/11/2025.
 */

package com.adyen.checkout.dropin

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultCaller
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.adyen.checkout.core.common.AdyenLogger.LogLevel
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.dropin.internal.DropInResultContract
import com.adyen.checkout.ui.theme.CheckoutTheme

// TODO - Discuss the current Drop-in approach. Do we need DropIn object and can we make improvement?
// TODO - KDocs
object DropIn {

    fun registerForResult(
        caller: ActivityResultCaller,
        callback: DropInResultCallback,
    ): DropInLauncher {
        val activityResultLauncher = caller.registerForActivityResult(DropInResultContract(), callback::onDropInResult)
        return DropInLauncher(activityResultLauncher)
    }

    @JvmOverloads
    fun start(
        launcher: DropInLauncher,
        dropInContext: CheckoutContext.Sessions,
        // TODO - define drop in session service
        serviceClass: Class<out DropInService> = DropInService::class.java,
        theme: CheckoutTheme = CheckoutTheme(),
    ) {
        launcher.launch(dropInContext, serviceClass, theme)
    }

    @JvmOverloads
    fun start(
        launcher: DropInLauncher,
        dropInContext: CheckoutContext.Advanced,
        serviceClass: Class<out DropInService>,
        theme: CheckoutTheme = CheckoutTheme(),
    ) {
        launcher.launch(dropInContext, serviceClass, theme)
    }

    /**
     * Turns the handling of redirect returns by [DropInRedirectHandlingActivity] on or off, together with every
     * intent filter declared on it.
     *
     * The system keeps this setting across app restarts and updates, and it takes precedence over `android:enabled`
     * in your manifest. While it is off, a redirect started by Drop-in can't return to it.
     *
     * @param context Any context of your app.
     * @param enabled Whether redirect returns are handled.
     */
    fun setRedirectHandlingEnabled(context: Context, enabled: Boolean) {
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        try {
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context, DropInRedirectHandlingActivity::class.java),
                state,
                PackageManager.DONT_KILL_APP,
            )
        } catch (e: IllegalArgumentException) {
            // Thrown when the merged manifest doesn't contain the activity, for example after tools:node="remove"
            adyenLog(LogLevel.ERROR, e) {
                "Can't turn redirect handling ${if (enabled) "on" else "off"}, " +
                    "${DropInRedirectHandlingActivity::class.java.name} is not in the manifest"
            }
        }
    }
}

@Composable
fun rememberLauncherForDropInResult(
    callback: DropInResultCallback
): DropInLauncher {
    val activityResultLauncher = rememberLauncherForActivityResult(DropInResultContract(), callback::onDropInResult)
    val dropInLauncher = remember { DropInLauncher(activityResultLauncher) }
    return dropInLauncher
}

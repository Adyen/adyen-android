/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/11/2025.
 */

package com.adyen.checkout.dropin

import android.app.Service
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultCaller
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.adyen.checkout.core.components.CheckoutContext
import com.adyen.checkout.dropin.internal.DropInResultContract

// TODO - KDocs
object DropIn {

    fun registerForResult(
        caller: ActivityResultCaller,
        callback: DropInResultCallback,
    ): DropInLauncher {
        val activityResultLauncher = caller.registerForActivityResult(DropInResultContract(), callback::onDropInResult)
        return DropInLauncher(activityResultLauncher)
    }

    fun start(
        launcher: DropInLauncher,
        dropInContext: CheckoutContext.Sessions,
        // TODO - define drop in session service
        serviceClass: Class<out Service> = Service::class.java,
    ) {
        launcher.launch(dropInContext, serviceClass)
    }

    fun start(
        launcher: DropInLauncher,
        dropInContext: CheckoutContext.Advanced,
        // TODO - define drop in service
        serviceClass: Class<out Service>,
    ) {
        launcher.launch(dropInContext, serviceClass)
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

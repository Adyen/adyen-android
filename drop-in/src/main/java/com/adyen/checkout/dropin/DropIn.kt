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
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable

// TODO - KDocs
object DropIn {

    fun registerForResult(
        caller: ActivityResultCaller,
        callback: DropInResultCallback
    ): ActivityResultLauncher<DropInResultContract.Input> {
        return caller.registerForActivityResult(DropInResultContract(), callback::onDropInResult)
    }

    fun start(
        launcher: ActivityResultLauncher<DropInResultContract.Input>,
        dropInContext: CheckoutDropInContext.Sessions,
        // TODO - define drop in session service and add default value
        serviceClass: Class<out Service>,
    ) {
        val input = createLauncherInput(dropInContext, serviceClass)
        launcher.launch(input)
    }

    fun start(
        launcher: ActivityResultLauncher<DropInResultContract.Input>,
        dropInContext: CheckoutDropInContext.Advanced,
        // TODO - define drop in service
        serviceClass: Class<out Service>,
    ) {
        val input = createLauncherInput(dropInContext, serviceClass)
        launcher.launch(input)
    }

    private fun createLauncherInput(
        dropInContext: CheckoutDropInContext,
        // TODO - define drop in service
        serviceClass: Class<out Service>,
    ): DropInResultContract.Input {
        return DropInResultContract.Input(
            dropInContext = dropInContext,
            serviceClass = serviceClass,
        )
    }
}

@Composable
fun rememberLauncherForDropInResult(
    callback: DropInResultCallback
): ActivityResultLauncher<DropInResultContract.Input> {
    return rememberLauncherForActivityResult(DropInResultContract(), callback::onDropInResult)
}

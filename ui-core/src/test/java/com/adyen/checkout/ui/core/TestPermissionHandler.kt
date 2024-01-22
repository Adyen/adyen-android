/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/1/2024.
 */

package com.adyen.checkout.ui.core

import android.content.Context
import com.adyen.checkout.core.internal.ui.PermissionHandler
import com.adyen.checkout.core.PermissionHandlerCallback

class TestPermissionHandler(
    private val shouldGrantPermission: Boolean = false
) : PermissionHandler {
    override fun requestPermission(context: Context, requiredPermission: String, callback: PermissionHandlerCallback) {
        if (shouldGrantPermission) {
            callback.onPermissionGranted(requiredPermission)
        } else {
            callback.onPermissionDenied(requiredPermission)
        }
    }
}

class TestPermissionHandlerWithDifferentPermission : PermissionHandler {
    override fun requestPermission(context: Context, requiredPermission: String, callback: PermissionHandlerCallback) {
        callback.onPermissionGranted("Different_$requiredPermission")
    }
}

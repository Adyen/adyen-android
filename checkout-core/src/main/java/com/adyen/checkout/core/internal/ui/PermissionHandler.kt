/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/1/2024.
 */

package com.adyen.checkout.core.internal.ui

import android.content.Context

interface PermissionHandler {
    fun requestPermission(context: Context, requiredPermission: String, callback: PermissionHandlerCallback)
}

interface PermissionHandlerCallback {
    fun onPermissionGranted(requestedPermission: String)
    fun onPermissionDenied(requestedPermission: String)
}

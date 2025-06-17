/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/1/2024.
 */

package com.adyen.checkout.ui.core.internal.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.adyen.checkout.core.old.PermissionHandlerCallback
import com.adyen.checkout.core.old.internal.ui.PermissionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal suspend fun PermissionHandler.checkPermission(
    context: Context,
    requiredPermission: String
): PermissionHandlerResult = suspendCancellableCoroutine { continuation ->
    if (ContextCompat.checkSelfPermission(context, requiredPermission) == PackageManager.PERMISSION_GRANTED) {
        continuation.resume(PermissionHandlerResult.PERMISSION_GRANTED)
        return@suspendCancellableCoroutine
    }

    requestPermission(
        context = context,
        requiredPermission = requiredPermission,
        callback = object : PermissionHandlerCallback {
            override fun onPermissionGranted(requestedPermission: String) {
                if (requestedPermission == requiredPermission) {
                    continuation.resume(PermissionHandlerResult.PERMISSION_GRANTED)
                } else {
                    continuation.resume(PermissionHandlerResult.WRONG_PERMISSION)
                }
            }

            override fun onPermissionDenied(requestedPermission: String) {
                continuation.resume(PermissionHandlerResult.PERMISSION_DENIED)
            }

            override fun onPermissionRequestNotHandled(requestedPermission: String) {
                continuation.resume(PermissionHandlerResult.PERMISSION_REQUEST_NOT_HANDLED)
            }
        },
    )
}

internal enum class PermissionHandlerResult {
    PERMISSION_GRANTED,
    PERMISSION_DENIED,
    PERMISSION_REQUEST_NOT_HANDLED,
    WRONG_PERMISSION,
}

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
import com.adyen.checkout.core.PermissionHandlerCallback
import com.adyen.checkout.core.internal.ui.PermissionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal suspend fun PermissionHandler.checkPermission(context: Context, requiredPermission: String): Boolean? =
    suspendCancellableCoroutine { continuation ->
        if (ContextCompat.checkSelfPermission(context, requiredPermission) == PackageManager.PERMISSION_GRANTED) {
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }

        requestPermission(
            context = context,
            requiredPermission = requiredPermission,
            callback = object : PermissionHandlerCallback {
                override fun onPermissionGranted(requestedPermission: String) {
                    if (requestedPermission == requiredPermission) {
                        continuation.resume(true)
                    } else {
                        continuation.resume(null)
                    }
                }

                override fun onPermissionDenied(requestedPermission: String) {
                    continuation.resume(false)
                }
            },
        )
    }
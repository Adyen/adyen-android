/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/1/2024.
 */

package com.adyen.checkout.core.internal.ui

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.PermissionHandlerCallback

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PermissionHandler {
    fun requestPermission(context: Context, requiredPermission: String, callback: PermissionHandlerCallback)
}

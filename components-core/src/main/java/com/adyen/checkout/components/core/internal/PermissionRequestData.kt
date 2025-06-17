/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/1/2024.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.PermissionHandlerCallback

/**
 * Runtime permission request data
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class PermissionRequestData(
    val requiredPermission: String,
    val permissionCallback: PermissionHandlerCallback
)

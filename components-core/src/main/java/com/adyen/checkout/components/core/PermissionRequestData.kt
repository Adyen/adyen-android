/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/1/2024.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.core.internal.ui.PermissionHandlerCallback

/**
 * Runtime permission request data
 */
data class PermissionRequestData(
    val requiredPermission: String,
    val permissionCallback: PermissionHandlerCallback
)

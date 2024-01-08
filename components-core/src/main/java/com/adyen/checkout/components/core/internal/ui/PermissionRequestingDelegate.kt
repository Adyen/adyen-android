/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/1/2024.
 */

package com.adyen.checkout.components.core.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.PermissionRequestData
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PermissionRequestingDelegate {

    val permissionFlow: Flow<PermissionRequestData>
}

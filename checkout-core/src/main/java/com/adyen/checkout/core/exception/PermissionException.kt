/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 16/11/2022.
 */

package com.adyen.checkout.core.exception

import androidx.annotation.RestrictTo

/**
 *
 * This exception indicates that the required runtime permission is not granted.
 */

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PermissionException(
    errorMessage: String,
    val requiredPermission: String
) : CheckoutException(errorMessage)

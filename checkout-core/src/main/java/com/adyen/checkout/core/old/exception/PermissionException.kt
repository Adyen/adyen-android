/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 16/11/2022.
 */

package com.adyen.checkout.core.old.exception

/**
 * This exception indicates that the required runtime permission is not granted.
 */
@Deprecated(
    message = "This exception is not being used anymore. " +
        "To handle runtime permissions, override onPermissionRequest() from ActionComponentCallback."
)
class PermissionException(
    errorMessage: String,
    val requiredPermission: String
) : CheckoutException(errorMessage)

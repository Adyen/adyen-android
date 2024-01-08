/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/1/2024.
 */

package com.adyen.checkout.ui.core.internal.exception

import com.adyen.checkout.core.exception.CheckoutException

/**
 * Exception thrown when requested runtime permission is denied.
 */
class PermissionException(errorMessage: String) : CheckoutException(errorMessage)

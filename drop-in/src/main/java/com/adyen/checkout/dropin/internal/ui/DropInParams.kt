/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

internal data class DropInParams(
    val hideStoredPaymentMethods: Boolean,
    val startWithLastStoredPaymentMethod: Boolean,
)

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

internal data class PreselectedPaymentMethodViewState(
    val logoTxVariant: String,
    val title: String,
    val subtitle: String,
    val payButtonText: String,
)

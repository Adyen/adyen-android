/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/6/2026.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.common.CheckoutResultCode

/**
 * The result of a payment using the advanced flow.
 *
 * @property resultCode The result code of the payment.
 */
data class AdvancedCheckoutResult(
    val resultCode: CheckoutResultCode,
)

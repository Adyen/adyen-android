/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/3/2026.
 */

package com.adyen.checkout.core.components

interface CheckoutTarget {
    data class PaymentMethod(val type: String) : CheckoutTarget
    data class StoredPaymentMethod(val id: String) : CheckoutTarget
}

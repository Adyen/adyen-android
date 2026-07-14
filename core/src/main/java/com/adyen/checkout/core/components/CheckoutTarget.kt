/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/3/2026.
 */

package com.adyen.checkout.core.components

/**
 * Identifies which payment method a [CheckoutController] should be created for.
 */
interface CheckoutTarget {

    /**
     * Targets a regular payment method by its type.
     *
     * @param type The payment method type, as it appears in the `/paymentMethods` response.
     */
    data class PaymentMethod(val type: String) : CheckoutTarget

    /**
     * Targets a stored payment method by its id.
     *
     * @param id The id of the stored payment method, as it appears in the `/paymentMethods` response.
     */
    data class StoredPaymentMethod(val id: String) : CheckoutTarget
}

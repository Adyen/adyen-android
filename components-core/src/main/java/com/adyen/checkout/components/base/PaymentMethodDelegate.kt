/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/11/2020.
 */

package com.adyen.checkout.components.base

/**
 * Differentiates between a regular PaymentMethod and a StoredPaymentMethod
 */
interface PaymentMethodDelegate {

    fun getPaymentMethodType(): String
}

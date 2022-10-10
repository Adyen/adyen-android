/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/6/2022.
 */

package com.adyen.checkout.components.base

/**
 * Handles all the logic in payment components
 */
interface PaymentMethodDelegate : ComponentDelegate {

    fun getPaymentMethodType(): String
}

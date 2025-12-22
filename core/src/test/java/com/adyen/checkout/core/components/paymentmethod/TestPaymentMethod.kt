/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2025.
 */

package com.adyen.checkout.core.components.paymentmethod

import kotlinx.parcelize.Parcelize

@Parcelize
class TestPaymentMethod(
    override var type: String? = "test",
    override var checkoutAttemptId: String? = null,
    override var sdkData: String? = null,
) : PaymentMethodDetails()

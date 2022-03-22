/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/3/2022.
 */

package com.adyen.checkout.components.models

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentComponentData

class TestComponentState : PaymentComponentState<TestPaymentMethod>(
    data = PaymentComponentData(
        paymentMethod = null,
        storePaymentMethod = false,
        shopperReference = null,
        amount = null,
        billingAddress = null,
        deliveryAddress = null,
        shopperName = null,
        telephoneNumber = null,
        shopperEmail = null,
        dateOfBirth = null,
        socialSecurityNumber = null,
        installments = null,
        order = null
    ),
    isInputValid = false,
    isReady = false
)

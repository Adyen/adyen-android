/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

/**
 * Abstract class representing a payment method from the /paymentMethods API response.
 *
 * Specific payment method types extend this class with their own fields.
 * Unknown payment methods are deserialized as [InstantPaymentMethod].
 */
abstract class PaymentMethod : PaymentMethodResponse()

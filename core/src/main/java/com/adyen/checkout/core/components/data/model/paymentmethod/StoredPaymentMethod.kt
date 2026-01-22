/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

/**
 * Abstract class representing a stored payment method from the /paymentMethods API response.
 *
 * Specific stored payment method types extend this class with their own fields.
 * Unknown stored payment methods are deserialized as [StoredInstantPaymentMethod].
 */
abstract class StoredPaymentMethod : PaymentMethodResponse() {
    abstract val id: String
    abstract val supportedShopperInteractions: List<String>

    // TODO: Move this where it is being used
    val isEcommerce: Boolean
        get() = supportedShopperInteractions.contains(ECOMMERCE)

    companion object {
        const val ID = "id"
        const val SUPPORTED_SHOPPER_INTERACTIONS = "supportedShopperInteractions"
        private const val ECOMMERCE = "Ecommerce"
    }
}

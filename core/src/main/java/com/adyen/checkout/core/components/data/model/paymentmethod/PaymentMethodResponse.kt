/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.model.ModelObject

/**
 * Abstract parent class for [PaymentMethod] and [StoredPaymentMethod].
 */
abstract class PaymentMethodResponse
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor() : ModelObject() {

    abstract val type: String
    abstract val name: String

    companion object {
        const val TYPE = "type"
        const val NAME = "name"
    }
}

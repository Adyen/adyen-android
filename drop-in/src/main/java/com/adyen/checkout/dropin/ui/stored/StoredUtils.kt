/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/12/2020.
 */

package com.adyen.checkout.dropin.ui.stored

import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
import com.adyen.checkout.core.Environment
import com.adyen.checkout.dropin.ui.paymentmethods.GenericStoredModel
import com.adyen.checkout.dropin.ui.paymentmethods.StoredCardModel
import com.adyen.checkout.dropin.ui.paymentmethods.StoredPaymentMethodModel

internal fun StoredPaymentMethod.mapStoredModel(
    isRemovingEnabled: Boolean,
    environment: Environment
): StoredPaymentMethodModel {
    return when (this.type) {
        PaymentMethodTypes.SCHEME -> with(this) {
            StoredCardModel(
                id.orEmpty(),
                brand.orEmpty(),
                isRemovingEnabled,
                lastFour.orEmpty(),
                expiryMonth.orEmpty(),
                expiryYear.orEmpty(),
                environment,
            )
        }
        else -> GenericStoredModel(
            id.orEmpty(),
            type.orEmpty(),
            isRemovingEnabled,
            name.orEmpty(),
            environment,
        )
    }
}

internal fun StoredPaymentMethod.isStoredPaymentSupported(): Boolean {
    return !type.isNullOrEmpty() &&
        !id.isNullOrEmpty() &&
        PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(type) &&
        isEcommerce
}

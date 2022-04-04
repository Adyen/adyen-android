/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/12/2020.
 */

package com.adyen.checkout.dropin.ui.stored

import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.dropin.ui.paymentmethods.GenericStoredModel
import com.adyen.checkout.dropin.ui.paymentmethods.StoredCardModel
import com.adyen.checkout.dropin.ui.paymentmethods.StoredPaymentMethodModel

internal fun makeStoredModel(
    storedPaymentMethod: StoredPaymentMethod,
    isRemovingEnabled: Boolean
): StoredPaymentMethodModel {
    return when (storedPaymentMethod.type) {
        PaymentMethodTypes.SCHEME -> {
            StoredCardModel(
                storedPaymentMethod.id.orEmpty(),
                storedPaymentMethod.brand.orEmpty(),
                isRemovingEnabled,
                storedPaymentMethod.lastFour.orEmpty(),
                storedPaymentMethod.expiryMonth.orEmpty(),
                storedPaymentMethod.expiryYear.orEmpty()
            )
        }
        else -> GenericStoredModel(
            storedPaymentMethod.id.orEmpty(),
            storedPaymentMethod.type.orEmpty(),
            isRemovingEnabled,
            storedPaymentMethod.name.orEmpty()
        )
    }
}

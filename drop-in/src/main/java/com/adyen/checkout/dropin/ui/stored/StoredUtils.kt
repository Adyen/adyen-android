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

internal fun makeStoredModel(storedPaymentMethod: StoredPaymentMethod, isRemovingEnabled: Boolean): StoredPaymentMethodModel {
    return when (storedPaymentMethod.type) {
        PaymentMethodTypes.SCHEME -> {
            StoredCardModel(
                id = storedPaymentMethod.id.orEmpty(),
                imageId = storedPaymentMethod.brand.orEmpty(),
                isRemovable = isRemovingEnabled,
                lastFour = storedPaymentMethod.lastFour.orEmpty(),
                expiryMonth = storedPaymentMethod.expiryMonth.orEmpty(),
                expiryYear = storedPaymentMethod.expiryYear.orEmpty()
            )
        }

        PaymentMethodTypes.CASH_APP_PAY -> {
            GenericStoredModel(
                id = storedPaymentMethod.id.orEmpty(),
                imageId = storedPaymentMethod.type.orEmpty(),
                isRemovable = isRemovingEnabled,
                name = storedPaymentMethod.cashtag.orEmpty(),
                description = storedPaymentMethod.name.orEmpty(),
            )
        }

        else -> {
            GenericStoredModel(
                id = storedPaymentMethod.id.orEmpty(),
                imageId = storedPaymentMethod.type.orEmpty(),
                isRemovable = isRemovingEnabled,
                name = storedPaymentMethod.name.orEmpty(),
                description = null,
            )
        }
    }
}

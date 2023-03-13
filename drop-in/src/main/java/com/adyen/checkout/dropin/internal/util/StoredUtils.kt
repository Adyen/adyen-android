/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/12/2020.
 */

package com.adyen.checkout.dropin.internal.util

import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.Environment
import com.adyen.checkout.dropin.internal.ui.model.GenericStoredModel
import com.adyen.checkout.dropin.internal.ui.model.StoredACHDirectDebitModel
import com.adyen.checkout.dropin.internal.ui.model.StoredCardModel
import com.adyen.checkout.dropin.internal.ui.model.StoredPaymentMethodModel

internal fun StoredPaymentMethod.mapStoredModel(
    isRemovingEnabled: Boolean,
    environment: Environment
): StoredPaymentMethodModel {
    return when (this.type) {
        PaymentMethodTypes.SCHEME -> with(this) {
            StoredCardModel(
                id = id.orEmpty(),
                imageId = brand.orEmpty(),
                isRemovable = isRemovingEnabled,
                lastFour = lastFour.orEmpty(),
                expiryMonth = expiryMonth.orEmpty(),
                expiryYear = expiryYear.orEmpty(),
                environment = environment,
            )
        }
        PaymentMethodTypes.ACH -> with(this) {
            StoredACHDirectDebitModel(
                id = id.orEmpty(),
                imageId = type.orEmpty(),
                isRemovable = isRemovingEnabled,
                lastFour = bankAccountNumber?.takeLast(LAST_FOUR_LENGTH) ?: "",
                environment = environment,
            )
        }
        else -> GenericStoredModel(
            id = id.orEmpty(),
            imageId = type.orEmpty(),
            isRemovable = isRemovingEnabled,
            name = name.orEmpty(),
            environment = environment,
        )
    }
}

internal fun StoredPaymentMethod.isStoredPaymentSupported(): Boolean {
    return !type.isNullOrEmpty() &&
        !id.isNullOrEmpty() &&
        PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(type) &&
        isEcommerce
}

private const val LAST_FOUR_LENGTH = 4

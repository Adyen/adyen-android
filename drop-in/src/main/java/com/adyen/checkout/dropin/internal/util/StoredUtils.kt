/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/12/2020.
 */

package com.adyen.checkout.dropin.internal.util

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.dropin.internal.ui.model.GenericStoredModel
import com.adyen.checkout.dropin.internal.ui.model.StoredACHDirectDebitModel
import com.adyen.checkout.dropin.internal.ui.model.StoredCardModel
import com.adyen.checkout.dropin.internal.ui.model.StoredPayByBankUSModel
import com.adyen.checkout.dropin.internal.ui.model.StoredPayToModel
import com.adyen.checkout.dropin.internal.ui.model.StoredPaymentMethodModel

@Suppress("LongMethod")
internal fun StoredPaymentMethod.mapStoredModel(
    isRemovingEnabled: Boolean,
    environment: Environment
): StoredPaymentMethodModel {
    return when (this.type) {
        PaymentMethodTypes.SCHEME -> {
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

        PaymentMethodTypes.ACH -> {
            StoredACHDirectDebitModel(
                id = id.orEmpty(),
                imageId = type.orEmpty(),
                isRemovable = isRemovingEnabled,
                lastFour = bankAccountNumber?.takeLast(LAST_FOUR_LENGTH) ?: "",
                environment = environment,
            )
        }

        PaymentMethodTypes.CASH_APP_PAY -> {
            GenericStoredModel(
                id = id.orEmpty(),
                imageId = type.orEmpty(),
                isRemovable = isRemovingEnabled,
                name = cashtag.orEmpty(),
                description = name,
                environment = environment,
            )
        }

        PaymentMethodTypes.PAY_BY_BANK_US -> {
            StoredPayByBankUSModel(
                id = id.orEmpty(),
                imageId = type.orEmpty(),
                isRemovable = isRemovingEnabled,
                name = label.orEmpty(),
                description = name,
                environment = environment,
            )
        }

        PaymentMethodTypes.PAY_TO -> {
            StoredPayToModel(
                id = id.orEmpty(),
                imageId = type.orEmpty(),
                isRemovable = isRemovingEnabled,
                name = label.orEmpty(),
                description = name,
                environment = environment,
            )
        }

        else -> GenericStoredModel(
            id = id.orEmpty(),
            imageId = type.orEmpty(),
            isRemovable = isRemovingEnabled,
            name = name.orEmpty(),
            description = null,
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

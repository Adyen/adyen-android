/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.dropin.old.internal.ui.model.PaymentMethodModel
import com.adyen.checkout.dropin.old.internal.ui.model.StoredPaymentMethodModel
import com.adyen.checkout.dropin.old.internal.util.isStoredPaymentSupported
import com.adyen.checkout.dropin.old.internal.util.mapStoredModel

internal object Helpers {

    internal fun List<StoredPaymentMethod>.mapToStoredPaymentMethodsModelList(
        isRemovingStoredPaymentMethodsEnabled: Boolean
    ): List<StoredPaymentMethodModel> = mapNotNull { storedPaymentMethod ->
        if (storedPaymentMethod.isStoredPaymentSupported()) {
            storedPaymentMethod.mapStoredModel(isRemovingStoredPaymentMethodsEnabled, Environment.TEST)
        } else {
            null
        }
    }

    internal fun List<PaymentMethod>.mapToPaymentMethodModelList(): List<PaymentMethodModel> =
        mapIndexed { index, paymentMethod ->
            paymentMethod.mapToModel(index)
        }

    private fun PaymentMethod.mapToModel(index: Int): PaymentMethodModel {
        val icon = when (type) {
            PaymentMethodTypes.SCHEME -> CARD_LOGO_TYPE
            PaymentMethodTypes.GOOGLE_PAY_LEGACY -> GOOGLE_PAY_LOGO_TYPE
            PaymentMethodTypes.GIFTCARD -> brand
            else -> type
        }
        val drawIconBorder = icon != GOOGLE_PAY_LOGO_TYPE
        return PaymentMethodModel(
            index = index,
            type = type.orEmpty(),
            name = name.orEmpty(),
            icon = icon.orEmpty(),
            drawIconBorder = drawIconBorder,
            environment = Environment.TEST,
            brandList = emptyList(),
        )
    }

    private const val CARD_LOGO_TYPE = "card"
    private const val GOOGLE_PAY_LOGO_TYPE = PaymentMethodTypes.GOOGLE_PAY
}

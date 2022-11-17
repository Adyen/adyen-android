/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 28/10/2022.
 */

package com.adyen.checkout.util

import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodModel
import com.adyen.checkout.dropin.ui.paymentmethods.StoredPaymentMethodModel
import com.adyen.checkout.dropin.ui.stored.isStoredPaymentSupported
import com.adyen.checkout.dropin.ui.stored.mapStoredModel

internal object Helpers {

    internal fun List<StoredPaymentMethod>.mapToStoredPaymentMethodsModelList(
        isRemovingStoredPaymentMethodsEnabled: Boolean
    ): List<StoredPaymentMethodModel> = mapNotNull { storedPaymentMethod ->
        if (storedPaymentMethod.isStoredPaymentSupported()) {
            storedPaymentMethod.mapStoredModel(isRemovingStoredPaymentMethodsEnabled)
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
            drawIconBorder = drawIconBorder
        )
    }

    private const val CARD_LOGO_TYPE = "card"
    private const val GOOGLE_PAY_LOGO_TYPE = PaymentMethodTypes.GOOGLE_PAY
}

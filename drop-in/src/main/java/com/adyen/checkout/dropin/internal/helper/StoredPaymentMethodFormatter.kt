/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/2/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.format
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredACHDirectDebitPaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCashAppPayPaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPayByBankUSPaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPayToPaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethodResponse
import com.adyen.checkout.dropin.internal.ui.UiText
import java.util.Locale

internal object StoredPaymentMethodFormatter {

    private const val LAST_DIGITS_COUNT = 4

    fun getIcon(storedPaymentMethod: StoredPaymentMethodResponse): String {
        return when (storedPaymentMethod) {
            is StoredCardPaymentMethodResponse -> storedPaymentMethod.brand
            else -> storedPaymentMethod.type
        }
    }

    fun getTitle(storedPaymentMethod: StoredPaymentMethodResponse): String {
        return when (storedPaymentMethod) {
            is StoredCashAppPayPaymentMethodResponse -> storedPaymentMethod.cashtag
            is StoredPayByBankUSPaymentMethodResponse -> storedPaymentMethod.label.orEmpty()
            is StoredPayToPaymentMethodResponse -> storedPaymentMethod.label
            is StoredACHDirectDebitPaymentMethodResponse -> {
                "•••• ${storedPaymentMethod.bankAccountNumber.takeLast(LAST_DIGITS_COUNT)}"
            }

            is StoredCardPaymentMethodResponse -> "•••• ${storedPaymentMethod.lastFour}"
            else -> storedPaymentMethod.name
        }
    }

    fun getSubtitle(storedPaymentMethod: StoredPaymentMethodResponse): String? {
        return when (storedPaymentMethod) {
            is StoredACHDirectDebitPaymentMethodResponse,
            is StoredCashAppPayPaymentMethodResponse,
            is StoredPayByBankUSPaymentMethodResponse,
            is StoredPayToPaymentMethodResponse,
            is StoredCardPaymentMethodResponse -> storedPaymentMethod.name

            else -> null
        }
    }

    fun getRemovalTitle(storedPaymentMethod: StoredPaymentMethodResponse): String {
        // TODO - Treat StoredBCMCPaymentMethod like a card once stored Bancontact is supported, like iOS
        return when (storedPaymentMethod) {
            is StoredACHDirectDebitPaymentMethodResponse,
            is StoredCardPaymentMethodResponse -> "${storedPaymentMethod.name} ${getTitle(storedPaymentMethod)}"

            else -> storedPaymentMethod.name
        }
    }

    fun getDescription(storedPaymentMethod: StoredPaymentMethodResponse, amount: Amount?, locale: Locale): UiText {
        val name = storedPaymentMethod.name
        return when {
            amount == null -> UiText.Localized(
                CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION,
                listOf(name),
            )

            amount.value == 0L -> UiText.Localized(
                CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION_SAVE_DETAILS,
                listOf(name),
            )

            else -> UiText.Localized(
                CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION_WITH_AMOUNT,
                listOf(name, amount.format(locale)),
            )
        }
    }
}

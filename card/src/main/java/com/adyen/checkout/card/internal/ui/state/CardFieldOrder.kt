/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

private val CANONICAL_ORDER = listOf(
    CardFieldId.CARD_NUMBER,
    CardFieldId.EXPIRY_DATE,
    CardFieldId.SECURITY_CODE,
    CardFieldId.HOLDER_NAME,
    CardFieldId.SOCIAL_SECURITY_NUMBER,
    CardFieldId.KCP_BIRTH_DATE_OR_TAX_NUMBER,
    CardFieldId.KCP_CARD_PASSWORD,
    CardFieldId.POSTAL_CODE,
    CardFieldId.STORE_PAYMENT_METHOD,
    CardFieldId.INSTALLMENTS,
)

/**
 * The order the card form is laid out in, ignoring which fields are currently visible. This is the one place the
 * sequence is decided, so it is also where a future experiment would reorder it.
 */
internal fun canonicalCardFieldOrder(): List<CardFieldId> = CANONICAL_ORDER

/**
 * The fields the shopper can currently see, in the order they see them.
 *
 * Visibility is read from the state rather than kept alongside it, so that it cannot drift from what the view state
 * producer puts on screen.
 */
internal fun visibleCardFields(state: CardComponentState): List<CardFieldId> =
    canonicalCardFieldOrder().filter { state.isVisible(it) }

private fun CardComponentState.isVisible(id: CardFieldId): Boolean = when (id) {
    CardFieldId.CARD_NUMBER -> cardNumber.isVisible
    CardFieldId.EXPIRY_DATE -> expiryDate.isVisible
    CardFieldId.SECURITY_CODE -> securityCode.isVisible
    CardFieldId.HOLDER_NAME -> holderName.isVisible
    CardFieldId.SOCIAL_SECURITY_NUMBER -> socialSecurityNumber.isVisible
    CardFieldId.KCP_BIRTH_DATE_OR_TAX_NUMBER -> kcpBirthDateOrTaxNumber.isVisible
    CardFieldId.KCP_CARD_PASSWORD -> kcpCardPassword.isVisible
    CardFieldId.POSTAL_CODE -> postalCode.isVisible
    CardFieldId.STORE_PAYMENT_METHOD -> isStorePaymentFieldVisible
    CardFieldId.INSTALLMENTS -> installmentState.installmentOptions.isNotEmpty()
}

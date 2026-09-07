/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementState
import com.adyen.checkout.core.components.internal.ui.state.form.FormState
import com.adyen.checkout.core.components.internal.ui.state.form.toFormElementIfVisible

/**
 * Derives the card form from a card state: which elements the shopper can see, and in which order.
 *
 * Both are read from the state rather than stored, so neither can drift from what the producer puts on screen.
 */
internal class CardFormStateFactory(
    private val state: CardComponentState,
) {

    fun create(): FormState<CardFormElementId> = FormState(
        elements = CANONICAL_ORDER.mapNotNull { id -> elementFor(id) },
    )

    /**
     * Do not add an else branch. Every id is listed so that a new [CardFormElementId] fails to compile until its
     * visibility and validity are decided; the alternative is a field silently missing from the screen, or one that
     * never holds up a payment.
     */
    private fun elementFor(id: CardFormElementId): FormElementState<CardFormElementId>? = with(state) {
        when (id) {
            CardFormElementId.CARD_NUMBER -> cardNumber.toFormElementIfVisible(id)
            CardFormElementId.EXPIRY_DATE -> expiryDate.toFormElementIfVisible(id)
            CardFormElementId.SECURITY_CODE -> securityCode.toFormElementIfVisible(id)
            CardFormElementId.HOLDER_NAME -> holderName.toFormElementIfVisible(id)
            CardFormElementId.SOCIAL_SECURITY_NUMBER -> socialSecurityNumber.toFormElementIfVisible(id)
            CardFormElementId.KCP_BIRTH_DATE_OR_TAX_NUMBER -> kcpBirthDateOrTaxNumber.toFormElementIfVisible(id)
            CardFormElementId.KCP_CARD_PASSWORD -> kcpCardPassword.toFormElementIfVisible(id)
            CardFormElementId.POSTAL_CODE -> postalCode.toFormElementIfVisible(id)

            // Neither of these can hold up a payment: the switch always holds a value, and an installment is only
            // offered when there is something to choose from.
            CardFormElementId.STORE_PAYMENT_METHOD ->
                if (isStorePaymentFieldVisible) FormElementState(id, isValid = true) else null

            CardFormElementId.INSTALLMENTS ->
                if (installmentState.installmentOptions.isNotEmpty()) FormElementState(id, isValid = true) else null
        }
    }

    companion object {

        /**
         * The order the card form is laid out in, ignoring visibility. Change the order here and nowhere else.
         *
         * Only [create] needs it. Tests reach it so the sequence can be asserted directly, rather than inferred from
         * a state built to make every field visible.
         */
        @VisibleForTesting
        internal val CANONICAL_ORDER = listOf(
            CardFormElementId.CARD_NUMBER,
            CardFormElementId.EXPIRY_DATE,
            CardFormElementId.SECURITY_CODE,
            CardFormElementId.HOLDER_NAME,
            CardFormElementId.SOCIAL_SECURITY_NUMBER,
            CardFormElementId.KCP_BIRTH_DATE_OR_TAX_NUMBER,
            CardFormElementId.KCP_CARD_PASSWORD,
            CardFormElementId.POSTAL_CODE,
            CardFormElementId.STORE_PAYMENT_METHOD,
            CardFormElementId.INSTALLMENTS,
        )
    }
}

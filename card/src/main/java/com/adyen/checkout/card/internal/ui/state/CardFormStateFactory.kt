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
 * Derives the card form from a card state: which elements the shopper can see, in which order, and what the form's own
 * rules need to know about each of them.
 *
 * Both of those facts are read from the state rather than kept alongside it, so that neither can drift from what the
 * view state producer puts on screen.
 */
internal class CardFormStateFactory(
    private val state: CardComponentState,
) {

    fun create(): FormState<CardFormElementId> = FormState(
        elements = CANONICAL_ORDER.mapNotNull { id -> elementFor(id) },
    )

    /**
     * Every id is listed rather than falling back to an else, so that adding one to [CardFormElementId] does not
     * compile until its visibility and validity are decided. Getting that wrong means a field silently missing from
     * the screen, or one that never holds up a payment.
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
         * The order the card form is laid out in, ignoring which fields are currently visible. This is the one place
         * the sequence is decided, so it is also where a future experiment would reorder it.
         *
         * Only [create] needs it. It is reachable from tests so that the sequence itself can be asserted, rather than
         * inferred from a state built to make every field visible.
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

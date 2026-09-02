/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import androidx.compose.runtime.Immutable
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState

/**
 * One row of the card form, carrying everything needed to render it. A field that is not shown has no element, and an
 * element always has its data, so there is nothing to look up elsewhere and nothing that can be missing.
 *
 * [CardNumberFormat] sits on two elements rather than beside the list, so that neither depends on anything outside
 * itself. Both derive it from the same brand state, so they cannot disagree.
 */
// TODO - Form fields: the elements below that carry nothing but a text input have the same shape in every component.
// Weigh a shared core element type against the exhaustive `when` it would cost each component.
internal sealed interface CardFormElement {

    /** The composition key, and what tells the state layer which field a focus event came from. */
    val id: CardFormElementId

    data class CardNumber(
        val textInputViewState: TextInputViewState,
        val cardBrandViewState: CardBrandViewState,
        val cardNumberFormat: CardNumberFormat,
        val supportedCardBrandsViewState: SupportedCardBrandsViewState,
    ) : CardFormElement {
        override val id get() = CardFormElementId.CARD_NUMBER
    }

    data class ExpiryDate(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFormElementId.EXPIRY_DATE
    }

    data class SecurityCode(
        val textInputViewState: TextInputViewState,
        val cardNumberFormat: CardNumberFormat,
    ) : CardFormElement {
        override val id get() = CardFormElementId.SECURITY_CODE
    }

    data class HolderName(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFormElementId.HOLDER_NAME
    }

    data class SocialSecurityNumber(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFormElementId.SOCIAL_SECURITY_NUMBER
    }

    data class KcpBirthDateOrTaxNumber(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFormElementId.KCP_BIRTH_DATE_OR_TAX_NUMBER
    }

    data class KcpCardPassword(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFormElementId.KCP_CARD_PASSWORD
    }

    data class PostalCode(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFormElementId.POSTAL_CODE
    }

    data class StorePaymentMethod(
        val isSelected: Boolean,
    ) : CardFormElement {
        override val id get() = CardFormElementId.STORE_PAYMENT_METHOD
    }

    /**
     * The installments row on the card form, which shows the current choice and opens the installments screen. The
     * options themselves belong to that screen, not to this row, so they live in [InstallmentPickerViewState].
     */
    @Immutable
    data class Installments(
        val selectedInstallment: InstallmentModel?,
    ) : CardFormElement {
        override val id get() = CardFormElementId.INSTALLMENTS
    }
}

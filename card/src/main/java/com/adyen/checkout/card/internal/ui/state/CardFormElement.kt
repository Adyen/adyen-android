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
 * One row of the card form, carrying everything needed to render it.
 *
 * What the shopper sees is what is in [CardViewState.elements], in that order. There is nothing to look up elsewhere,
 * and nothing that can be missing: a field that is not shown has no element, and an element always has its data.
 *
 * [CardNumberFormat] appears on two elements rather than being passed alongside the list. The card number and the
 * security code both need it, the view state producer derives it once, and this way neither element depends on anything
 * outside itself.
 */
// TODO - POC: the five elements below that carry only a text input are the same shape in every component. Extract a
// shared simple text input element to core once the other components have their own lists to compare against.
internal sealed interface CardFormElement {

    /**
     * Identifies the field this element renders. Used as the composition key, and to tell the state layer which field
     * gained focus or acted on a focus request.
     */
    val id: CardFieldId

    data class CardNumber(
        val textInputViewState: TextInputViewState,
        val cardBrandViewState: CardBrandViewState,
        val cardNumberFormat: CardNumberFormat,
        val supportedCardBrandsViewState: SupportedCardBrandsViewState,
    ) : CardFormElement {
        override val id get() = CardFieldId.CARD_NUMBER
    }

    data class ExpiryDate(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFieldId.EXPIRY_DATE
    }

    data class SecurityCode(
        val textInputViewState: TextInputViewState,
        val cardNumberFormat: CardNumberFormat,
    ) : CardFormElement {
        override val id get() = CardFieldId.SECURITY_CODE
    }

    data class HolderName(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFieldId.HOLDER_NAME
    }

    data class SocialSecurityNumber(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFieldId.SOCIAL_SECURITY_NUMBER
    }

    data class KcpBirthDateOrTaxNumber(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFieldId.KCP_BIRTH_DATE_OR_TAX_NUMBER
    }

    data class KcpCardPassword(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFieldId.KCP_CARD_PASSWORD
    }

    data class PostalCode(
        val textInputViewState: TextInputViewState,
    ) : CardFormElement {
        override val id get() = CardFieldId.POSTAL_CODE
    }

    data class StorePaymentMethod(
        val isSelected: Boolean,
    ) : CardFormElement {
        override val id get() = CardFieldId.STORE_PAYMENT_METHOD
    }

    /**
     * The installments row on the card form, which shows the current choice and opens the installments screen. The
     * options themselves belong to that screen, not to this row, so they live in [InstallmentPickerViewState].
     */
    @Immutable
    data class Installments(
        val selectedInstallment: InstallmentModel?,
    ) : CardFormElement {
        override val id get() = CardFieldId.INSTALLMENTS
    }
}

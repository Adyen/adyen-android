/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 10/3/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.state.StateUpdater
import com.adyen.checkout.components.core.internal.ui.model.state.StateUpdaterRegistry
import com.adyen.checkout.core.ui.model.ExpiryDate

internal class CardStateUpdaterRegistry : StateUpdaterRegistry<CardFieldId, CardDelegateState> {

    private val updaters = CardFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            CardFieldId.CARD_NUMBER -> CardNumberUpdater()
            CardFieldId.SELECTED_CARD_INDEX -> SelectedCardIndexUpdater()
            CardFieldId.CARD_SECURITY_CODE -> CardSecurityCodeUpdater()
            CardFieldId.CARD_EXPIRY_DATE -> CardExpiryDateUpdater()
            CardFieldId.CARD_HOLDER_NAME -> CardHolderNameUpdater()
            CardFieldId.SOCIAL_SECURITY_NUMBER -> SocialSecurityNumberUpdater()
            CardFieldId.KCP_BIRTH_DATE_OR_TAX_NUMBER -> KcpBirthDateOrTaxNumberUpdater()
            CardFieldId.KCP_CARD_PASSWORD -> KcpCardPasswordUpdater()
            CardFieldId.ADDRESS_POSTAL_CODE -> AddressPostalCodeUpdater()
            CardFieldId.STORED_PAYMENT_METHOD_SWITCH -> StorePaymentMethodSwitchUpdater()
            CardFieldId.INSTALLMENT_OPTION -> InstallmentOptionUpdater()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getFieldState(
        key: CardFieldId,
        state: CardDelegateState
    ): ComponentFieldDelegateState<T> {
        val updater =
            updaters[key] as? StateUpdater<CardDelegateState, ComponentFieldDelegateState<T>>
                ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return updater.getFieldState(state)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> updateFieldState(
        key: CardFieldId,
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<T>
    ): CardDelegateState {
        val updater =
            updaters[key] as? StateUpdater<CardDelegateState, ComponentFieldDelegateState<T>>
                ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return updater.updateFieldState(state, fieldState)
    }
}

internal class CardNumberUpdater :
    StateUpdater<CardDelegateState, ComponentFieldDelegateState<String>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<String> =
        state.cardNumberDelegateState


    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<String>
    ) = state.copy(
        cardNumberDelegateState = fieldState,
    )
}

internal class SelectedCardIndexUpdater :
    StateUpdater<CardDelegateState, ComponentFieldDelegateState<Int>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<Int> =
        state.selectedCardIndexDelegateState

    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<Int>
    ) = state.copy(
        selectedCardIndexDelegateState = fieldState,
    )
}

internal class CardSecurityCodeUpdater :
    StateUpdater<CardDelegateState, ComponentFieldDelegateState<String>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<String> =
        state.cardSecurityCodeDelegateState

    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<String>
    ) = state.copy(
        cardSecurityCodeDelegateState = fieldState,
    )
}

internal class CardExpiryDateUpdater :
    StateUpdater<CardDelegateState, ComponentFieldDelegateState<ExpiryDate>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<ExpiryDate> =
        state.cardExpiryDateDelegateState

    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<ExpiryDate>
    ) = state.copy(
        cardExpiryDateDelegateState = fieldState,
    )
}

internal class CardHolderNameUpdater :
    StateUpdater<CardDelegateState, ComponentFieldDelegateState<String>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<String> =
        state.cardHolderNameDelegateState

    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<String>
    ) = state.copy(
        cardHolderNameDelegateState = fieldState,
    )
}

internal class SocialSecurityNumberUpdater :
    StateUpdater<CardDelegateState, ComponentFieldDelegateState<String>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<String> =
        state.socialSecurityNumberDelegateState

    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<String>
    ) = state.copy(
        socialSecurityNumberDelegateState = fieldState,
    )
}

internal class KcpBirthDateOrTaxNumberUpdater :
    StateUpdater<CardDelegateState, ComponentFieldDelegateState<String>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<String> =
        state.kcpBirthDateOrTaxNumberDelegateState

    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<String>
    ) = state.copy(
        kcpBirthDateOrTaxNumberDelegateState = fieldState,
    )
}

internal class KcpCardPasswordUpdater :
    StateUpdater<CardDelegateState, ComponentFieldDelegateState<String>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<String> =
        state.kcpCardPasswordDelegateState

    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<String>
    ) = state.copy(
        kcpCardPasswordDelegateState = fieldState,
    )
}

internal class AddressPostalCodeUpdater :
    StateUpdater<CardDelegateState, ComponentFieldDelegateState<String>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<String> =
        state.addressPostalCodeDelegateState

    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<String>
    ) = state.copy(
        addressPostalCodeDelegateState = fieldState,
    )
}

internal class StorePaymentMethodSwitchUpdater :
    StateUpdater<CardDelegateState, ComponentFieldDelegateState<Boolean>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<Boolean> =
        state.storedPaymentMethodSwitchDelegateState

    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<Boolean>
    ) = state.copy(
        storedPaymentMethodSwitchDelegateState = fieldState,
    )
}

internal class InstallmentOptionUpdater :
    StateUpdater<CardDelegateState, ComponentFieldDelegateState<InstallmentModel?>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<InstallmentModel?> =
        state.installmentOptionDelegateState

    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<InstallmentModel?>
    ) = state.copy(
        installmentOptionDelegateState = fieldState,
    )
}

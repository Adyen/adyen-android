/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.helper.isHiddenCardType
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.form.keyboardActionFor
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TrailingIcon
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState
import com.adyen.checkout.ui.internal.element.input.FocusRequestToken

internal class CardViewStateProducer(
    private val amount: Amount?,
    private val showSubmitButton: Boolean,
) : ViewStateProducer<CardComponentState, CardViewState> {

    override fun produce(state: CardComponentState) = CardViewState(
        // The form decides which fields are shown and in which order, so building one element per member of that order
        // is the only place either question is answered.
        elements = state.form.order.map { id -> state.toElement(id) },
        isLoading = state.isLoading,
        payButtonViewState = if (showSubmitButton) PayButtonViewState(amount, state.isLoading) else null,
        installmentPickerViewState = state.installmentState.toPickerViewState(),
    )

    private fun CardComponentState.toElement(id: CardFieldId): CardFormElement = when (id) {
        CardFieldId.CARD_NUMBER -> CardFormElement.CardNumber(
            textInputViewState = getInputViewState(
                id = id,
                field = cardNumber.copy(description = getCardNumberInputDescription(cardBrandState)),
                customTrailingIcon = getCardNumberTrailingIcon(isCardScanButtonVisible()),
            ),
            cardBrandViewState = getCardBrandViewState(cardBrandState),
            cardNumberFormat = getCardNumberFormat(cardBrandState),
            supportedCardBrandsViewState = getSupportedCardBrandsViewState(this),
        )

        CardFieldId.EXPIRY_DATE -> CardFormElement.ExpiryDate(
            textInputViewState = getInputViewState(id, expiryDate, getExpiryDateTrailingIcon(expiryDate)),
        )

        CardFieldId.SECURITY_CODE -> {
            val cardNumberFormat = getCardNumberFormat(cardBrandState)
            CardFormElement.SecurityCode(
                textInputViewState = getInputViewState(
                    id = id,
                    field = securityCode,
                    customTrailingIcon = getSecurityCodeTrailingIcon(securityCode, cardNumberFormat),
                ),
                cardNumberFormat = cardNumberFormat,
            )
        }

        CardFieldId.HOLDER_NAME -> CardFormElement.HolderName(getInputViewState(id, holderName))

        CardFieldId.SOCIAL_SECURITY_NUMBER ->
            CardFormElement.SocialSecurityNumber(getInputViewState(id, socialSecurityNumber))

        CardFieldId.KCP_BIRTH_DATE_OR_TAX_NUMBER ->
            CardFormElement.KcpBirthDateOrTaxNumber(getInputViewState(id, kcpBirthDateOrTaxNumber))

        CardFieldId.KCP_CARD_PASSWORD -> CardFormElement.KcpCardPassword(getInputViewState(id, kcpCardPassword))

        CardFieldId.POSTAL_CODE -> CardFormElement.PostalCode(getInputViewState(id, postalCode))

        CardFieldId.STORE_PAYMENT_METHOD -> CardFormElement.StorePaymentMethod(isSelected = storePaymentMethod)

        CardFieldId.INSTALLMENTS -> CardFormElement.Installments(
            selectedInstallment = installmentState.selectedInstallment,
        )
    }

    /**
     * The scan button replaces the brand logos while there is nothing to show a brand for, so it is only ever a choice
     * of trailing icon.
     */
    private fun CardComponentState.isCardScanButtonVisible() = isCardScanningAvailable && cardNumber.text.isEmpty()

    private fun getSupportedCardBrandsViewState(state: CardComponentState) = SupportedCardBrandsViewState(
        supportedCardBrands = state.supportedCardBrands.filterNot { isHiddenCardType(it.txVariant) },
        // Every supported brand is only worth showing while the setting is on and no brand has been detected for the
        // number the shopper is typing.
        isVisible = state.showSupportedCardBrandLogos && when (state.cardBrandState) {
            is CardBrandState.NoBrandsDetected,
            is CardBrandState.UnsupportedBrand,
            is CardBrandState.HiddenBrand -> true

            is CardBrandState.SingleUnreliableBrand,
            is CardBrandState.SingleReliableBrand,
            is CardBrandState.SingleReliableWithHiddenBrand,
            is CardBrandState.DualBrand,
            is CardBrandState.DualBrandWithShopperSelection -> false
        },
    )

    /**
     * Builds the view state of one text input, adding the two things only the form as a whole can answer: which action
     * key the field shows, and whether it is the one being asked to take focus.
     */
    private fun CardComponentState.getInputViewState(
        id: CardFieldId,
        field: TextInputComponentState,
        customTrailingIcon: TrailingIcon? = null,
    ): TextInputViewState = field.toViewState(
        customTrailingIcon = customTrailingIcon,
        keyboardAction = form.keyboardActionFor(id),
        focusRequest = form.focusRequest?.takeIf { it.id == id }?.let { FocusRequestToken(it) },
    )

    private fun getCardNumberInputDescription(cardBrandState: CardBrandState): CheckoutLocalizationKey? {
        if (cardBrandState is CardBrandState.DualBrandWithShopperSelection) {
            return CheckoutLocalizationKey.CARD_DUAL_BRAND_SELECTOR_DESCRIPTION
        }

        return null
    }

    private fun getCardBrandViewState(cardBrandState: CardBrandState): CardBrandViewState {
        return when (cardBrandState) {
            is CardBrandState.NoBrandsDetected,
            is CardBrandState.UnsupportedBrand,
            is CardBrandState.HiddenBrand -> CardBrandViewState.Placeholder

            is CardBrandState.SingleUnreliableBrand ->
                CardBrandViewState.SingleBrand(cardBrandState.cardBrandData.cardBrand)

            is CardBrandState.SingleReliableBrand ->
                CardBrandViewState.SingleBrand(cardBrandState.cardBrandData.cardBrand)

            is CardBrandState.SingleReliableWithHiddenBrand ->
                CardBrandViewState.SingleBrand(cardBrandState.cardBrandData.cardBrand)

            is CardBrandState.DualBrand -> CardBrandViewState.DualBrand(
                cardBrandState.cardBrandDataList.map { it.cardBrand },
            )

            is CardBrandState.DualBrandWithShopperSelection -> CardBrandViewState.SelectableDualBrand(
                cardBrandState.cardBrandDataList.map { cardBrandData ->
                    SelectableCardBrandItem(
                        brand = cardBrandData.cardBrand,
                        isSelected = cardBrandData == cardBrandState.shopperSelectedCardBrandData,
                    )
                },
            )
        }
    }

    private fun getCardNumberFormat(cardBrandState: CardBrandState): CardNumberFormat {
        val cardBrandData = when (cardBrandState) {
            is CardBrandState.NoBrandsDetected,
            is CardBrandState.UnsupportedBrand,
            is CardBrandState.HiddenBrand -> null

            is CardBrandState.SingleUnreliableBrand -> cardBrandState.cardBrandData
            is CardBrandState.SingleReliableBrand -> cardBrandState.cardBrandData
            is CardBrandState.SingleReliableWithHiddenBrand -> cardBrandState.cardBrandData
            is CardBrandState.DualBrand -> cardBrandState.cardBrandDataList.firstOrNull()
            is CardBrandState.DualBrandWithShopperSelection -> cardBrandState.shopperSelectedCardBrandData
        }

        return cardBrandData?.cardBrand.toCardNumberFormat()
    }
}

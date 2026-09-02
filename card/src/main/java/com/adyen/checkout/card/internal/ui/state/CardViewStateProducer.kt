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
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState

internal class CardViewStateProducer(
    private val amount: Amount?,
    private val showSubmitButton: Boolean,
) : ViewStateProducer<CardComponentState, CardViewState> {

    override fun produce(state: CardComponentState) = CardViewState(
        elements = state.form.elements.map { state.toElement(it.id) },
        isLoading = state.isLoading,
        payButtonViewState = if (showSubmitButton) PayButtonViewState(amount, state.isLoading) else null,
        installmentPickerViewState = state.installmentState.toPickerViewState(),
    )

    private fun CardComponentState.toElement(id: CardFormElementId): CardFormElement = when (id) {
        CardFormElementId.CARD_NUMBER -> CardFormElement.CardNumber(
            textInputViewState = cardNumber
                .copy(description = getCardNumberInputDescription(cardBrandState))
                .toViewState(form, focusRequest, id, getCardNumberTrailingIcon(isCardScanButtonVisible())),
            cardBrandViewState = getCardBrandViewState(cardBrandState),
            cardNumberFormat = getCardNumberFormat(cardBrandState),
            supportedCardBrandsViewState = getSupportedCardBrandsViewState(),
        )

        CardFormElementId.EXPIRY_DATE -> CardFormElement.ExpiryDate(
            textInputViewState = expiryDate.toViewState(form, focusRequest, id, getExpiryDateTrailingIcon(expiryDate)),
        )

        CardFormElementId.SECURITY_CODE -> {
            val cardNumberFormat = getCardNumberFormat(cardBrandState)
            CardFormElement.SecurityCode(
                textInputViewState = securityCode
                    .toViewState(form, focusRequest, id, getSecurityCodeTrailingIcon(securityCode, cardNumberFormat)),
                cardNumberFormat = cardNumberFormat,
            )
        }

        CardFormElementId.HOLDER_NAME -> CardFormElement.HolderName(holderName.toViewState(form, focusRequest, id))

        CardFormElementId.SOCIAL_SECURITY_NUMBER ->
            CardFormElement.SocialSecurityNumber(socialSecurityNumber.toViewState(form, focusRequest, id))

        CardFormElementId.KCP_BIRTH_DATE_OR_TAX_NUMBER ->
            CardFormElement.KcpBirthDateOrTaxNumber(kcpBirthDateOrTaxNumber.toViewState(form, focusRequest, id))

        CardFormElementId.KCP_CARD_PASSWORD -> CardFormElement.KcpCardPassword(
            kcpCardPassword.toViewState(form, focusRequest, id),
        )

        CardFormElementId.POSTAL_CODE -> CardFormElement.PostalCode(postalCode.toViewState(form, focusRequest, id))

        CardFormElementId.STORE_PAYMENT_METHOD -> CardFormElement.StorePaymentMethod(isSelected = storePaymentMethod)

        CardFormElementId.INSTALLMENTS -> CardFormElement.Installments(
            selectedInstallment = installmentState.selectedInstallment,
        )
    }

    /**
     * The scan button replaces the brand logos while there is nothing to show a brand for, so it is only ever a choice
     * of trailing icon.
     */
    private fun CardComponentState.isCardScanButtonVisible() = isCardScanningAvailable && cardNumber.text.isEmpty()

    private fun CardComponentState.getSupportedCardBrandsViewState() = SupportedCardBrandsViewState(
        supportedCardBrands = supportedCardBrands.filterNot { isHiddenCardType(it.txVariant) },
        // Every supported brand is only worth showing while the setting is on and no brand has been detected for the
        // number the shopper is typing.
        isVisible = showSupportedCardBrandLogos && when (cardBrandState) {
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

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.helper.isHiddenCardType
import com.adyen.checkout.card.internal.ui.model.CardNumberTrailingIcon
import com.adyen.checkout.card.internal.ui.model.ExpiryDateTrailingIcon
import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
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

    override fun produce(state: CardComponentState): CardViewState {
        // we only show all supported card brands when the setting is enabled
        // and we do not detect any brands for this specific card
        val isSupportedCardBrandsShown = state.showSupportedCardBrandLogos && when (state.cardBrandState) {
            is CardBrandState.NoBrandsDetected,
            is CardBrandState.UnsupportedBrand,
            is CardBrandState.HiddenBrand -> true

            is CardBrandState.SingleUnreliableBrand,
            is CardBrandState.SingleReliableBrand,
            is CardBrandState.SingleReliableWithHiddenBrand,
            is CardBrandState.DualBrand,
            is CardBrandState.DualBrandWithShopperSelection -> false
        }

        val cardNumberInputDescription = getCardNumberInputDescription(state.cardBrandState)
        val cardBrandViewState = getCardBrandViewState(state.cardBrandState)
        val cardNumberFormat = getCardNumberFormat(state.cardBrandState)
        val isCardScanButtonVisible = state.isCardScanningAvailable && state.cardNumber.text.isEmpty()

        val storePaymentViewState = if (state.isStorePaymentFieldVisible) {
            StorePaymentViewState(isSelected = state.storePaymentMethod)
        } else {
            null
        }

        return CardViewState(
            fieldOrder = state.form.order,
            cardNumber = state.fieldViewState(
                id = CardFieldId.CARD_NUMBER,
                field = state.cardNumber.copy(description = cardNumberInputDescription),
                customTrailingIcon = getCardNumberTrailingIcon(isCardScanButtonVisible),
            ),
            expiryDate = state.fieldViewState(
                CardFieldId.EXPIRY_DATE,
                state.expiryDate,
                getExpiryDateTrailingIcon(state.expiryDate),
            ),
            securityCode = state.fieldViewState(
                CardFieldId.SECURITY_CODE,
                state.securityCode,
                getSecurityCodeTrailingIcon(state.securityCode, cardNumberFormat),
            ),
            holderName = state.fieldViewState(CardFieldId.HOLDER_NAME, state.holderName),
            socialSecurityNumber = state.fieldViewState(CardFieldId.SOCIAL_SECURITY_NUMBER, state.socialSecurityNumber),
            kcpBirthDateOrTaxNumber = state.fieldViewState(
                CardFieldId.KCP_BIRTH_DATE_OR_TAX_NUMBER,
                state.kcpBirthDateOrTaxNumber,
            ),
            kcpCardPassword = state.fieldViewState(CardFieldId.KCP_CARD_PASSWORD, state.kcpCardPassword),
            postalCode = state.fieldViewState(CardFieldId.POSTAL_CODE, state.postalCode),
            storePaymentViewState = storePaymentViewState,
            supportedCardBrandsViewState = getSupportedCardBrandsViewState(state, isSupportedCardBrandsShown),
            cardBrandViewState = cardBrandViewState,
            cardNumberFormat = cardNumberFormat,
            isLoading = state.isLoading,
            isCardScanButtonVisible = isCardScanButtonVisible,
            installmentViewState = state.installmentState.toViewState(),
            payButtonViewState = if (showSubmitButton) PayButtonViewState(amount, state.isLoading) else null,
        )
    }

    private fun getSupportedCardBrandsViewState(
        state: CardComponentState,
        isVisible: Boolean,
    ) = SupportedCardBrandsViewState(
        supportedCardBrands = state.supportedCardBrands.filterNot { isHiddenCardType(it.txVariant) },
        isVisible = isVisible,
    )

    /**
     * Builds the view state of one field, adding the two things only the form as a whole can answer: which action key
     * the field shows, and whether it is the one being asked to take focus.
     */
    private fun CardComponentState.fieldViewState(
        id: CardFieldId,
        field: TextInputComponentState,
        customTrailingIcon: TrailingIcon? = null,
    ): TextInputViewState? = field.toViewState(
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

    private fun getCardNumberTrailingIcon(isCardScanButtonVisible: Boolean): CardNumberTrailingIcon {
        return if (isCardScanButtonVisible) {
            CardNumberTrailingIcon.ScanButton
        } else {
            CardNumberTrailingIcon.BrandLogos
        }
    }

    private fun getExpiryDateTrailingIcon(
        expiryDate: TextInputComponentState,
    ): ExpiryDateTrailingIcon {
        return if (expiryDate.isValid && expiryDate.text.isNotEmpty()) {
            ExpiryDateTrailingIcon.Checkmark
        } else {
            ExpiryDateTrailingIcon.Placeholder
        }
    }

    private fun getSecurityCodeTrailingIcon(
        securityCode: TextInputComponentState,
        cardNumberFormat: CardNumberFormat,
    ): SecurityCodeTrailingIcon {
        return when {
            securityCode.isValid && securityCode.text.isNotEmpty() -> SecurityCodeTrailingIcon.Checkmark
            cardNumberFormat == CardNumberFormat.AMEX -> SecurityCodeTrailingIcon.PlaceholderAmex
            else -> SecurityCodeTrailingIcon.PlaceholderDefault
        }
    }
}

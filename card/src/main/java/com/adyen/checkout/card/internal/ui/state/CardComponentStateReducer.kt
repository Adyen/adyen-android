/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.helper.ExpiryDateParser
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.firstInvalid
import com.adyen.checkout.core.components.internal.ui.state.form.nextTextInputAfter
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class CardComponentStateReducer(
    private val cardBrandIntentsHandler: CardBrandIntentsHandler,
) : ComponentStateReducer<CardComponentState, CardIntent> {

    @Suppress("CyclomaticComplexMethod")
    override fun reduce(state: CardComponentState, intent: CardIntent): CardComponentState {
        return when (intent) {
            is CardIntent.UpdateCardNumber -> state.copy(
                cardNumber = state.cardNumber.updateText(intent.number),
            )

            is CardIntent.UpdateExpiryDate -> state.copy(
                expiryDate = state.expiryDate.updateText(intent.expiryDate),
            )

            is CardIntent.UpdateSecurityCode -> state.copy(
                securityCode = state.securityCode.updateText(intent.securityCode),
            )

            is CardIntent.UpdateHolderName -> state.copy(
                holderName = state.holderName.updateText(intent.holderName),
            )

            is CardIntent.UpdateSocialSecurityNumber -> state.copy(
                socialSecurityNumber = state.socialSecurityNumber.updateText(intent.socialSecurityNumber),
            )

            is CardIntent.UpdateKcpBirthDateOrTaxNumber -> state.copy(
                kcpBirthDateOrTaxNumber = state.kcpBirthDateOrTaxNumber.updateText(intent.kcpBirthDateOrTaxNumber),
            )

            is CardIntent.UpdateKcpCardPassword -> state.copy(
                kcpCardPassword = state.kcpCardPassword.updateText(intent.kcpCardPassword),
            )

            is CardIntent.UpdatePostalCode -> state.copy(
                postalCode = state.postalCode.updateText(intent.postalCode)
            )

            is CardIntent.UpdateFieldFocus -> state.updateFieldFocus(intent.id, intent.hasFocus)

            is CardIntent.UpdateStorePaymentMethod -> state.copy(
                storePaymentMethod = intent.isChecked,
            )

            is CardIntent.SelectBrand -> {
                cardBrandIntentsHandler.onBrandSelected(state, intent)
            }

            is CardIntent.UpdateDetectedCardTypes -> {
                cardBrandIntentsHandler.onUpdateDetectedCardTypes(state, intent)
            }

            is CardIntent.UpdateInstallment -> state.copy(
                installmentState = state.installmentState.copy(selectedInstallment = intent.installment),
            )

            is CardIntent.UpdateLoading -> state.copy(
                isLoading = intent.isLoading,
            )

            is CardIntent.UpdateCardScanningAvailability -> state.copy(
                isCardScanningAvailable = intent.isAvailable,
            )

            is CardIntent.UpdateCardScanResult -> state.copy(
                cardNumber = state.cardNumber.updateText(intent.pan.orEmpty()),
                expiryDate = state.expiryDate.updateText(
                    ExpiryDateParser.formatToMMyy(intent.expiryMonth, intent.expiryYear),
                ),
            ).requestFocusAfterScan(intent)

            is CardIntent.HighlightValidationErrors -> highlightValidationErrors(state)
        }
    }

    /**
     * Moves the shopper to the first field the scan did not fill. A scan can come back with only part of a card, or
     * with nothing usable at all, and a scan that returns no card number also clears the one that was there, so the
     * shopper has to go back rather than forward.
     *
     * This goes by what the scanner returned rather than by what is still invalid, because the reducer runs before the
     * validator: at this point every error still describes the text from before the scan.
     */
    private fun CardComponentState.requestFocusAfterScan(
        intent: CardIntent.UpdateCardScanResult,
    ): CardComponentState {
        val focusTarget = when {
            intent.pan.isNullOrBlank() -> CardFieldId.CARD_NUMBER
            intent.expiryMonth == null || intent.expiryYear == null -> form.nextTextInputAfter(CardFieldId.CARD_NUMBER)
            else -> form.nextTextInputAfter(CardFieldId.EXPIRY_DATE)
        }
        return copy(focusRequest = focusTarget?.let { FocusRequest(id = it) })
    }

    /**
     * A focus gain normally means the shopper tapped the field, and a field the shopper is working on should not be
     * showing an error. The exception is focus we asked for after the shopper pressed pay, which exists precisely to
     * point at an error and so must not clear it.
     *
     * Losing focus is the same event whatever caused it, and always shows an error the field is holding back.
     */
    // TODO - Form fields rollout: will move to core, so that every component shares these rules instead of copying
    // them. Nothing here is about cards. Only updateTextField stays behind, because just the card state knows which
    // property each id points at.
    private fun CardComponentState.updateFieldFocus(id: CardFieldId, hasFocus: Boolean): CardComponentState {
        val request = focusRequest?.takeIf { it.id == id }
        val updated = updateTextField(id) { field ->
            // TODO - Form fields cleanup: will be removed. FormState.focusRequest does this job instead, but the UI
            // does not read it yet, so this flag is still what moves focus on screen and has to be kept up to date.
            val focused = field.copy(isFocused = hasFocus)
            when {
                !hasFocus -> focused.showErrorIfPresent()
                request?.keepErrorHighlight == true -> focused
                else -> focused.hideErrorIfPresent()
            }
        }

        // The request has been answered, so it must not outlive the focus change it asked for and make the shopper's
        // next tap on the same field look programmatic.
        return if (hasFocus && request != null) updated.copy(focusRequest = null) else updated
    }

    /**
     * The shopper pressed pay on a form that cannot be submitted: show every error at once, and send focus to the
     * first field that is wrong in the order the shopper reads them.
     */
    // TODO - Form fields rollout: will move to core together with updateFieldFocus. Nothing here is about cards
    // either. Only isFieldValid stays behind.
    private fun highlightValidationErrors(state: CardComponentState): CardComponentState {
        val firstInvalid = state.form.firstInvalid { state.isFieldValid(it) }

        val highlighted = CardFieldId.entries.fold(state) { current, id ->
            current.updateTextField(id) { field ->
                // TODO - Form fields cleanup: will be removed. The focusRequest below does this job instead. Setting
                // the flag on the chosen field and clearing it on the rest is what moves focus until the UI reads it.
                field.showErrorIfPresent().copy(isFocused = id == firstInvalid)
            }
        }

        return highlighted.copy(
            focusRequest = firstInvalid?.let { FocusRequest(id = it, keepErrorHighlight = true) },
        )
    }

    /**
     * Applies [transform] to the text input [id] identifies. Fields that are not text inputs have nothing to
     * transform, so they are left alone.
     */
    private fun CardComponentState.updateTextField(
        id: CardFieldId,
        transform: (TextInputComponentState) -> TextInputComponentState,
    ): CardComponentState = when (id) {
        CardFieldId.CARD_NUMBER -> copy(cardNumber = transform(cardNumber))
        CardFieldId.EXPIRY_DATE -> copy(expiryDate = transform(expiryDate))
        CardFieldId.SECURITY_CODE -> copy(securityCode = transform(securityCode))
        CardFieldId.HOLDER_NAME -> copy(holderName = transform(holderName))
        CardFieldId.SOCIAL_SECURITY_NUMBER -> copy(socialSecurityNumber = transform(socialSecurityNumber))
        CardFieldId.KCP_BIRTH_DATE_OR_TAX_NUMBER -> copy(kcpBirthDateOrTaxNumber = transform(kcpBirthDateOrTaxNumber))
        CardFieldId.KCP_CARD_PASSWORD -> copy(kcpCardPassword = transform(kcpCardPassword))
        CardFieldId.POSTAL_CODE -> copy(postalCode = transform(postalCode))
        CardFieldId.STORE_PAYMENT_METHOD,
        CardFieldId.INSTALLMENTS -> this
    }

    /**
     * Whether the field [id] identifies is currently valid. Nothing that is not a text input can be invalid today, so
     * those never hold up a submission.
     */
    private fun CardComponentState.isFieldValid(id: CardFieldId): Boolean = when (id) {
        CardFieldId.CARD_NUMBER -> cardNumber.isValid
        CardFieldId.EXPIRY_DATE -> expiryDate.isValid
        CardFieldId.SECURITY_CODE -> securityCode.isValid
        CardFieldId.HOLDER_NAME -> holderName.isValid
        CardFieldId.SOCIAL_SECURITY_NUMBER -> socialSecurityNumber.isValid
        CardFieldId.KCP_BIRTH_DATE_OR_TAX_NUMBER -> kcpBirthDateOrTaxNumber.isValid
        CardFieldId.KCP_CARD_PASSWORD -> kcpCardPassword.isValid
        CardFieldId.POSTAL_CODE -> postalCode.isValid
        CardFieldId.STORE_PAYMENT_METHOD,
        CardFieldId.INSTALLMENTS -> true
    }
}

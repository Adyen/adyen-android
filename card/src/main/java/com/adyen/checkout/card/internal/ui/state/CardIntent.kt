/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.DetectedCardTypeList
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateIntent

internal sealed interface CardIntent : ComponentStateIntent {

    // User input intents
    data class UpdateCardNumber(val number: String) : CardIntent

    data class UpdateExpiryDate(val expiryDate: String) : CardIntent

    data class UpdateSecurityCode(val securityCode: String) : CardIntent

    data class UpdateHolderName(val holderName: String) : CardIntent

    data class UpdateSocialSecurityNumber(val socialSecurityNumber: String) : CardIntent

    data class UpdateKcpBirthDateOrTaxNumber(val kcpBirthDateOrTaxNumber: String) : CardIntent

    data class UpdateKcpCardPassword(val kcpCardPassword: String) : CardIntent

    data class UpdatePostalCode(val postalCode: String) : CardIntent

    /**
     * A field has gained or lost focus. Unlike a value change, which carries the meaning of the field it belongs to,
     * focus is the same event whichever field reports it, so one intent covers all of them.
     */
    data class UpdateFieldFocus(val id: CardFieldId, val hasFocus: Boolean) : CardIntent

    data class UpdateStorePaymentMethod(val isChecked: Boolean) : CardIntent

    data class SelectBrand(val cardBrand: CardBrand) : CardIntent

    data class UpdateInstallment(val installment: InstallmentModel) : CardIntent

    // System intents
    data class UpdateDetectedCardTypes(val detectedCardTypeList: DetectedCardTypeList) : CardIntent

    data class UpdateLoading(val isLoading: Boolean) : CardIntent

    data class UpdateCardScanningAvailability(val isAvailable: Boolean) : CardIntent

    data class UpdateCardScanResult(val pan: String?, val expiryMonth: Int?, val expiryYear: Int?) : CardIntent

    data object HighlightValidationErrors : CardIntent
}

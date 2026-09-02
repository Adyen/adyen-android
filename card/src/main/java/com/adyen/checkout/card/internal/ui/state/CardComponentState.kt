/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal data class CardComponentState(
    // UI input fields
    val cardNumber: TextInputComponentState,
    val expiryDate: TextInputComponentState,
    val securityCode: TextInputComponentState,
    val holderName: TextInputComponentState,
    val socialSecurityNumber: TextInputComponentState,
    val kcpBirthDateOrTaxNumber: TextInputComponentState,
    val kcpCardPassword: TextInputComponentState,
    val postalCode: TextInputComponentState,

    // UI configuration
    val storePaymentMethod: Boolean,
    val isStorePaymentFieldVisible: Boolean,
    val supportedCardBrands: List<CardBrand>,
    val showSupportedCardBrandLogos: Boolean,
    val isLoading: Boolean,
    val isCardScanningAvailable: Boolean,

    // Component state
    val cardBrandState: CardBrandState,
    val networkBinLookupState: NetworkBinLookupState?,
    val installmentState: InstallmentState,

    val focusRequest: FocusRequest<CardFormElementId>? = null,
) : ComponentState {

    /**
     * Which fields are on screen and in which order, plus any pending focus move. The order is derived from the fields
     * above rather than stored, so that it cannot disagree with them, and computed once per state because both the
     * reducer and the view state producer read it several times.
     *
     * [LazyThreadSafetyMode.PUBLICATION] because the merchant owns the coroutine scope this component runs in, so the
     * same state can be read from more than one thread. Deriving the same value twice is harmless; publishing it
     * unsafely would not be.
     */
    val form: FormState<CardFormElementId> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        CardFormStateFactory(this).create()
    }
}

/**
 * Applies [transform] to the text input [id] names. Elements that are not text inputs have nothing to transform, so
 * they leave the state unchanged.
 *
 * It lives next to the state it updates rather than in the reducer, so that adding a field above does not compile until
 * it is mapped here. `CardComponentStateTest` asserts that no two ids write the same property.
 */
internal fun CardComponentState.updateTextInput(
    id: CardFormElementId,
    transform: (TextInputComponentState) -> TextInputComponentState,
): CardComponentState = when (id) {
    CardFormElementId.CARD_NUMBER -> copy(cardNumber = transform(cardNumber))
    CardFormElementId.EXPIRY_DATE -> copy(expiryDate = transform(expiryDate))
    CardFormElementId.SECURITY_CODE -> copy(securityCode = transform(securityCode))
    CardFormElementId.HOLDER_NAME -> copy(holderName = transform(holderName))
    CardFormElementId.SOCIAL_SECURITY_NUMBER -> copy(socialSecurityNumber = transform(socialSecurityNumber))
    CardFormElementId.KCP_BIRTH_DATE_OR_TAX_NUMBER -> copy(kcpBirthDateOrTaxNumber = transform(kcpBirthDateOrTaxNumber))
    CardFormElementId.KCP_CARD_PASSWORD -> copy(kcpCardPassword = transform(kcpCardPassword))
    CardFormElementId.POSTAL_CODE -> copy(postalCode = transform(postalCode))
    CardFormElementId.STORE_PAYMENT_METHOD,
    CardFormElementId.INSTALLMENTS -> this
}

internal sealed class CardBrandState {
    // No brands
    data object NoBrandsDetected : CardBrandState()
    data object UnsupportedBrand : CardBrandState()
    data object HiddenBrand : CardBrandState()

    // Single brand
    data class SingleUnreliableBrand(val cardBrandData: CardBrandData) : CardBrandState()
    data class SingleReliableBrand(val cardBrandData: CardBrandData) : CardBrandState()
    data class SingleReliableWithHiddenBrand(val cardBrandData: CardBrandData) : CardBrandState()

    // Dual brand
    data class DualBrand(val cardBrandDataList: List<CardBrandData>) : CardBrandState()
    data class DualBrandWithShopperSelection(
        val cardBrandDataList: List<CardBrandData>,
        val shopperSelectedCardBrandData: CardBrandData,
    ) : CardBrandState()
}

internal data class CardBrandData(
    val cardBrand: CardBrand,
    val enableLuhnCheck: Boolean,
    val cvcPolicy: Brand.FieldPolicy,
    val expiryDatePolicy: Brand.FieldPolicy,
    val panLength: Int?,
    val paymentMethodVariant: String?,
    val localizedBrand: String?
)

internal data class InstallmentState(
    val installmentOptions: List<InstallmentModel>,
    val selectedInstallment: InstallmentModel?,
)

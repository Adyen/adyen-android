/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.ViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState

internal data class CardViewState(
    val cardNumber: TextInputViewState?,
    val expiryDate: TextInputViewState?,
    val securityCode: TextInputViewState?,
    val holderName: TextInputViewState?,
    val socialSecurityNumber: TextInputViewState?,
    val kcpBirthDateOrTaxNumber: TextInputViewState?,
    val kcpCardPassword: TextInputViewState?,
    val postalCode: TextInputViewState?,
    val storePaymentViewState: StorePaymentViewState?,
    val supportedCardBrandsViewState: SupportedCardBrandsViewState,
    val cardBrandViewState: CardBrandViewState,
    val cardNumberFormat: CardNumberFormat,
    val isLoading: Boolean,
    val isCardScanButtonVisible: Boolean,
    val installmentViewState: InstallmentViewState?,
    val amount: Amount?,
) : ViewState

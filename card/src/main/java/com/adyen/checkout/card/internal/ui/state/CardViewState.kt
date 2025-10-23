/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.ViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState

internal data class CardViewState(
    val cardNumber: TextInputState,
    val expiryDate: TextInputState,
    val supportedCardBrands: List<CardBrand>,
    val isSupportedCardBrandsShown: Boolean,
    val detectedBrand: CardBrand?,
    // TODO - Card. isAmex flag is added for simplicity, to be used in formatting.
    //  should be removed once detected card types are available.
    val isAmex: Boolean,
    val isLoading: Boolean,
) : ViewState

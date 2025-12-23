/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.components.internal.ui.state.ViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState

internal data class StoredCardViewState(
    val securityCode: TextInputViewState,
    val brand: CardBrand?,
    val isLoading: Boolean,
) : ViewState

internal val StoredCardViewState.isAmex: Boolean
    get() = brand?.txVariant == CardType.AMERICAN_EXPRESS.txVariant

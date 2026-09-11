/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import androidx.compose.runtime.Immutable
import com.adyen.checkout.core.components.internal.ui.state.ViewState
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState

/**
 * @param elements The stored card form, in the order the shopper sees it. Empty for a stored card that asks for no
 * security code, in which case the screen shows only the pay button.
 * @param isLoading Whether the component is waiting on a payment, which stops the shopper interacting with the form.
 * @param payButtonViewState The pay button, or null when the merchant provides their own.
 */
@Immutable
internal data class StoredCardViewState(
    val elements: List<StoredCardFormElement>,
    val isLoading: Boolean,
    val payButtonViewState: PayButtonViewState?,
) : ViewState

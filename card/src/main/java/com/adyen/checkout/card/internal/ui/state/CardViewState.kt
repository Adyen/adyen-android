/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import androidx.compose.runtime.Immutable
import com.adyen.checkout.core.components.internal.ui.state.ViewState
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState

/**
 * @param elements The card form, in the order the shopper sees it. Being in the list is what makes a field visible, and
 * each element carries everything needed to render it.
 * @param isLoading Whether the component is waiting on a payment, which stops the shopper interacting with the form.
 * @param payButtonViewState The pay button, or null when the merchant provides their own.
 * @param installmentPickerViewState The installments screen. One property per secondary screen, named rather than found
 * among [elements], because a secondary screen is a separate screen with its own data.
 */
@Immutable
internal data class CardViewState(
    val elements: List<CardFormElement>,
    val isLoading: Boolean,
    val payButtonViewState: PayButtonViewState?,
    val installmentPickerViewState: InstallmentPickerViewState?,
) : ViewState

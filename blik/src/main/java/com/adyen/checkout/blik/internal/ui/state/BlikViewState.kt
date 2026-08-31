/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import androidx.compose.runtime.Immutable
import com.adyen.checkout.core.components.internal.ui.state.ViewState
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState

/**
 * @param elements The Blik form, in the order the shopper sees it. Each element carries everything needed to render it.
 * The helper text above the form is not in here: it is static screen copy, not a field.
 * @param isLoading Whether the component is waiting on a payment, which stops the shopper interacting with the form.
 * @param payButtonViewState The pay button, or null when the merchant provides their own.
 */
@Immutable
internal data class BlikViewState(
    val elements: List<BlikFormElement>,
    val isLoading: Boolean,
    val payButtonViewState: PayButtonViewState?,
) : ViewState

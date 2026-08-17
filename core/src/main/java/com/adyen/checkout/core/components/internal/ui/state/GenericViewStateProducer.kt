/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GenericViewStateProducer(
    private val amount: Amount?,
    private val showSubmitButton: Boolean,
) : ViewStateProducer<GenericComponentState, GenericViewState> {

    override fun produce(state: GenericComponentState): GenericViewState {
        return GenericViewState(
            isLoading = state.isLoading,
            payButtonViewState = if (showSubmitButton) PayButtonViewState(amount, state.isLoading) else null,
        )
    }
}

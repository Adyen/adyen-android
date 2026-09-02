/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState

internal class BlikViewStateProducer(
    private val amount: Amount?,
    private val showSubmitButton: Boolean,
) : ViewStateProducer<BlikComponentState, BlikViewState> {

    override fun produce(state: BlikComponentState) = BlikViewState(
        elements = state.form.elements.map { state.toElement(it.id) },
        isLoading = state.isLoading,
        payButtonViewState = if (showSubmitButton) PayButtonViewState(amount, state.isLoading) else null,
    )

    private fun BlikComponentState.toElement(id: BlikFormElementId): BlikFormElement = when (id) {
        BlikFormElementId.BLIK_CODE -> BlikFormElement.BlikCode(blikCode.toViewState(form, focusRequest, id))
    }
}

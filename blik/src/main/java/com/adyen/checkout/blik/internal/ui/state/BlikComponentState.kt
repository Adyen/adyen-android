/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormState
import com.adyen.checkout.core.components.internal.ui.state.form.toFormElementIfVisible
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal data class BlikComponentState(
    val blikCode: TextInputComponentState,
    val isLoading: Boolean,
    val focusRequest: FocusRequest<BlikFormElementId>? = null,
) : ComponentState {

    /**
     * Which fields are on screen and in which order, plus any pending focus move. The Blik code is always required, so
     * unlike card there is nothing to derive: the form is one field.
     */
    val form: FormState<BlikFormElementId> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormState(elements = listOfNotNull(blikCode.toFormElementIfVisible(BlikFormElementId.BLIK_CODE)))
    }
}

/**
 * Applies [transform] to the text input [id] names.
 *
 * It lives next to the state it updates so that adding a field above does not compile until it is mapped here.
 */
internal fun BlikComponentState.updateTextInput(
    id: BlikFormElementId,
    transform: (TextInputComponentState) -> TextInputComponentState,
): BlikComponentState = when (id) {
    BlikFormElementId.BLIK_CODE -> copy(blikCode = transform(blikCode))
}

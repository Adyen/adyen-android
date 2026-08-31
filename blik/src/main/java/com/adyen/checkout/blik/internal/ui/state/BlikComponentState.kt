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
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal data class BlikComponentState(
    val blikCode: TextInputComponentState,
    val isLoading: Boolean,
    // A focus move the state layer is asking the UI to make. Unlike the field order this is not derivable, since it
    // records something that happened rather than something that is.
    val focusRequest: FocusRequest<BlikFieldId>? = null,
) : ComponentState {

    /**
     * Which fields are on screen and in which order, plus any pending focus move. The Blik code is always required, so
     * unlike card there is nothing to derive: the form is one field.
     */
    val form: FormState<BlikFieldId> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormState(order = listOf(BlikFieldId.BLIK_CODE), focusRequest = focusRequest)
    }
}

/**
 * Applies [transform] to the text input [id] names.
 *
 * It lives next to the state it updates so that adding a field above does not compile until it is mapped here.
 */
internal fun BlikComponentState.updateTextInput(
    id: BlikFieldId,
    transform: (TextInputComponentState) -> TextInputComponentState,
): BlikComponentState = when (id) {
    BlikFieldId.BLIK_CODE -> copy(blikCode = transform(blikCode))
}

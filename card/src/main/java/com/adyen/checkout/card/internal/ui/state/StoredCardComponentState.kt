/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal data class StoredCardComponentState(
    val securityCode: TextInputComponentState,
    val isLoading: Boolean,
    val detectedCardType: DetectedCardType?,
    // A focus move the state layer is asking the UI to make. Unlike the field order this is not derivable, since it
    // records something that happened rather than something that is.
    val focusRequest: FocusRequest<StoredCardFieldId>? = null,
) : ComponentState {

    /**
     * Which fields are on screen and in which order, plus any pending focus move. The order is derived from the field
     * above rather than stored, so that it cannot disagree with it: a stored card that asks for no security code has an
     * empty form, and the screen shows no content at all.
     */
    val form: FormState<StoredCardFieldId> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormState(
            order = listOfNotNull(StoredCardFieldId.SECURITY_CODE.takeIf { securityCode.isVisible }),
            focusRequest = focusRequest,
        )
    }
}

/**
 * Applies [transform] to the text input [id] names.
 *
 * It lives next to the state it updates so that adding a field above does not compile until it is mapped here.
 */
internal fun StoredCardComponentState.updateTextInput(
    id: StoredCardFieldId,
    transform: (TextInputComponentState) -> TextInputComponentState,
): StoredCardComponentState = when (id) {
    StoredCardFieldId.SECURITY_CODE -> copy(securityCode = transform(securityCode))
}

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
import com.adyen.checkout.core.components.internal.ui.state.form.toFormElementIfVisible
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal data class StoredCardComponentState(
    val securityCode: TextInputComponentState,
    val isLoading: Boolean,
    val detectedCardType: DetectedCardType?,
    val focusRequest: FocusRequest<StoredCardFormElementId>? = null,
) : ComponentState {

    /**
     * Which fields are on screen and in which order. Derived from the field above, so it cannot disagree with it: a
     * stored card that asks for no security code has an empty form, and the screen shows no content at all.
     */
    val form: FormState<StoredCardFormElementId> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormState(elements = listOfNotNull(securityCode.toFormElementIfVisible(StoredCardFormElementId.SECURITY_CODE)))
    }
}

/**
 * Applies [transform] to the text input that [id] names.
 *
 * It lives next to the state it updates so that adding a field above does not compile until it is mapped here.
 */
internal fun StoredCardComponentState.updateTextInput(
    id: StoredCardFormElementId,
    transform: (TextInputComponentState) -> TextInputComponentState,
): StoredCardComponentState = when (id) {
    StoredCardFormElementId.SECURITY_CODE -> copy(securityCode = transform(securityCode))
}

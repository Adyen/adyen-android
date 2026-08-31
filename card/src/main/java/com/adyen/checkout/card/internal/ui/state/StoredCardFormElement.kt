/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState

/**
 * One row of the stored card form, carrying everything needed to render it.
 */
internal sealed interface StoredCardFormElement {

    val id: StoredCardFieldId

    data class SecurityCode(
        val textInputViewState: TextInputViewState,
        val cardNumberFormat: CardNumberFormat,
    ) : StoredCardFormElement {
        override val id get() = StoredCardFieldId.SECURITY_CODE
    }
}

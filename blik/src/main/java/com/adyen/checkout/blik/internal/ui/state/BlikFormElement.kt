/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2026.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState

/**
 * One row of the Blik form, carrying everything needed to render it. A sealed interface for one element is more than
 * Blik needs today, and is what every other component uses, so a second element changes nothing.
 */
internal sealed interface BlikFormElement {

    val id: BlikFormElementId

    data class BlikCode(
        val textInputViewState: TextInputViewState,
    ) : BlikFormElement {
        override val id get() = BlikFormElementId.BLIK_CODE
    }
}

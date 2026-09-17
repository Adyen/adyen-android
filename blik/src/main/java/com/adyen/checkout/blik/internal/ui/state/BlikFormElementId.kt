/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2026.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.form.FormElementId

/**
 * Every element of the Blik form. The helper text above the input is not one: it is screen copy the shopper cannot
 * interact with, so it has nothing to order, focus or validate.
 */
internal enum class BlikFormElementId(override val isTextInput: Boolean) : FormElementId {
    BLIK_CODE(isTextInput = true),
}

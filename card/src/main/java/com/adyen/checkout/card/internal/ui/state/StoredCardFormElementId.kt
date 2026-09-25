/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.form.FormElementId

internal enum class StoredCardFormElementId(override val isTextInput: Boolean) : FormElementId {
    SECURITY_CODE(isTextInput = true),
}

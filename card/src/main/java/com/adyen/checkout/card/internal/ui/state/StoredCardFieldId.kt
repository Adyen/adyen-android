/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.form.FormFieldId

/**
 * Every element of the stored card form, whether or not the shopper can currently see it. The security code is hidden
 * for a stored card that does not ask for one, which leaves the form empty.
 */
internal enum class StoredCardFieldId(override val isTextInput: Boolean) : FormFieldId {
    SECURITY_CODE(isTextInput = true),
}

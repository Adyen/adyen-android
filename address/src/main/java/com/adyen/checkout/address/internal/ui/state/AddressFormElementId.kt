/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.form.FormElementId

internal enum class AddressFormElementId(override val isTextInput: Boolean) : FormElementId {
    COUNTRY(isTextInput = false),
    POSTAL_CODE(isTextInput = true),
}

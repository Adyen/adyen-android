/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.form.FormElementId

/**
 * Every element of the address form, whether or not the shopper can currently see it.
 *
 * The declaration order carries no meaning; the order the shopper sees comes from the selected country's spec.
 */
internal enum class AddressFormElementId(override val isTextInput: Boolean) : FormElementId {
    COUNTRY(isTextInput = false),
    STREET(isTextInput = true),
    HOUSE_NUMBER_OR_NAME(isTextInput = true),
    POSTAL_CODE(isTextInput = true),
    CITY(isTextInput = true),
    STATE_OR_PROVINCE(isTextInput = false),
}

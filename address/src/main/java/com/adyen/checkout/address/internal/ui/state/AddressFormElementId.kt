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
 * The state or province has two elements because it is a picker when its states load and a text field when they fail
 * to load, and whether an element is a text input is fixed per id. The declaration order carries no meaning; the
 * order the shopper sees comes from the selected country's spec.
 */
internal enum class AddressFormElementId(override val isTextInput: Boolean) : FormElementId {
    COUNTRY(isTextInput = false),
    STREET(isTextInput = true),
    HOUSE_NUMBER_OR_NAME(isTextInput = true),
    POSTAL_CODE(isTextInput = true),
    CITY(isTextInput = true),
    STATE_OR_PROVINCE_PICKER(isTextInput = false),
    STATE_OR_PROVINCE_TEXT(isTextInput = true),
}

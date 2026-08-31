/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2026.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.form.FormFieldId

/**
 * Every element of the MB Way form.
 *
 * The country code is a picker rather than a text input, so it has no keyboard action, but it is still a member of the
 * form: it takes part in the order, and the UI renders the form by walking that order.
 */
internal enum class MBWayFieldId(override val isTextInput: Boolean) : FormFieldId {
    COUNTRY_CODE(isTextInput = false),
    PHONE_NUMBER(isTextInput = true),
}

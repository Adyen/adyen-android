/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

internal enum class TestFormElementId(override val isTextInput: Boolean) : FormElementId {
    NUMBER(true),
    VERIFICATION_CODE(true),
    HOLDER_NAME(true),
    STORE_DETAILS(false),
}

internal fun formOf(vararg ids: TestFormElementId) = FormState(elements = ids.map { valid(it) })

internal fun valid(id: TestFormElementId) = FormElementState(id, isValid = true)

internal fun invalid(id: TestFormElementId) = FormElementState(id, isValid = false)

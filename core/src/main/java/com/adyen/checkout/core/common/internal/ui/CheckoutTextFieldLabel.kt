/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 20/10/2026.
 */

package com.adyen.checkout.core.common.internal.ui

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString

/**
 * Resolves the label of a text field, with [CheckoutLocalizationKey.GENERAL_OPTIONAL] appended when the field is
 * optional.
 *
 * @param label The label of the field.
 * @param isOptional Whether the field is optional, usually taken from
 * [com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState.isOptional].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun resolveTextFieldLabel(label: CheckoutLocalizationKey, isOptional: Boolean): String {
    val resolvedLabel = resolveString(label)
    if (!isOptional) return resolvedLabel
    return "$resolvedLabel ${resolveString(CheckoutLocalizationKey.GENERAL_OPTIONAL)}"
}

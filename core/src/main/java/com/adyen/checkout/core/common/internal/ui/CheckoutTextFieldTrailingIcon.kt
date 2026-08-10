/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/8/2026.
 */

package com.adyen.checkout.core.common.internal.ui

import androidx.annotation.RestrictTo
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import com.adyen.checkout.core.components.internal.ui.state.model.TrailingIcon
import com.adyen.checkout.ui.internal.element.input.CheckoutTextFieldErrorIcon

/**
 * Renders the trailing icon of a text field, and animates the transitions between icons.
 *
 * Pass this as the `trailingIcon` of a `CheckoutTextField` to get the generic
 * [TrailingIcon.Empty] and [TrailingIcon.Error] states handled for you. Fields that declare their own
 * [TrailingIcon] states render them through [content].
 *
 * @param trailingIcon The icon to render, usually taken from
 * [com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState.trailingIcon].
 * @param content Renders the field specific states. It is never invoked for [TrailingIcon.Empty] or
 * [TrailingIcon.Error], as those are handled internally.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun CheckoutTextFieldTrailingIcon(
    trailingIcon: TrailingIcon,
    content: @Composable (TrailingIcon) -> Unit = {},
) {
    AnimatedContent(targetState = trailingIcon, label = "TrailingIcon") { state ->
        when (state) {
            TrailingIcon.Empty -> Unit
            TrailingIcon.Error -> CheckoutTextFieldErrorIcon()
            else -> content(state)
        }
    }
}

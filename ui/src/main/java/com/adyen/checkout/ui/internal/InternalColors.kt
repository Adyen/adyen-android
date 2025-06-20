/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/4/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.adyen.checkout.ui.theme.AdyenColor
import com.adyen.checkout.ui.theme.AdyenColors

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Immutable
data class InternalColors(
    val background: Color,
    val container: Color,
    val primary: Color,
    val textOnPrimary: Color,
    val action: Color,
    val destructive: Color,
    val textOnDestructive: Color,
    val disabled: Color,
    val textOnDisabled: Color,
    val outline: Color,
    val text: Color,
    val textSecondary: Color,
) {

    internal companion object {

        fun from(colors: AdyenColors) = with(colors) {
            InternalColors(
                background = background.toCompose(),
                container = container.toCompose(),
                primary = primary.toCompose(),
                textOnPrimary = textOnPrimary.toCompose(),
                action = action.toCompose(),
                destructive = destructive.toCompose(),
                textOnDestructive = textOnDestructive.toCompose(),
                disabled = disabled.toCompose(),
                textOnDisabled = textOnDisabled.toCompose(),
                outline = outline.toCompose(),
                text = text.toCompose(),
                textSecondary = textSecondary.toCompose(),
            )
        }
    }
}

internal fun AdyenColor.toCompose() = Color(value)

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Suppress("LongParameterList")
@Composable
internal fun AdyenTextFieldDecorationBox(
    label: String,
    innerTextField: @Composable () -> Unit,
    supportingText: String?,
    isError: Boolean,
    interactionSource: MutableInteractionSource,
    style: InternalTextFieldStyle,
    prefix: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val isFocused = interactionSource.collectIsFocusedAsState().value

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val labelColor = if (isFocused) style.activeColor else style.textColor
        SubHeadline(
            text = label,
            color = labelColor,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .styledBackground(style, isFocused, isError)
                .fillMaxWidth()
                .heightIn(48.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            prefix?.let {
                Body(prefix, color = AdyenCheckoutTheme.colors.textSecondary)
            }

            val selectionColor = style.activeColor
            val customTextSelectionColors = TextSelectionColors(
                handleColor = selectionColor,
                backgroundColor = selectionColor.copy(alpha = 0.4f),
            )

            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                Box(Modifier.weight(1f)) {
                    innerTextField()
                }
            }

            trailingIcon?.invoke()
        }
        supportingText?.let {
            val supportingTextColor = if (isError) style.errorColor else AdyenCheckoutTheme.colors.textSecondary
            Footnote(
                text = supportingText,
                color = supportingTextColor,
            )
        }
    }
}

@Stable
private fun Modifier.styledBackground(
    style: InternalTextFieldStyle,
    isFocused: Boolean,
    isError: Boolean,
): Modifier {
    val borderColor = when {
        isError -> style.errorColor
        isFocused -> style.textColor
        else -> style.borderColor
    }
    val borderWidth = if (isFocused || isError) style.borderWidth + 1 else style.borderWidth
    return this
        .background(style.backgroundColor, RoundedCornerShape(style.cornerRadius.dp))
        .border(
            width = borderWidth.dp,
            color = borderColor,
            shape = RoundedCornerShape(style.cornerRadius.dp),
        )
}

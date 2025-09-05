/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * A composable function that provides the decoration box for [CheckoutTextField].
 *
 * This internal composable handles the layout and styling of elements surrounding the
 * actual input field, including the label, supporting text, prefix, and trailing icon.
 * It applies Adyen's theming for colors, shapes, and text styles based on the
 * provided [InternalTextFieldStyle] and interaction states (focused, error).
 *
 * @param label The label text to be displayed above the text field.
 * @param innerTextField The composable representing the actual input field (e.g., [BasicTextField]).
 * @param supportingText Optional supporting text to be displayed below the text field.
 * @param isError Indicates whether the text field is in an error state. When `true`,
 * the supporting text and border might change color to reflect an error.
 * @param interactionSource The [MutableInteractionSource] representing the stream of
 * interactions for this text field, used to determine focus state and apply indications.
 * @param innerIndication An optional [Indication] that will be used for the background of the
 * text field. If `null`, no indication will be applied.
 * @param style The [InternalTextFieldStyle] that defines the visual appearance of the
 * text field, including colors, corner radius, and text styles.
 * @param prefix An optional string to be displayed at the beginning of the input area,
 * before the user's input.
 * @param trailingIcon An optional composable function that provides a trailing icon to be
 * displayed at the end of the text field.
 */
@Composable
internal fun CheckoutTextFieldDecorationBox(
    label: String,
    innerTextField: @Composable () -> Unit,
    supportingText: String?,
    isError: Boolean,
    interactionSource: MutableInteractionSource,
    innerIndication: Indication?,
    style: InternalTextFieldStyle,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val isFocused = interactionSource.collectIsFocusedAsState().value

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.Small),
        modifier = modifier,
    ) {
        val labelColor = if (isFocused) style.activeColor else style.textColor
        SubHeadline(
            text = label,
            color = labelColor,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Small),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(style.cornerRadius.dp))
                .indication(interactionSource, innerIndication)
                .styledBackground(style, isFocused, isError)
                .fillMaxWidth()
                .heightIn(Dimensions.MinTouchTarget)
                .padding(horizontal = Dimensions.Large, vertical = Dimensions.Medium),
        ) {
            prefix?.let {
                Body(prefix, color = CheckoutThemeProvider.colors.textSecondary)
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

        AnimatedVisibility(
            visible = supportingText != null,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            val supportingTextColor = if (isError) style.errorColor else CheckoutThemeProvider.colors.textSecondary
            supportingText?.let {
                Footnote(
                    text = it,
                    color = supportingTextColor,
                )
            }
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

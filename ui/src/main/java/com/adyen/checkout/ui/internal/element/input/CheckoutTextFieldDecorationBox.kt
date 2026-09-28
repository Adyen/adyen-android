/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element.input

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.Label
import com.adyen.checkout.ui.internal.text.SubHeadline
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@Suppress("LongMethod")
@Composable
internal fun CheckoutTextFieldDecorationBox(
    innerTextField: @Composable () -> Unit,
    supportingText: String?,
    isError: Boolean,
    interactionSource: MutableInteractionSource,
    innerIndication: Indication?,
    style: InternalTextFieldStyle,
    modifier: Modifier = Modifier,
    label: String? = null,
    hint: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val isFocused = interactionSource.collectIsFocusedAsState().value

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Small),
        modifier = modifier,
    ) {
        label?.let {
            Label(text = label)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(style.cornerRadius.dp))
                .indication(interactionSource, innerIndication)
                .styledBackground(style, isFocused, isError)
                .fillMaxWidth()
                .heightIn(Dimensions.MinTouchTarget),
        ) {
            Spacer(Modifier.size(Dimensions.Spacing.Large))

            leadingContent?.let {
                leadingContent()
                Spacer(Modifier.size(Dimensions.Spacing.Small))
            }

            val selectionColor = style.activeColor
            val customTextSelectionColors = TextSelectionColors(
                handleColor = selectionColor,
                backgroundColor = selectionColor.copy(alpha = 0.4f),
            )

            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                Box(Modifier.weight(1f)) {
                    innerTextField()
                    if (!isFocused && hint != null) {
                        Body(hint, color = CheckoutThemeProvider.colors.textSecondary)
                    }
                }
            }

            if (trailingContent == null) {
                Spacer(Modifier.size(Dimensions.Spacing.Large))
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.sizeIn(
                        minWidth = Dimensions.MinTouchTarget,
                        minHeight = Dimensions.MinTouchTarget,
                    ),
                ) {
                    trailingContent()
                }
            }
        }

        AnimatedVisibility(
            visible = supportingText != null,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            val supportingTextColor = if (isError) style.errorColor else CheckoutThemeProvider.colors.textSecondary
            supportingText?.let {
                SubHeadline(
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
        isFocused -> style.activeColor
        else -> style.borderColor
    }
    val borderWidth = when {
        isError -> style.errorBorderWidth
        isFocused -> style.focusedBorderWidth
        else -> style.defaultBorderWidth
    }
    return this
        .background(style.backgroundColor, RoundedCornerShape(style.cornerRadius.dp))
        .border(
            width = borderWidth,
            color = borderColor,
            shape = RoundedCornerShape(style.cornerRadius.dp),
        )
}

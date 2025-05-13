/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/4/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.theme.AdyenColor
import com.adyen.checkout.ui.theme.AdyenElements
import com.adyen.checkout.ui.theme.AdyenTextFieldStyle
import com.adyen.checkout.ui.theme.AdyenCheckoutTheme as Theme

@Suppress("LongParameterList")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun AdyenTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val style = AdyenTextFieldDefaults.textFieldStyle(AdyenCheckoutTheme.elements.textField)
    val innerTextStyle = AdyenCheckoutTheme.textStyles.body
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            color = style.textColor,
            fontSize = innerTextStyle.size.sp,
            fontWeight = FontWeight(innerTextStyle.weight),
            lineHeight = innerTextStyle.lineHeight.sp,
        ),
        singleLine = true,
        cursorBrush = SolidColor(style.activeColor),
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        modifier = modifier,
        decorationBox = { innerTextField ->
            val isFocused = interactionSource.collectIsFocusedAsState().value

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val labelColor = if (isFocused) style.activeColor else style.textColor
                Label(
                    text = label,
                    color = labelColor,
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .styledBackground(style, isFocused, isError)
                        .fillMaxWidth()
                        .heightIn(48.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    val selectionColor = style.activeColor
                    val customTextSelectionColors = TextSelectionColors(
                        handleColor = selectionColor,
                        backgroundColor = selectionColor.copy(alpha = 0.4f),
                    )

                    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                        innerTextField()
                    }

                    trailingIcon?.invoke()
                }
                supportingText?.let {
                    val supportingTextColor = if (isError) style.errorColor else AdyenCheckoutTheme.colors.textSecondary
                    Caption(
                        text = supportingText,
                        color = supportingTextColor,
                    )
                }
            }
        },
    )
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

@Preview
@Composable
private fun AdyenTextFieldPreview(
    @PreviewParameter(StylingPreviewParameterProvider::class) theme: Theme,
) {
    AdyenCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(theme.colors.background.toCompose())
                .padding(16.dp),
        ) {
            AdyenTextField(
                value = "",
                onValueChange = {},
                label = "Label",
                supportingText = "Description",
            )

            AdyenTextField(
                value = "Value",
                onValueChange = {},
                label = "Label",
                supportingText = "Description",
                trailingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_checkmark),
                        contentDescription = null,
                        tint = AdyenCheckoutTheme.colors.text,
                    )
                },
            )

            val focusRequester = remember { FocusRequester() }
            AdyenTextField(
                value = "Value",
                onValueChange = {},
                label = "Label",
                supportingText = "Description",
                modifier = Modifier.focusRequester(focusRequester),
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            AdyenTextField(
                value = "Value",
                onValueChange = {},
                label = "Label",
                supportingText = "Description",
                isError = true,
            )
        }
    }
}

private class StylingPreviewParameterProvider : PreviewParameterProvider<Theme> {

    private val themeProvider = ThemePreviewParameterProvider()

    private val styles = sequenceOf(
        AdyenTextFieldStyle(),
        AdyenTextFieldStyle(
            backgroundColor = AdyenColor(0x00FFFFFF),
        ),
    )

    override val values = styles.flatMap { style ->
        themeProvider.values.map { theme ->
            theme.copy(
                elements = AdyenElements.default(
                    textField = style,
                ),
            )
        }
    }
}

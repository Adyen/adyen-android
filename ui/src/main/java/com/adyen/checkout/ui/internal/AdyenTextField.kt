/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/4/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.theme.AdyenColor
import com.adyen.checkout.ui.theme.AdyenElements
import com.adyen.checkout.ui.theme.AdyenTextFieldStyle
import com.adyen.checkout.ui.theme.AdyenCheckoutTheme as Theme

/**
 * A composable that provides a styled text field with Adyen's theming.
 *
 * This function wraps [androidx.compose.foundation.text.BasicTextField] and applies
 * styling defined by [InternalTextFieldStyle].
 *
 * @param value The current text to be displayed in the text field.
 * @param onValueChange A callback that is triggered when the text in the field changes.
 * @param label The label text to be displayed for the text field.
 * @param modifier Optional [Modifier] to be applied to this composable.
 * @param enabled Controls the enabled state of the text field. When `false`, the text field
 * is not interactable.
 * @param supportingText Optional supporting text to be displayed below the text field.
 * @param isError Indicates whether the text field is in an error state. When `true`,
 * the text field's appearance may change to reflect an error.
 * @param keyboardOptions Optional keyboard options that can be used to configure the keyboard.
 * @param interactionSource Optional [MutableInteractionSource] representing the stream of
 * interactions for this text field.
 * @param innerIndication Optional [Indication] that will be used for the internal
 * [AdyenTextFieldDecorationBox].
 * @param prefix An optional string to be displayed at the beginning of the input area,
 * before the user's input.
 * @param trailingIcon An optional composable function that provides a trailing icon to be
 * displayed at the end of the text field.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun AdyenTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    innerIndication: Indication? = null,
    prefix: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val style = AdyenTextFieldDefaults.textFieldStyle(AdyenCheckoutTheme.elements.textField)
    val innerTextStyle = AdyenCheckoutTheme.textStyles.body
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
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
        decorationBox = { innerTextField ->
            AdyenTextFieldDecorationBox(
                label = label,
                innerTextField = innerTextField,
                supportingText = supportingText,
                isError = isError,
                interactionSource = interactionSource,
                indication = innerIndication,
                prefix = prefix,
                trailingIcon = trailingIcon,
                style = style,
            )
        },
    )
}

@Preview
@Composable
private fun AdyenTextFieldPreview(
    @PreviewParameter(TextFieldStylePreviewParameterProvider::class) theme: Theme,
) {
    AdyenCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
            modifier = Modifier
                .background(theme.colors.background.toCompose())
                .padding(Dimensions.Large),
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
                prefix = "Prefix",
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

internal class TextFieldStylePreviewParameterProvider : PreviewParameterProvider<Theme> {

    private val themeProvider = ThemePreviewParameterProvider()

    private val styles = sequenceOf(
        AdyenTextFieldStyle(),
        // Transparent background to get an outlined look
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

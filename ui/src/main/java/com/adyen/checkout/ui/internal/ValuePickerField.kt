/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/6/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * A composable that displays a value and a label, designed to trigger an action on click.
 * This field is not directly editable by the user and is visually consistent with [CheckoutTextField].
 * It's typically used for scenarios where selecting a value requires opening a separate picker or dialog.
 *
 * @param value The current value to be displayed in the field.
 * @param label The descriptive label for the field.
 * @param onClick Lambda to be executed when the field is clicked.
 * @param modifier Optional [Modifier] to be applied to this composable.
 * @param supportingText Optional text displayed below the field for additional context or guidance.
 * @param isError Boolean indicating whether the field should be displayed in an error state.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun ValuePickerField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
) {
    val style = CheckoutTextFieldDefaults.textFieldStyle(CheckoutThemeProvider.elements.textField)
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val state = remember(value) { TextFieldState(value) }

    CheckoutTextField(
        label = label,
        inputState = state,
        // Because we disable the CheckoutTextField we need to override focus and input events
        modifier = modifier
            .focusable(interactionSource = interactionSource)
            .pointerInput(onClick) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (up != null) {
                        onClick()
                    }
                }
            }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.isEnter) {
                    onClick()
                    return@onKeyEvent true
                }
                return@onKeyEvent false
            },
        enabled = false,
        supportingText = supportingText,
        isError = isError,
        trailingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = style.textColor,
            )
        },
        interactionSource = interactionSource,
        innerIndication = ripple(color = style.textColor),
    )
}

private val KeyEvent.isEnter: Boolean
    get() = when (key) {
        Key.DirectionCenter,
        Key.Enter,
        Key.NumPadEnter,
        Key.Spacebar -> true

        else -> false
    }

@Preview
@Composable
private fun ValuePickerFieldPreview(
    @PreviewParameter(TextFieldStylePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
            modifier = Modifier
                .background(theme.colors.background.toCompose())
                .padding(Dimensions.Large),
        ) {
            ValuePickerField(
                value = "Value",
                label = "Label",
                onClick = { },
                supportingText = "Description",
            )

            ValuePickerField(
                value = "Value",
                label = "Label",
                onClick = { },
                supportingText = "Description",
                isError = true,
            )
        }
    }
}

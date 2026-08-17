/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

// The optional slot parameters below are named after their Material 3 counterparts. Renaming them to "content" would
// make these components inconsistent with the framework APIs they wrap.
@file:Suppress("ComposableLambdaParameterNaming")

package com.adyen.checkout.ui.internal.element.input

import androidx.annotation.RestrictTo
import androidx.compose.foundation.Indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A composable that provides a styled text field with Adyen's theming.
 *
 * This function wraps [androidx.compose.foundation.text.BasicTextField] and applies
 * styling defined by [InternalTextFieldStyle].
 *
 * @param label The label text to be displayed for the text field.
 * @param modifier Optional [Modifier] to be applied to this composable.
 * @param state The [TextFieldState] to be used for the text field. Use [rememberTextFieldStateWithCurrentValue]
 * to create a state that syncs with external value changes.
 * @param onValueChange A callback that is triggered when the text in the field changes.
 * @param enabled Controls the enabled state of the text field. When `false`, the text field
 * is not interactable.
 * @param supportingText Optional supporting text to be displayed below the text field.
 * @param isError Indicates whether the text field is in an error state. When `true`,
 * the text field's appearance may change to reflect an error.
 * @param keyboardOptions Optional keyboard options that can be used to configure the keyboard.
 * @param imeAction The action key the keyboard shows. It takes precedence over the one in [keyboardOptions] and over
 * the one an [inputTransformation] asks for, so that the field the form considers last is the one that closes the
 * keyboard. Leave it unspecified to keep whatever those two ask for.
 * @param interactionSource Optional [MutableInteractionSource] representing the stream of
 * interactions for this text field.
 * @param innerIndication Optional [Indication] that will be used for the internal
 * [CheckoutTextFieldDecorationBox].
 * @param focusRequest A pending request to give this field focus, or null if there is none. Each new request moves
 * focus once, and is reported back through [onFocusRequestConsumed].
 * @param onFocusRequestConsumed Called after a [focusRequest] has been acted on, so that the state layer can clear it.
 * @param prefix An optional string to be displayed at the beginning of the input area,
 * before the user's input.
 * @param trailingIcon A composable function that provides a trailing icon to be displayed at the end
 * of the text field, or null for no icon at all. Payment method fields should render this through
 * `CheckoutTextFieldTrailingIcon`, which handles the generic empty and error icons for every field.
 * This parameter is required so that every new field has to make a deliberate choice about it.
 */
@Suppress("LongMethod")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun CheckoutTextField(
    label: String?,
    state: TextFieldState,
    modifier: Modifier = Modifier,
    onValueChange: ((String) -> Unit)? = null,
    enabled: Boolean = true,
    supportingText: String? = null,
    isError: Boolean = false,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    imeAction: ImeAction = ImeAction.Unspecified,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    innerIndication: Indication? = null,
    // TODO - Form fields cleanup: will be removed. Use focusRequest below instead. This stays until every field
    // composable has moved over to it.
    shouldFocus: Boolean = false,
    focusRequest: FocusRequestToken? = null,
    onFocusRequestConsumed: (() -> Unit)? = null,
    prefix: String? = null,
    hint: String? = null,
    isSecureField: Boolean = false,
    trailingIcon: @Composable (() -> Unit)?,
) {
    val style = CheckoutThemeProvider.elements.textField
    val innerTextStyle = CheckoutThemeProvider.textStyles.body
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val focusModifier = modifier
        .focusRequester(focusRequester)
        .bringIntoViewRequester(bringIntoViewRequester)
    val textStyle = TextStyle(
        color = style.textColor,
        fontSize = innerTextStyle.size.sp,
        fontWeight = FontWeight(innerTextStyle.weight),
        lineHeight = innerTextStyle.lineHeight.sp,
    )
    val cursorBrush = SolidColor(style.activeColor)
    val resolvedKeyboardOptions = keyboardOptions.merge(KeyboardOptions(imeAction = imeAction))
    val decorator = TextFieldDecorator { innerTextField ->
        CheckoutTextFieldDecorationBox(
            label = label,
            innerTextField = innerTextField,
            supportingText = supportingText,
            isError = isError,
            interactionSource = interactionSource,
            innerIndication = innerIndication,
            prefix = prefix,
            hint = if (state.text.isEmpty()) hint else null,
            trailingIcon = trailingIcon,
            style = style,
        )
    }
    if (!isSecureField) {
        BasicTextField(
            state = state,
            modifier = focusModifier,
            enabled = enabled,
            inputTransformation = inputTransformation,
            outputTransformation = outputTransformation,
            textStyle = textStyle,
            lineLimits = TextFieldLineLimits.SingleLine,
            cursorBrush = cursorBrush,
            keyboardOptions = resolvedKeyboardOptions,
            interactionSource = interactionSource,
            decorator = decorator,
        )
    } else {
        BasicSecureTextField(
            state = state,
            modifier = focusModifier,
            enabled = enabled,
            inputTransformation = inputTransformation,
            textStyle = textStyle,
            cursorBrush = cursorBrush,
            keyboardOptions = resolvedKeyboardOptions,
            interactionSource = interactionSource,
            decorator = decorator,
        )
    }

    if (onValueChange != null) {
        val currentOnValueChange by rememberUpdatedState(onValueChange)
        LaunchedEffect(state) {
            snapshotFlow { state.text }
                .collectLatest { value ->
                    currentOnValueChange(value.toString())
                }
        }
    }

    // TODO - Form fields cleanup: will be removed together with the shouldFocus parameter. The focusRequest effect
    // below does the same job.
    LaunchedEffect(shouldFocus) {
        if (shouldFocus) {
            // Throws if the requester is not attached to a focusable node, which can happen when the field leaves
            // composition between composition and this effect running. Losing the focus is preferable to crashing.
            runCatching { focusRequester.requestFocus() }
        }
    }

    LaunchedEffect(focusRequest) {
        if (focusRequest != null) {
            // Throws if the requester is not attached to a focusable node, which can happen when the field leaves
            // composition between composition and this effect running. Losing the focus is preferable to crashing.
            runCatching { focusRequester.requestFocus() }

            // Taking focus scrolls the field into view on its own, but a field that already has focus is told nothing
            // and would stay off screen, so ask directly. This runs outside the effect because acting on the request
            // clears it, which would otherwise cancel the scroll a frame or two in.
            coroutineScope.launch { bringIntoViewRequester.bringIntoView() }

            onFocusRequestConsumed?.invoke()
        }
    }
}

@Composable
fun rememberTextFieldStateWithCurrentValue(currentText: String): TextFieldState {
    val state = rememberTextFieldState(currentText)
    LaunchedEffect(currentText) {
        // this allows external state changes to be reflected in the text field
        if (state.text.toString() != currentText) {
            state.setTextAndPlaceCursorAtEnd(currentText)
        }
    }
    return state
}

@Preview
@Composable
private fun CheckoutTextFieldPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        CheckoutTextField(
            onValueChange = {},
            label = "Label",
            state = rememberTextFieldStateWithCurrentValue(""),
            supportingText = "Description",
            trailingIcon = null,
        )

        CheckoutTextField(
            onValueChange = {},
            label = "Label",
            state = rememberTextFieldStateWithCurrentValue(""),
            prefix = "Prefix",
            trailingIcon = null,
        )

        val focusRequester = remember { FocusRequester() }
        CheckoutTextField(
            onValueChange = {},
            label = "Label",
            state = rememberTextFieldStateWithCurrentValue("Value"),
            trailingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_checkmark),
                    contentDescription = null,
                    tint = CheckoutThemeProvider.colors.text,
                )
            },
            modifier = Modifier.focusRequester(focusRequester),
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        CheckoutTextField(
            onValueChange = {},
            state = rememberTextFieldStateWithCurrentValue("Value"),
            label = "Label",
            supportingText = "Invalid input",
            isError = true,
            // Components get this icon from CheckoutTextFieldTrailingIcon, it is passed manually here so that the
            // preview matches what an errored field actually looks like.
            trailingIcon = { CheckoutTextFieldErrorIcon() },
        )

        CheckoutTextField(
            onValueChange = {},
            state = rememberTextFieldStateWithCurrentValue("Value"),
            label = "Password",
            isSecureField = true,
            modifier = Modifier.focusRequester(focusRequester),
            trailingIcon = null,
        )
    }
}

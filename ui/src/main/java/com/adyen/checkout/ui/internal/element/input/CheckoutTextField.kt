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
import androidx.compose.foundation.text.input.KeyboardActionHandler
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.autofill.contentType
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
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * An Adyen-themed single-line text field backed by [TextFieldState].
 *
 * Regular fields use [BasicTextField], while secure fields use [BasicSecureTextField]. Their shared decoration and
 * appearance are defined by [CheckoutTextFieldDecorationBox] and [InternalTextFieldStyle].
 *
 * @param state State that owns the text and selection. Use [rememberTextFieldStateWithCurrentValue] when external value
 * changes must be reflected in the field.
 * @param label Optional label displayed above the input.
 * @param contentType Autofill content type, or `null` when none applies to the field.
 * @param modifier [Modifier] applied to the text field.
 * @param onValueChange Optional callback invoked after the text in [state] changes.
 * @param enabled Whether the field accepts user input.
 * @param supportingText Optional text displayed below the field.
 * @param hint Optional hint displayed when [state] is empty.
 * @param isError Whether to use the error appearance for the field and its supporting text.
 * @param isSecureField Whether to obscure the input by rendering a [BasicSecureTextField].
 * @param inputTransformation Transformation applied to user input before it is committed to [state].
 * @param outputTransformation Transformation applied when displaying a regular field. Secure fields do not use it.
 * @param keyboardOptions Options used to configure the software keyboard.
 * @param imeAction Action displayed by the software keyboard. This overrides the action from [keyboardOptions] and
 * [inputTransformation]. Leave it unspecified to use the action requested by either of them.
 * @param isFocusRequested Whether the form is requesting focus for this field. Each request is handled once and then
 * reported through [onFocusRequestConsumed].
 * @param onFocusRequestConsumed Optional callback invoked after a focus request has been handled so the state layer can
 * clear it.
 * @param interactionSource Source through which field interactions are emitted.
 * @param innerIndication Optional indication drawn by [CheckoutTextFieldDecorationBox].
 * @param style Visual style applied to the field.
 * @param leadingContent Optional content displayed at the start of the field.
 * @param trailingContent Optional content displayed at the end of the field. Payment method fields should render this
 * through `CheckoutTextFieldTrailingIcon`, which handles generic empty and error states. This parameter is required so
 * every field makes a deliberate trailing-content choice.
 */
@Suppress("LongMethod")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun CheckoutTextField(
    state: TextFieldState,
    label: String?,
    contentType: ContentType?,
    modifier: Modifier = Modifier,
    onValueChange: ((String) -> Unit)? = null,
    enabled: Boolean = true,
    supportingText: String? = null,
    hint: String? = null,
    isError: Boolean = false,
    isSecureField: Boolean = false,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    imeAction: ImeAction = ImeAction.Unspecified,
    onKeyboardAction: KeyboardActionHandler? = null,
    isFocusRequested: Boolean = false,
    onFocusRequestConsumed: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    innerIndication: Indication? = null,
    style: InternalTextFieldStyle = CheckoutThemeProvider.elements.textField,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)?,
) {
    val innerTextStyle = CheckoutThemeProvider.textStyles.body
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val autofillModifier = contentType?.let { modifier.contentType(it) } ?: modifier
    val focusModifier = autofillModifier
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
            hint = if (state.text.isEmpty()) hint else null,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
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
            onKeyboardAction = onKeyboardAction,
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
            onKeyboardAction = onKeyboardAction,
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

    LaunchedEffect(isFocusRequested) {
        if (isFocusRequested) {
            // runCatching is there to prevent a crash caused by the requester not being attached to a focusable node
            // this can happen if the field leaves composition between composition and this effect running
            runCatching { focusRequester.requestFocus() }

            // This is needed when the focus is requested on a field that already has focus but is not visible (e.g.
            // hidden by scrolling)
            bringIntoViewRequester.bringIntoView()

            // Should be called last because it clears the request, which ends this effect
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
            label = "Label",
            state = rememberTextFieldStateWithCurrentValue(""),
            contentType = null,
            supportingText = "Description",
            trailingContent = null,
        )

        CheckoutTextField(
            label = "Label",
            state = rememberTextFieldStateWithCurrentValue(""),
            contentType = null,
            leadingContent = { Body("Prefix", color = CheckoutThemeProvider.colors.textSecondary) },
            trailingContent = null,
        )

        val focusRequester = remember { FocusRequester() }
        CheckoutTextField(
            label = "Label",
            state = rememberTextFieldStateWithCurrentValue("Value"),
            contentType = null,
            leadingContent = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = CheckoutThemeProvider.colors.text,
                )
            },
            trailingContent = {
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
            state = rememberTextFieldStateWithCurrentValue("Value"),
            contentType = null,
            label = "Label",
            supportingText = "Invalid input",
            isError = true,
            // Components get this icon from CheckoutTextFieldTrailingIcon, it is passed manually here so that the
            // preview matches what an errored field actually looks like.
            trailingContent = { CheckoutTextFieldErrorIcon() },
        )

        CheckoutTextField(
            state = rememberTextFieldStateWithCurrentValue("Value"),
            contentType = null,
            label = "Password",
            isSecureField = true,
            modifier = Modifier.focusRequester(focusRequester),
            trailingContent = null,
        )
    }
}

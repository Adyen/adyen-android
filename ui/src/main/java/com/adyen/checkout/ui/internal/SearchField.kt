/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/11/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.coroutines.flow.collectLatest

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun SearchField(
    modifier: Modifier = Modifier,
    // TODO - Use string resources
    hint: String = "Search..",
    onValueChange: ((String) -> Unit)? = null,
    enabled: Boolean = true,
    supportingText: String? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    shouldFocus: Boolean = false,
) {
    val style = CheckoutTextFieldDefaults.textFieldStyle(CheckoutThemeProvider.elements.textField)
    val innerTextStyle = CheckoutThemeProvider.textStyles.body
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val state = rememberTextFieldState()

    BasicTextField(
        state = state,
        modifier = modifier.focusRequester(focusRequester),
        enabled = enabled,
        textStyle = TextStyle(
            color = style.textColor,
            fontSize = innerTextStyle.size.sp,
            fontWeight = FontWeight(innerTextStyle.weight),
            lineHeight = innerTextStyle.lineHeight.sp,
        ),
        lineLimits = TextFieldLineLimits.SingleLine,
        cursorBrush = SolidColor(style.activeColor),
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        decorator = { innerTextField ->
            CheckoutTextFieldDecorationBox(
                hint = if (state.text.isEmpty()) hint else null,
                innerTextField = innerTextField,
                supportingText = supportingText,
                isError = isError,
                interactionSource = interactionSource,
                innerIndication = null,
                trailingIcon = {
                    TrailingSearchIcon(
                        isQueryEmpty = state.text.isEmpty(),
                        onDeleteClick = { state.edit { delete(0, state.text.length) } },
                        rippleColor = style.textColor,
                    )
                },
                style = style,
            )
        },
    )

    if (onValueChange != null) {
        val currentOnValueChange by rememberUpdatedState(onValueChange)
        LaunchedEffect(state) {
            snapshotFlow { state.text }
                .collectLatest { value ->
                    currentOnValueChange(value.toString())
                }
        }
    }

    LaunchedEffect(shouldFocus) {
        if (shouldFocus) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
private fun TrailingSearchIcon(
    isQueryEmpty: Boolean,
    onDeleteClick: () -> Unit,
    rippleColor: Color,
) {
    val trailingIconResource = if (isQueryEmpty) {
        R.drawable.ic_search
    } else {
        R.drawable.ic_cross
    }

    Icon(
        imageVector = ImageVector.vectorResource(trailingIconResource),
        contentDescription = null,
        tint = CheckoutThemeProvider.colors.text,
        modifier = if (!isQueryEmpty) {
            Modifier.clickable(
                interactionSource = null,
                indication = ripple(color = rippleColor, radius = Dimensions.Medium),
                role = Role.Button,
                onClick = onDeleteClick,
            )
        } else {
            Modifier
        },
    )
}

@Preview
@Composable
private fun SearchFieldPreview(
    @PreviewParameter(TextFieldStylePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
            modifier = Modifier
                .background(theme.colors.background.toCompose())
                .padding(Dimensions.Large),
        ) {
            SearchField()

            val focusRequester = remember { FocusRequester() }
            SearchField(
                modifier = Modifier.focusRequester(focusRequester),
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            SearchField(
                isError = true,
                supportingText = "Error",
            )
        }
    }
}

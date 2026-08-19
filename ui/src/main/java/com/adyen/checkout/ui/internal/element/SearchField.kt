/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element

import androidx.annotation.RestrictTo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.rememberTextFieldStateWithCurrentValue
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun SearchField(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(),
    hint: String,
    onValueChange: ((String) -> Unit)? = null,
    enabled: Boolean = true,
    supportingText: String? = null,
    isError: Boolean = false,
    shouldFocus: Boolean = false,
) {
    val style = CheckoutThemeProvider.elements.searchField
    CheckoutTextField(
        state = state,
        onValueChange = onValueChange,
        label = null,
        hint = if (state.text.isEmpty()) hint else null,
        supportingText = supportingText,
        isError = isError,
        enabled = enabled,
        innerIndication = null,
        shouldFocus = shouldFocus,
        style = style,
        leadingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                contentDescription = null,
                tint = style.textColor,
            )
        },
        trailingIcon = trailingSearchIcon(
            shouldShowIcon = state.text.isNotEmpty(),
            onDeleteClick = { state.edit { delete(0, state.text.length) } },
            rippleColor = style.textColor,
        ),
        modifier = modifier,
    )
}

@Composable
private fun trailingSearchIcon(
    shouldShowIcon: Boolean,
    onDeleteClick: () -> Unit,
    rippleColor: Color,
): (@Composable () -> Unit)? {
    return if (shouldShowIcon) {
        @Composable {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_cross),
                contentDescription = null,
                tint = CheckoutThemeProvider.colors.text,
                modifier = Modifier.clickable(
                    interactionSource = null,
                    indication = ripple(color = rippleColor, radius = Dimensions.Spacing.Medium),
                    role = Role.Button,
                    onClick = onDeleteClick,
                ),
            )
        }
    } else {
        null
    }
}

@Preview
@Composable
private fun SearchFieldPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        SearchField(
            hint = "Search..",
        )

        val focusRequester = remember { FocusRequester() }
        SearchField(
            hint = "Search..",
            state = rememberTextFieldStateWithCurrentValue("Value"),
            modifier = Modifier.focusRequester(focusRequester),
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        SearchField(
            hint = "Search..",
            isError = true,
            supportingText = "Error",
        )
    }
}

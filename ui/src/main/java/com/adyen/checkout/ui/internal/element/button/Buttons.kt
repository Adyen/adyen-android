/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element.button

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.BodyEmphasized
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    buttonState: CheckoutButtonState = CheckoutButtonState.ENABLED,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    CheckoutButton(
        onClick = onClick,
        text = text,
        modifier = modifier,
        buttonState = buttonState,
        leadingIcon = leadingIcon,
        style = CheckoutThemeProvider.elements.buttons.primary,
    )
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        PrimaryButton(
            onClick = {},
            text = "Primary",
            buttonState = CheckoutButtonState.ENABLED,
            modifier = Modifier.fillMaxWidth(),
        )
        PrimaryButton(
            onClick = {},
            text = "Primary",
            buttonState = CheckoutButtonState.ENABLED,
            leadingIcon = {
                Icon(painter = painterResource(id = R.drawable.ic_checkmark), contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        PrimaryButton(
            onClick = {},
            text = "Loading",
            buttonState = CheckoutButtonState.LOADING,
            modifier = Modifier.fillMaxWidth(),
        )
        PrimaryButton(
            onClick = {},
            text = "Primary",
            buttonState = CheckoutButtonState.DISABLED,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    buttonState: CheckoutButtonState = CheckoutButtonState.ENABLED,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    CheckoutButton(
        onClick = onClick,
        text = text,
        modifier = modifier,
        buttonState = buttonState,
        leadingIcon = leadingIcon,
        style = CheckoutThemeProvider.elements.buttons.secondary,
    )
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        SecondaryButton(
            onClick = {},
            text = "Secondary",
            buttonState = CheckoutButtonState.ENABLED,
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            onClick = {},
            text = "Secondary",
            buttonState = CheckoutButtonState.ENABLED,
            leadingIcon = {
                Icon(painter = painterResource(id = R.drawable.ic_checkmark), contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            onClick = {},
            text = "Loading",
            buttonState = CheckoutButtonState.LOADING,
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            onClick = {},
            text = "Secondary",
            buttonState = CheckoutButtonState.DISABLED,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun TertiaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    buttonState: CheckoutButtonState = CheckoutButtonState.ENABLED,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    CheckoutButton(
        onClick = onClick,
        text = text,
        modifier = modifier,
        buttonState = buttonState,
        leadingIcon = leadingIcon,
        style = CheckoutThemeProvider.elements.buttons.tertiary,
    )
}

@Preview(showBackground = true)
@Composable
private fun TertiaryButtonPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        TertiaryButton(
            onClick = {},
            text = "Tertiary",
            buttonState = CheckoutButtonState.ENABLED,
            modifier = Modifier.fillMaxWidth(),
        )
        TertiaryButton(
            onClick = {},
            text = "Tertiary",
            buttonState = CheckoutButtonState.ENABLED,
            leadingIcon = {
                Icon(painter = painterResource(id = R.drawable.ic_checkmark), contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        TertiaryButton(
            onClick = {},
            text = "Loading",
            buttonState = CheckoutButtonState.LOADING,
            modifier = Modifier.fillMaxWidth(),
        )
        TertiaryButton(
            onClick = {},
            text = "Tertiary",
            buttonState = CheckoutButtonState.DISABLED,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun DestructiveButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    buttonState: CheckoutButtonState = CheckoutButtonState.ENABLED,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    CheckoutButton(
        onClick = onClick,
        text = text,
        modifier = modifier,
        buttonState = buttonState,
        leadingIcon = leadingIcon,
        style = CheckoutThemeProvider.elements.buttons.destructive,
    )
}

@Preview(showBackground = true)
@Composable
private fun DestructiveButtonPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        DestructiveButton(
            onClick = {},
            text = "Destructive",
            buttonState = CheckoutButtonState.ENABLED,
            modifier = Modifier.fillMaxWidth(),
        )
        DestructiveButton(
            onClick = {},
            text = "Destructive",
            buttonState = CheckoutButtonState.ENABLED,
            leadingIcon = {
                Icon(painter = painterResource(id = R.drawable.ic_checkmark), contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        DestructiveButton(
            onClick = {},
            text = "Loading",
            buttonState = CheckoutButtonState.LOADING,
            modifier = Modifier.fillMaxWidth(),
        )
        DestructiveButton(
            onClick = {},
            text = "Destructive",
            buttonState = CheckoutButtonState.DISABLED,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CheckoutButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier,
    buttonState: CheckoutButtonState,
    leadingIcon: @Composable (() -> Unit)?,
    style: InternalButtonStyle,
) {
    val enabled = when (buttonState) {
        CheckoutButtonState.ENABLED -> true
        CheckoutButtonState.LOADING,
        CheckoutButtonState.DISABLED -> false
    }
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(style.cornerRadius.dp),
        enabled = enabled,
        colors = buttonColors(
            containerColor = style.backgroundColor,
            contentColor = style.textColor,
            disabledContainerColor = style.disabledBackgroundColor,
            disabledContentColor = style.disabledTextColor,
        ),
        content = {
            val contentColor = when (buttonState) {
                CheckoutButtonState.ENABLED -> style.textColor
                CheckoutButtonState.LOADING -> style.loadingTextColor
                CheckoutButtonState.DISABLED -> style.disabledTextColor
            }
            if (buttonState == CheckoutButtonState.LOADING) {
                val size = 16 * LocalDensity.current.fontScale
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = (size / 8).dp,
                    modifier = Modifier.size(size.dp),
                )
                Spacer(Modifier.size(8.dp))
            } else {
                leadingIcon?.let {
                    it()
                    Spacer(Modifier.size(8.dp))
                }
            }

            BodyEmphasized(text, color = contentColor)
        },
        contentPadding = PaddingValues(
            horizontal = Dimensions.Spacing.ExtraLarge,
            vertical = Dimensions.Spacing.Medium,
        ),
        modifier = modifier,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class CheckoutButtonState {
    ENABLED,
    LOADING,
    DISABLED,
}

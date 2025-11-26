/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/4/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.adyen.checkout.ui.theme.CheckoutTheme

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    CheckoutButton(
        onClick = onClick,
        text = text,
        modifier = modifier,
        isLoading = isLoading,
        style = ButtonDefaults.primaryButtonStyle(),
    )
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
            modifier = Modifier
                .background(CheckoutThemeProvider.colors.background)
                .padding(Dimensions.Large),
        ) {
            PrimaryButton(
                onClick = {},
                text = "Primary",
                isLoading = false,
                modifier = Modifier.fillMaxWidth(),
            )
            PrimaryButton(
                onClick = {},
                text = "Loading",
                isLoading = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    CheckoutButton(
        onClick = onClick,
        text = text,
        modifier = modifier,
        isLoading = isLoading,
        style = ButtonDefaults.secondaryButtonStyle(),
    )
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
            modifier = Modifier
                .background(CheckoutThemeProvider.colors.background)
                .padding(Dimensions.Large),
        ) {
            SecondaryButton(
                onClick = {},
                text = "Secondary",
                isLoading = false,
                modifier = Modifier.fillMaxWidth(),
            )
            SecondaryButton(
                onClick = {},
                text = "Loading",
                isLoading = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun TertiaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    CheckoutButton(
        onClick = onClick,
        text = text,
        modifier = modifier,
        isLoading = isLoading,
        style = ButtonDefaults.tertiaryButtonStyle(),
    )
}

@Preview(showBackground = true)
@Composable
private fun TertiaryButtonPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
            modifier = Modifier
                .background(CheckoutThemeProvider.colors.background)
                .padding(Dimensions.Large),
        ) {
            TertiaryButton(
                onClick = {},
                text = "Tertiary",
                isLoading = false,
                modifier = Modifier.fillMaxWidth(),
            )
            TertiaryButton(
                onClick = {},
                text = "Loading",
                isLoading = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun DestructiveButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    CheckoutButton(
        onClick = onClick,
        text = text,
        modifier = modifier,
        isLoading = isLoading,
        style = ButtonDefaults.destructiveButtonStyle(),
    )
}

@Preview(showBackground = true)
@Composable
private fun DestructiveButtonPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
            modifier = Modifier
                .background(CheckoutThemeProvider.colors.background)
                .padding(Dimensions.Large),
        ) {
            DestructiveButton(
                onClick = {},
                text = "Destructive",
                isLoading = false,
                modifier = Modifier.fillMaxWidth(),
            )
            DestructiveButton(
                onClick = {},
                text = "Loading",
                isLoading = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CheckoutButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier,
    isLoading: Boolean,
    style: InternalButtonStyle,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(style.cornerRadius.dp),
        enabled = !isLoading,
        colors = buttonColors(
            containerColor = style.backgroundColor,
            contentColor = style.textColor,
            disabledContainerColor = style.disabledBackgroundColor,
            disabledContentColor = style.disabledTextColor,
        ),
        content = {
            val contentColor = if (isLoading) style.disabledTextColor else style.textColor
            if (isLoading) {
                val size = 16 * LocalDensity.current.fontScale
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = (size / 8).dp,
                    modifier = Modifier.size(size.dp),
                )
                Spacer(Modifier.size(8.dp))
            }

            BodyEmphasized(text, color = contentColor)
        },
        contentPadding = PaddingValues(horizontal = Dimensions.ExtraLarge, vertical = Dimensions.Medium),
        modifier = modifier,
    )
}

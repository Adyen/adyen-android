/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/4/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import com.adyen.checkout.ui.internal.theme.InternalTextStyle
import com.adyen.checkout.ui.theme.CheckoutTheme

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun Title(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.title, color, modifier, maxLines)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun Subtitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.subtitle, color, modifier, maxLines)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun Body(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.body, color, modifier, maxLines)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun BodyEmphasized(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.bodyEmphasized, color, modifier, maxLines)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun SubHeadline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.subHeadline, color, modifier, maxLines)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun SubHeadlineEmphasized(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.subHeadlineEmphasized, color, modifier, maxLines)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun Footnote(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.footnote, color, modifier, maxLines)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun FootnoteEmphasized(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.footnoteEmphasized, color, modifier, maxLines)
}

@Composable
private fun CheckoutText(
    text: String,
    style: InternalTextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = text,
        color = color,
        fontSize = style.size.sp,
        fontWeight = FontWeight(style.weight),
        lineHeight = style.lineHeight.sp,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun TextPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Small),
            modifier = Modifier
                .background(CheckoutThemeProvider.colors.background)
                .padding(Dimensions.Large),
        ) {
            Title("Title")
            Subtitle("Subtitle")
            Body("Body")
            BodyEmphasized("Body Emphasized")
            SubHeadline("SubHeadline")
            SubHeadlineEmphasized("SubHeadline Emphasized")
            Footnote("Footnote")
            FootnoteEmphasized("Footnote Emphasized")
        }
    }
}

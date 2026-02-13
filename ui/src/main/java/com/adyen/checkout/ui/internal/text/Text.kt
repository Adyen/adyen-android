/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/11/2025.
 */

package com.adyen.checkout.ui.internal.text

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun Title(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.title, color, modifier, maxLines, textAlign)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun Subtitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.subtitle, color, modifier, maxLines, textAlign)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun Body(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.body, color, modifier, maxLines, textAlign)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun BodyEmphasized(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.bodyEmphasized, color, modifier, maxLines, textAlign)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun SubHeadline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.subHeadline, color, modifier, maxLines, textAlign)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun SubHeadlineEmphasized(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.subHeadlineEmphasized, color, modifier, maxLines, textAlign)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun Footnote(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.footnote, color, modifier, maxLines, textAlign)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun FootnoteEmphasized(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CheckoutThemeProvider.colors.text,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    CheckoutText(text, CheckoutThemeProvider.textStyles.footnoteEmphasized, color, modifier, maxLines, textAlign)
}

@Composable
private fun CheckoutText(
    text: String,
    style: InternalTextStyle,
    color: Color,
    modifier: Modifier,
    maxLines: Int,
    textAlign: TextAlign?,
) {
    Text(
        text = text,
        color = color,
        fontSize = style.size.sp,
        fontWeight = FontWeight(style.weight),
        lineHeight = style.lineHeight.sp,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
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
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Small),
            modifier = Modifier
                .background(CheckoutThemeProvider.colors.background)
                .padding(Dimensions.Spacing.Large),
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

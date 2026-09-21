/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/9/2026.
 */

package com.adyen.checkout.core.common.internal.ui

import androidx.annotation.RestrictTo
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.Title
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * An icon above a title, a description and [content], introducing a screen that does not introduce itself through a
 * collapsing app bar title — anything stored, any state the shopper cannot type into, and any payment being completed
 * elsewhere.
 *
 * @param logoTxVariant The transaction variant the payment method logo is loaded for.
 * @param title What the screen is about, such as the payment method brand or its last four digits.
 * @param description The line about what is being paid or what is happening.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param content Drawn below the text.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun IconHeaderContent(
    logoTxVariant: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    IconHeaderContentLayout(
        icon = {
            CheckoutNetworkLogo(
                txVariant = logoTxVariant,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.LogoSize.large),
            )
        },
        title = title,
        description = description,
        modifier = modifier,
        content = content,
    )
}

/**
 * The same screen for one whose subject is not a payment method, and so has no logo to load: it draws an icon that
 * ships with the SDK instead.
 *
 * The icon is drawn at the painter's own size, so pass one that already measures what the design calls for.
 *
 * @param icon Drawn above the text, centred.
 * @param title What the screen is about.
 * @param description The line about what is happening.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param content Drawn below the text.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun IconHeaderContent(
    icon: Painter,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    IconHeaderContentLayout(
        icon = { Image(painter = icon, contentDescription = null) },
        title = title,
        description = description,
        modifier = modifier,
        content = content,
    )
}

@Composable
private fun IconHeaderContentLayout(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth(),
    ) {
        icon()

        Spacer(Modifier.size(Dimensions.Spacing.ExtraLarge))

        Title(
            text = title,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.size(Dimensions.Spacing.Small))

        Body(
            text = description,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.size(Dimensions.Spacing.DoubleExtraLarge))

        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun IconHeaderContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        IconHeaderContent(
            logoTxVariant = "ideal",
            title = "Redirection",
            description = "You are guided to the next step of the process.",
        ) {
            PaymentProgressStatus(title = "Processing..")
        }
    }
}

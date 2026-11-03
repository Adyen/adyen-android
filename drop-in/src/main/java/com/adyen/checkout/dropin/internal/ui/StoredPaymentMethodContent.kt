/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/9/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.common.internal.ui.IconHeaderContent
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.dropin.internal.helper.SavedStateBackStackPersister
import com.adyen.checkout.ui.internal.element.button.PrimaryButton
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * @param content What the payment method component renders.
 */
@Composable
internal fun StoredPaymentMethodContent(
    navigator: DropInNavigator,
    viewState: PaymentMethodViewState.Stored,
    content: @Composable () -> Unit,
) {
    DropInScaffold(
        navigationIcon = { PaymentMethodNavigationIcon(navigator) },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(Dimensions.Spacing.Large),
                verticalArrangement = Arrangement.Center,
            ) {
                IconHeaderContent(
                    logoTxVariant = viewState.logoTxVariant,
                    title = viewState.title,
                    description = viewState.description.resolve(),
                    content = content,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StoredPaymentMethodContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    StoredPaymentMethodContentPreview(
        theme = theme,
        navigator = DropInNavigator(SavedStateBackStackPersister(SavedStateHandle())),
        description = UiText.Localized(
            CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION_WITH_AMOUNT,
            listOf("Visa", "$140.98"),
        ),
        payButtonText = "Pay $140.98",
    )
}

@Preview(showBackground = true)
@Composable
private fun StoredPaymentMethodContentZeroAmountPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    StoredPaymentMethodContentPreview(
        theme = theme,
        navigator = DropInNavigator(SavedStateBackStackPersister(SavedStateHandle())),
        description = UiText.Localized(
            CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION_SAVE_DETAILS,
            listOf("Visa"),
        ),
        payButtonText = "Save details",
    )
}

@Preview(showBackground = true)
@Composable
private fun StoredPaymentMethodContentNoAmountPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    StoredPaymentMethodContentPreview(
        theme = theme,
        navigator = DropInNavigator(SavedStateBackStackPersister(SavedStateHandle())),
        description = UiText.Localized(
            CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION,
            listOf("Visa"),
        ),
        payButtonText = "Pay",
    )
}

@Composable
private fun StoredPaymentMethodContentPreview(
    theme: CheckoutTheme,
    navigator: DropInNavigator,
    description: UiText,
    payButtonText: String,
) {
    CheckoutThemePreviewWrapper(theme) {
        StoredPaymentMethodContent(
            navigator = navigator,
            viewState = PaymentMethodViewState.Stored(
                logoTxVariant = "visa",
                title = "•••• 4556",
                description = description,
            ),
            // Stands in for the payment method component, which cannot be built without a controller.
            content = {
                PrimaryButton(onClick = {}, text = payButtonText, modifier = Modifier.fillMaxWidth())
            },
        )
    }
}

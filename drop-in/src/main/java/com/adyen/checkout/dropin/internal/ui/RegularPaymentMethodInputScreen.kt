/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/9/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.dropin.internal.helper.SavedStateBackStackPersister
import com.adyen.checkout.ui.internal.element.button.PrimaryButton
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * @param content What the payment method component renders: the form and its own button.
 */
@Composable
internal fun RegularPaymentMethodInputScreen(
    navigator: DropInNavigator,
    viewState: PaymentMethodViewState.RegularInput,
    content: @Composable () -> Unit,
) {
    DropInScaffold(
        navigationIcon = { PaymentMethodNavigationIcon(navigator) },
        title = viewState.paymentMethodName,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            viewState.description?.let {
                Body(
                    text = resolveString(it),
                    color = CheckoutThemeProvider.colors.textSecondary,
                    modifier = Modifier.padding(
                        start = Dimensions.Spacing.Large,
                        top = Dimensions.Spacing.ExtraSmall,
                        end = Dimensions.Spacing.Large,
                        bottom = Dimensions.Spacing.Medium,
                    ),
                )
            }

            Column(modifier = Modifier.padding(Dimensions.Spacing.Large)) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegularPaymentMethodInputScreenPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        RegularPaymentMethodInputScreen(
            navigator = DropInNavigator(SavedStateBackStackPersister(SavedStateHandle())),
            viewState = PaymentMethodViewState.RegularInput(
                paymentMethodName = "Cards",
                description = CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_CARD_DESCRIPTION,
            ),
            // Stands in for the payment method component, which cannot be built without a controller.
            content = {
                PrimaryButton(onClick = {}, text = "Pay $140.98", modifier = Modifier.fillMaxWidth())
            },
        )
    }
}

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.common.internal.ui.IconHeaderContent
import com.adyen.checkout.core.common.internal.ui.NavigationCloseButton
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.element.button.PrimaryButton
import com.adyen.checkout.ui.internal.element.button.SecondaryButton
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun PreselectedPaymentMethodScreen(
    viewModel: PreselectedPaymentMethodViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    viewState?.let {
        PreselectedPaymentMethodContent(
            it,
            onBackClicked = { viewModel.onBackClicked() },
            onPayClicked = { viewModel.onPayClicked() },
            onOtherPaymentMethodClicked = { viewModel.onOtherPaymentMethodClicked() },
        )
    }
}

@Composable
private fun PreselectedPaymentMethodContent(
    viewState: PreselectedPaymentMethodViewState,
    onBackClicked: () -> Unit,
    onPayClicked: () -> Unit,
    onOtherPaymentMethodClicked: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        NavigationCloseButton(
            onClick = onBackClicked,
        )

        Spacer(Modifier.size(Dimensions.Spacing.ExtraLarge))

        IconHeaderContent(
            logoTxVariant = viewState.logoTxVariant,
            title = viewState.title,
            description = viewState.subtitle,
            modifier = Modifier.padding(horizontal = Dimensions.Spacing.Large),
        ) {
            PrimaryButton(
                onClick = onPayClicked,
                text = viewState.payButtonText,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.size(Dimensions.Spacing.Large))

            SecondaryButton(
                onClick = onOtherPaymentMethodClicked,
                text = resolveString(CheckoutLocalizationKey.DROP_IN_OTHER_PAYMENT_METHODS),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreselectedPaymentMethodScreenPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        val viewState = PreselectedPaymentMethodViewState(
            logoTxVariant = "visa",
            title = "•••• 1234",
            subtitle = "Use Visa to pay $9.99",
            payButtonText = "Pay $9.99",
        )
        PreselectedPaymentMethodContent(
            viewState = viewState,
            onBackClicked = {},
            onPayClicked = {},
            onOtherPaymentMethodClicked = {},
        )
    }
}

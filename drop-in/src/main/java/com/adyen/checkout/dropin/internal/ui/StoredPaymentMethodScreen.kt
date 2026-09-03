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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.adyen.checkout.dropin.internal.ui.element.PaymentMethodHeader
import com.adyen.checkout.ui.internal.element.button.PrimaryButton
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * @param content What the payment method component renders.
 */
@Composable
internal fun StoredPaymentMethodScreen(
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
                // TODO - This header is not final. When the component takes input the design names what is being
                //  asked for, such as "Security code", and says what it is for. Only the component knows what it is
                //  asking for, so this introduces the payment method for now.
                PaymentMethodHeader(
                    logoTxVariant = viewState.logoTxVariant,
                    paymentMethodName = viewState.title,
                    description = resolveString(
                        CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_STORED_DESCRIPTION,
                        viewState.paymentMethodName,
                        viewState.formattedAmount,
                    ),
                )

                Spacer(Modifier.size(Dimensions.Spacing.DoubleExtraLarge))

                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StoredPaymentMethodScreenPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        StoredPaymentMethodScreen(
            navigator = DropInNavigator(SavedStateBackStackPersister(SavedStateHandle())),
            viewState = PaymentMethodViewState.Stored(
                logoTxVariant = "visa",
                title = "•••• 4556",
                paymentMethodName = "Visa",
                formattedAmount = "$140.98",
            ),
            // Stands in for the payment method component, which cannot be built without a controller.
            content = {
                PrimaryButton(onClick = {}, text = "Pay $140.98", modifier = Modifier.fillMaxWidth())
            },
        )
    }
}

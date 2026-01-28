/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.AdyenPaymentFlow
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun PaymentMethodScreen(
    navigator: DropInNavigator,
    viewModel: PaymentMethodViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    PaymentMethodScreenContent(navigator, viewState)
}

@Composable
private fun PaymentMethodScreenContent(
    navigator: DropInNavigator,
    viewState: PaymentMethodViewState,
) {
    DropInScaffold(
        navigationIcon = {
            IconButton(
                onClick = { navigator.back() },
            ) {
                Icon(Icons.Default.Close, resolveString(CheckoutLocalizationKey.GENERAL_CLOSE))
            }
        },
        title = viewState.paymentMethod.name,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            viewState.description?.let {
                Body(
                    text = resolveString(it),
                    color = CheckoutThemeProvider.colors.textSecondary,
                    modifier = Modifier
                        .padding(
                            start = Dimensions.Spacing.Large,
                            top = Dimensions.Spacing.ExtraSmall,
                            end = Dimensions.Spacing.Large,
                            bottom = Dimensions.Spacing.Medium,
                        ),
                )
            }

            AdyenPaymentFlow(
                paymentMethod = viewState.paymentMethod,
                checkoutContext = viewState.checkoutContext,
                // TODO - Add callbacks implementation
                checkoutCallbacks = CheckoutCallbacks(
                    onSubmit = { CheckoutResult.Finished() },
                    onAdditionalDetails = { CheckoutResult.Finished() },
                    onError = {},
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.Spacing.Large),
            )
        }
    }
}

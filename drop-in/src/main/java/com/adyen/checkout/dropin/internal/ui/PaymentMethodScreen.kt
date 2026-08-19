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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutPaymentMethod
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun PaymentMethodScreen(
    navigator: DropInNavigator,
    controller: CheckoutController,
    viewModel: PaymentMethodViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    PaymentMethodScreenContent(navigator, viewState, controller)
}

@Composable
private fun PaymentMethodScreenContent(
    navigator: DropInNavigator,
    viewState: PaymentMethodViewState,
    controller: CheckoutController,
) {
    DropInScaffold(
        navigationIcon = {
            IconButton(
                onClick = { navigator.back() },
            ) {
                if (navigator.isEmptyAfterCurrent()) {
                    Icon(Icons.Filled.Close, resolveString(CheckoutLocalizationKey.GENERAL_CLOSE))
                } else {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, resolveString(CheckoutLocalizationKey.GENERAL_BACK))
                }
            }
        },
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
                    // TODO - Pass amount as format arg
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

            // The action is no longer rendered here: the coordinator navigates to the action screen instead, so this
            // screen only renders the payment method itself.
            // TODO - Prototype: secondary content (installments, MBWay country picker) was handled by
            //  CheckoutPaymentFlow and is unavailable until a ModalBottomSheet hosting CheckoutSecondary is added.
            // TODO - Pass theme and localization provider
            CheckoutPaymentMethod(
                controller = controller,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.Spacing.Large),
            )
        }
    }
}

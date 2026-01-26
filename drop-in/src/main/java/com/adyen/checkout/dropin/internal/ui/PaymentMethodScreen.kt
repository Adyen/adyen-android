/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun PaymentMethodScreen(
    navigator: DropInNavigator,
    viewModel: PaymentMethodViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    PaymentMethodScreenContent(viewState)
}

@Composable
private fun PaymentMethodScreenContent(viewState: PaymentMethodViewState) {
    // TODO - Implement PaymentMethodScreenContent
}

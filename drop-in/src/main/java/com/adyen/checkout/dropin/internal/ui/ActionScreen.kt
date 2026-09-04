/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.adyen.checkout.core.components.CheckoutAction
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.dropin.internal.ui.progress.PaymentProgressContent
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun ActionScreen(
    navigator: DropInNavigator,
    viewModel: PaymentMethodViewModel,
) {
    val viewState by viewModel.actionViewState.collectAsStateWithLifecycle()
    ActionScreenContent(navigator, viewState, viewModel.controller)
}

// TODO - Improve the presentation of this screen when design is final
@Composable
private fun ActionScreenContent(
    navigator: DropInNavigator,
    viewState: ActionViewState,
    controller: CheckoutController,
) {
    DropInScaffold(
        navigationIcon = {
            IconButton(
                onClick = { navigator.back() },
            ) {
                Icon(Icons.Filled.Close, resolveString(CheckoutLocalizationKey.GENERAL_CLOSE))
            }
        },
        // TODO - Decide what the title of this screen should be. Empty for now, because the progress content below
        //  already names the payment method.
        title = "",
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimensions.Spacing.Large),
        ) {
            // The backdrop of the screen. A redirect and an authentication render nothing of their own, so without
            // this the shopper would be looking at a blank screen while the payment is being completed elsewhere.
            PaymentProgressContent(
                logoTxVariant = viewState.logoTxVariant,
                paymentMethodName = viewState.paymentMethodName,
                description = resolveString(CheckoutLocalizationKey.DROP_IN_ACTION_DESCRIPTION),
                progressTitle = resolveString(CheckoutLocalizationKey.DROP_IN_ACTION_PROCESSING),
            )

            // Drawn on top, because only some actions have content of their own.
            // TODO - Nothing tells us whether an action needs user interaction, so the two cannot be shown as either
            //  or: a 3DS2 challenge renders over the backdrop rather than replacing it. Needs a way to distinguish.
            CheckoutAction(
                controller = controller,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

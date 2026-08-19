/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.CheckoutAction
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.ui.internal.element.button.PrimaryButton
import com.adyen.checkout.ui.internal.theme.Dimensions

/**
 * Displays the action of the active payment flow.
 *
 * Leaving this screen cancels Drop-in: the action is reached through [DropInNavigator.clearAndNavigateTo], so going
 * back pops the only remaining key and finishes the flow with a cancellation result.
 */
@Composable
internal fun ActionScreen(
    navigator: DropInNavigator,
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
        // TODO - Prototype: the title and the navigation icon of the action screen are pending design.
        title = "",
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimensions.Spacing.Large),
        ) {
            // Every action renders its own content: a redirect shows a progress state, 3DS2 shows the challenge.
            CheckoutAction(
                controller = controller,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.weight(1f))

            // Cancelling has the same effect as the close icon: the action is the only key on the back stack, so going
            // back finishes Drop-in with a cancellation result.
            // TODO - Prototype: shown for every action, because CheckoutRoute.Action carries no action type. Only the
            //  redirect actually needs it.
            PrimaryButton(
                onClick = { navigator.back() },
                text = resolveString(CheckoutLocalizationKey.GENERAL_CANCEL),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.internal.ui.NavigationCloseButton
import com.adyen.checkout.core.components.CheckoutAction
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun ActionScreen(
    navigator: DropInNavigator,
    controller: CheckoutController,
) {
    DropInScaffold(
        navigationIcon = {
            NavigationCloseButton(onClick = { navigator.back() })
        },
    ) { innerPadding ->
        CheckoutAction(
            controller = controller,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimensions.Spacing.Large),
        )
    }
}

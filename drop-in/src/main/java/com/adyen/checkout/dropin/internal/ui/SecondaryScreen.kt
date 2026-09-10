/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/9/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutSecondary
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun SecondaryScreen(
    navigator: DropInNavigator,
    identifier: String,
    controller: CheckoutController,
    theme: CheckoutTheme,
) {
    DropInScaffold(
        navigationIcon = { PaymentMethodNavigationIcon(navigator) },
    ) { innerPadding ->
        CheckoutSecondary(
            identifier = identifier,
            controller = controller,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimensions.Spacing.Large),
            theme = theme,
        )
    }
}

/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutSecondary
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * Displays a secondary screen of a payment flow, identified by [identifier].
 */
@Composable
internal fun SecondaryScreen(
    navigator: DropInNavigator,
    identifier: String,
    title: String,
    controller: CheckoutController,
    theme: CheckoutTheme,
) {
    DropInScaffold(
        navigationIcon = {
            IconButton(
                onClick = { navigator.back() },
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, resolveString(CheckoutLocalizationKey.GENERAL_BACK))
            }
        },
        title = title,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // TODO - Pass localization provider
            CheckoutSecondary(
                identifier = identifier,
                controller = controller,
                theme = theme,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.Spacing.Large),
            )
        }
    }
}

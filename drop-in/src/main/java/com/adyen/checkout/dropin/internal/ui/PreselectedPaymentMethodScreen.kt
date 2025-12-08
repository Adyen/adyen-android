/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.element.button.PrimaryButton
import com.adyen.checkout.ui.internal.element.button.SecondaryButton
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.Title
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun PreselectedPaymentMethodScreen(
    backStack: SnapshotStateList<NavKey>,
    viewModel: PreselectedPaymentMethodViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    PreselectedPaymentMethodContent(backStack, viewState)
}

@Composable
private fun PreselectedPaymentMethodContent(
    backStack: SnapshotStateList<NavKey>,
    viewState: PreselectedPaymentMethodViewState,
) {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    Column(Modifier.fillMaxWidth()) {
        IconButton(
            onClick = { backPressedDispatcher?.onBackPressed() },
        ) {
            Icon(Icons.Default.Close, resolveString(CheckoutLocalizationKey.GENERAL_CLOSE))
        }

        Spacer(Modifier.size(Dimensions.ExtraLarge))

        CheckoutNetworkLogo(
            txVariant = viewState.logoTxVariant,
            contentDescription = null,
            modifier = Modifier
                .size(Dimensions.LogoSize.large)
                .align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.size(Dimensions.ExtraLarge))

        Title(
            text = viewState.title,
            modifier = Modifier
                .padding(horizontal = Dimensions.Large)
                .align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.size(Dimensions.Small))

        Body(
            text = viewState.subtitle,
            color = CheckoutThemeProvider.colors.textSecondary,
            modifier = Modifier
                .padding(horizontal = Dimensions.Large)
                .align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.size(Dimensions.Large))
        Spacer(Modifier.size(Dimensions.ExtraLarge))

        PrimaryButton(
            onClick = {},
            text = viewState.payButtonText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Large),
        )

        Spacer(Modifier.size(Dimensions.Large))

        SecondaryButton(
            onClick = {
                backStack.removeLastOrNull()
                backStack.add(PaymentMethodListNavKey)
            },
            text = resolveString(CheckoutLocalizationKey.DROP_IN_OTHER_PAYMENT_METHODS),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Large),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreselectedPaymentMethodScreenPreview() {
    val title = "Visa •••• 1234"
    val viewState = PreselectedPaymentMethodViewState(
        logoTxVariant = "visa",
        title = title,
        subtitle = "Use your Visa card to pay $9.99",
        payButtonText = "Use $title",
    )
    PreselectedPaymentMethodContent(
        backStack = remember { mutableStateListOf() },
        viewState = viewState,
    )
}

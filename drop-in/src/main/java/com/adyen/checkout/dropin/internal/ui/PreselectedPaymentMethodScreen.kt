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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.adyen.checkout.ui.internal.Body
import com.adyen.checkout.ui.internal.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.Dimensions
import com.adyen.checkout.ui.internal.PrimaryButton
import com.adyen.checkout.ui.internal.SecondaryButton
import com.adyen.checkout.ui.internal.Title

@Composable
internal fun PreselectedPaymentMethodScreen(
    backStack: NavBackStack<NavKey>,
) {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    Column(Modifier.fillMaxWidth()) {
        IconButton(
            onClick = { backPressedDispatcher?.onBackPressed() },
        ) {
            // TODO - String resources
            Icon(Icons.Default.Close, "Close")
        }

        // TODO - Center text
        Title(
            text = "Stored payment method",
            modifier = Modifier.padding(horizontal = Dimensions.Large),
        )

        Spacer(Modifier.size(Dimensions.Small))

        // TODO - Center text
        Body(
            text = "Description",
            color = CheckoutThemeProvider.colors.textSecondary,
            modifier = Modifier.padding(horizontal = Dimensions.Large),
        )

        Spacer(Modifier.size(Dimensions.Large))
        Spacer(Modifier.size(Dimensions.ExtraLarge))

        PrimaryButton(
            onClick = {},
            text = "Pay now",
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
            // TODO - String resources
            text = "Other payment solutions",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Large),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreselectedPaymentMethodScreenPreview() {
    PreselectedPaymentMethodScreen(
        backStack = rememberNavBackStack(),
    )
}

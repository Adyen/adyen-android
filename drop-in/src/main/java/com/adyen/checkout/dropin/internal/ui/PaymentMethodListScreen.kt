/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.element.ListItem
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.SubHeadlineEmphasized
import com.adyen.checkout.ui.internal.text.Title
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import java.util.Locale

@Composable
internal fun PaymentMethodListScreen(
    navigator: DropInNavigator,
    viewModel: PaymentMethodListViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    PaymentMethodListContent(navigator, viewState)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PaymentMethodListContent(
    navigator: DropInNavigator,
    viewState: PaymentMethodListViewState,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        containerColor = CheckoutThemeProvider.colors.background,
        topBar = {
            MediumFlexibleTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CheckoutThemeProvider.colors.background,
                    scrolledContainerColor = CheckoutThemeProvider.colors.background,
                    navigationIconContentColor = CheckoutThemeProvider.colors.text,
                ),
                title = {
                    Title(viewState.amount)
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navigator.back() },
                    ) {
                        Icon(Icons.Default.Close, resolveString(CheckoutLocalizationKey.GENERAL_CLOSE))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.size(Dimensions.ExtraSmall))

            // TODO - Extract to string resources
            Body(
                text = "Select your preferred payment option and complete your payment",
                color = CheckoutThemeProvider.colors.textSecondary,
                modifier = Modifier.padding(horizontal = Dimensions.Large),
            )

            Spacer(Modifier.size(Dimensions.Large))

            viewState.paymentOptionsSection?.let { PaymentOptionsSection(it) }
        }
    }
}

@Composable
private fun PaymentOptionsSection(
    paymentOptionsSection: PaymentOptionsSection,
) {
    Column {
        SubHeadlineEmphasized(
            text = paymentOptionsSection.title,
            modifier = Modifier.padding(horizontal = Dimensions.Large),
        )

        Spacer(Modifier.size(Dimensions.Small))

        paymentOptionsSection.options.forEach { item ->
            ListItem(
                leadingIcon = {
                    CheckoutNetworkLogo(
                        txVariant = item.icon,
                        modifier = Modifier.size(Dimensions.LogoSize.large),
                    )
                },
                title = item.title,
                onClick = {},
                modifier = Modifier.padding(Dimensions.ExtraSmall),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentMethodListContentPreview() {
    CheckoutCompositionLocalProvider(
        locale = Locale.getDefault(),
        localizationProvider = null,
        environment = Environment.TEST,
    ) {
        val paymentMethods = listOf(
            PaymentMethodItem(
                icon = "card",
                title = "Cards",
            ),
            PaymentMethodItem(
                icon = "klarna",
                title = "Klarna pay in 30 days",
            ),
            PaymentMethodItem(
                icon = "ideal",
                title = "iDEAL",
            ),
        )
        PaymentMethodListContent(
            navigator = DropInNavigator(),
            viewState = PaymentMethodListViewState(
                amount = "$140.38",
                paymentOptionsSection = PaymentOptionsSection(
                    title = "Payment options",
                    options = paymentMethods,
                ),
            ),
        )
    }
}

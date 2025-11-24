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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodListContent(
    navigator: DropInNavigator,
    viewState: PaymentMethodListViewState,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        containerColor = CheckoutThemeProvider.colors.background,
        topBar = {
            DropInTopAppBar(
                title = viewState.amount,
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

            Body(
                text = resolveString(CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_DESCRIPTION),
                color = CheckoutThemeProvider.colors.textSecondary,
                modifier = Modifier.padding(horizontal = Dimensions.Large),
            )

            Spacer(Modifier.size(Dimensions.Large))

            viewState.favoritesSection?.let {
                FavoritesSection(it)

                Spacer(Modifier.size(Dimensions.Small))
            }

            viewState.paymentOptionsSection?.let { PaymentOptionsSection(it) }
        }
    }
}

@Composable
private fun FavoritesSection(
    favoritesSection: FavoritesSection,
) {
    Column {
        SubHeadlineEmphasized(
            text = "Favorites",
            modifier = Modifier.padding(horizontal = Dimensions.Large),
        )

        Spacer(Modifier.size(Dimensions.Small))

        favoritesSection.options.forEach { item ->
            ListItem(
                leadingIcon = {
                    CheckoutNetworkLogo(
                        txVariant = item.icon,
                        modifier = Modifier.size(Dimensions.LogoSize.large),
                    )
                },
                title = item.title,
                subtitle = item.subtitle,
                onClick = {},
                modifier = Modifier.padding(Dimensions.ExtraSmall),
            )
        }
    }
}

@Composable
private fun PaymentOptionsSection(
    paymentOptionsSection: PaymentOptionsSection,
) {
    Column {
        SubHeadlineEmphasized(
            text = resolveString(paymentOptionsSection.title),
            modifier = Modifier.padding(horizontal = Dimensions.Large),
        )

        Spacer(Modifier.size(Dimensions.Small))

        paymentOptionsSection.options.forEach { item ->
            ListItem(
                leadingIcon = {
                    CheckoutNetworkLogo(
                        txVariant = item.icon,
                        modifier = Modifier.size(Dimensions.LogoSize.medium),
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
        val storedPaymentMethods = listOf(
            PaymentMethodItem(
                icon = "mc",
                title = "Mastercard •••• 0023",
                subtitle = "AAdvantage card",
            ),
            PaymentMethodItem(
                icon = "wechat",
                title = "@someName",
                subtitle = "WeChat Pay",
            ),
        )

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
                favoritesSection = FavoritesSection(
                    options = storedPaymentMethods,
                ),
                paymentOptionsSection = PaymentOptionsSection(
                    title = CheckoutLocalizationKey.DROP_IN_PAYMENT_OPTIONS,
                    options = paymentMethods,
                ),
            ),
        )
    }
}

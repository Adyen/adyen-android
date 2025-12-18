/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/12/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.dropin.internal.ui.ManageFavoritesViewState.FavoriteListItem
import com.adyen.checkout.ui.internal.element.ListItem
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.SubHeadlineEmphasized
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import java.util.Locale

@Composable
internal fun ManageFavoritesScreen(
    navigator: DropInNavigator,
    viewModel: ManageFavoritesViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    ManageFavoritesContent(
        navigator = navigator,
        viewState = viewState,
    )
}

@Composable
private fun ManageFavoritesContent(
    navigator: DropInNavigator,
    viewState: ManageFavoritesViewState,
) {
    DropInScaffold(
        navigationIcon = {
            IconButton(
                onClick = { navigator.back() },
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, resolveString(CheckoutLocalizationKey.GENERAL_BACK))
            }
        },
        title = resolveString(CheckoutLocalizationKey.DROP_IN_MANAGE_FAVORITES_TITLE),
    ) {
        Spacer(Modifier.size(Dimensions.ExtraLarge))

        Section(
            title = "Cards",
            favorites = viewState.cards,
            onItemClick = {},
        )

        Section(
            title = "Others",
            favorites = viewState.others,
            onItemClick = {},
        )
    }
}

@Composable
private fun Section(
    title: String,
    favorites: List<FavoriteListItem>,
    onItemClick: (FavoriteListItem) -> Unit,
) {
    favorites.forEachIndexed { index, item ->
        if (index == 0) {
            SubHeadlineEmphasized(
                text = title,
                modifier = Modifier.padding(horizontal = Dimensions.Large, vertical = Dimensions.Small),
            )
        }

        ListItem(
            leadingIcon = {
                CheckoutNetworkLogo(
                    txVariant = item.icon,
                    modifier = Modifier.size(Dimensions.LogoSize.medium),
                )
            },
            title = item.title,
            subtitle = item.subtitle,
            trailingIcon = { Body(text = "Remove", color = CheckoutThemeProvider.colors.destructive) },
            onClick = { onItemClick(item) },
            modifier = Modifier.padding(horizontal = Dimensions.ExtraSmall),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageFavoritesContentPreview() {
    CheckoutCompositionLocalProvider(
        locale = Locale.getDefault(),
        localizationProvider = null,
        environment = Environment.TEST,
    ) {
        val viewState = ManageFavoritesViewState(
            cards = listOf(
                FavoriteListItem(
                    icon = "mc",
                    title = "•••• 1234",
                    subtitle = "Mastercard",
                ),
                FavoriteListItem(
                    icon = "visa",
                    title = "•••• 2567",
                    subtitle = "Visa",
                ),
            ),
            others = listOf(
                FavoriteListItem(
                    icon = "paybybank",
                    title = "•••• 1234",
                    subtitle = "WELLS FARGO",
                ),
                FavoriteListItem(
                    icon = "wechatpay",
                    title = "@someName",
                    subtitle = "WeChat Pay",
                ),
            ),
        )

        ManageFavoritesContent(
            navigator = DropInNavigator(),
            viewState = viewState,
        )
    }
}

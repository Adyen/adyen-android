/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/12/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.dropin.internal.ui.StoredPaymentMethodsViewState.StoredPaymentMethodsListItem
import com.adyen.checkout.ui.internal.element.ListItem
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.SubHeadlineEmphasized
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import java.util.Locale

@Composable
internal fun StoredPaymentMethodsScreen(
    navigator: DropInNavigator,
    viewModel: StoredPaymentMethodsViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    StoredPaymentMethodsContent(
        navigator = navigator,
        viewState = viewState,
        onRemoveItem = { viewModel.removeStoredPaymentMethod(it.id) },
    )
}

@Composable
private fun StoredPaymentMethodsContent(
    navigator: DropInNavigator,
    viewState: StoredPaymentMethodsViewState,
    onRemoveItem: (StoredPaymentMethodsListItem) -> Unit,
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
    ) { innerPadding ->
        var selectedItem by rememberSaveable { mutableStateOf<StoredPaymentMethodsListItem?>(null) }

        LazyColumn(
            contentPadding = PaddingValues(top = Dimensions.Spacing.ExtraLarge),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            section(
                title = CheckoutLocalizationKey.DROP_IN_MANAGE_FAVORITES_CARDS_SECTION_TITLE,
                storedPaymentMethods = viewState.cards,
                onItemClick = { selectedItem = it },
            )

            section(
                title = CheckoutLocalizationKey.DROP_IN_MANAGE_FAVORITES_OTHERS_SECTION_TITLE,
                storedPaymentMethods = viewState.others,
                onItemClick = { selectedItem = it },
            )
        }

        selectedItem?.let { item ->
            ConfirmationDialog(
                // TODO - Pass item title as parameter after we resolve parameterized strings
                confirmationText = resolveString(CheckoutLocalizationKey.DROP_IN_MANAGE_FAVORITES_REMOVE_CONFIRMATION),
                onConfirmationClick = {
                    onRemoveItem(item)
                    selectedItem = null
                },
                cancellationText = resolveString(CheckoutLocalizationKey.GENERAL_CANCEL),
                onDismissRequest = { selectedItem = null },
            )
        }
    }
}

private fun LazyListScope.section(
    title: CheckoutLocalizationKey,
    storedPaymentMethods: List<StoredPaymentMethodsListItem>,
    onItemClick: (StoredPaymentMethodsListItem) -> Unit,
) {
    if (storedPaymentMethods.isNotEmpty()) {
        item(key = title) {
            SubHeadlineEmphasized(
                text = resolveString(title),
                modifier = Modifier
                    .padding(horizontal = Dimensions.Spacing.Large, vertical = Dimensions.Spacing.Small)
                    .animateItem(),
            )
        }
    }

    storedPaymentMethods.forEach { item ->
        item(key = item.id) {
            StoredPaymentMethodListItem(
                item = item,
                onClick = onItemClick,
                modifier = Modifier
                    .padding(horizontal = Dimensions.Spacing.ExtraSmall)
                    .animateItem(),
            )
        }
    }
}

@Composable
private fun StoredPaymentMethodListItem(
    item: StoredPaymentMethodsListItem,
    onClick: (StoredPaymentMethodsListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        leadingIcon = {
            CheckoutNetworkLogo(
                txVariant = item.icon,
                modifier = Modifier.size(Dimensions.LogoSize.medium),
            )
        },
        title = item.title,
        subtitle = item.subtitle,
        trailingContent = {
            Body(
                text = resolveString(CheckoutLocalizationKey.DROP_IN_MANAGE_FAVORITES_REMOVE),
                color = CheckoutThemeProvider.colors.destructive,
            )
        },
        onClick = { onClick(item) },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun StoredPaymentMethodsContentPreview() {
    CheckoutCompositionLocalProvider(
        locale = Locale.getDefault(),
        localizationProvider = null,
        environment = Environment.TEST,
    ) {
        val viewState = StoredPaymentMethodsViewState(
            cards = listOf(
                StoredPaymentMethodsListItem(
                    id = "1",
                    icon = "mc",
                    title = "•••• 1234",
                    subtitle = "Mastercard",
                ),
                StoredPaymentMethodsListItem(
                    id = "2",
                    icon = "visa",
                    title = "•••• 2567",
                    subtitle = "Visa",
                ),
            ),
            others = listOf(
                StoredPaymentMethodsListItem(
                    id = "3",
                    icon = "paybybank",
                    title = "•••• 1234",
                    subtitle = "WELLS FARGO",
                ),
                StoredPaymentMethodsListItem(
                    id = "4",
                    icon = "wechatpay",
                    title = "@someName",
                    subtitle = "WeChat Pay",
                ),
            ),
        )

        StoredPaymentMethodsContent(
            navigator = DropInNavigator(),
            viewState = viewState,
            onRemoveItem = {},
        )
    }
}

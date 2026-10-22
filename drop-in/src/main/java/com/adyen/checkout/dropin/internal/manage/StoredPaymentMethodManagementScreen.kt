/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/10/2026.
 */

package com.adyen.checkout.dropin.internal.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.internal.ui.NavigationBackButton
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.dropin.internal.helper.SavedStateBackStackPersister
import com.adyen.checkout.dropin.internal.ui.ConfirmationDialog
import com.adyen.checkout.dropin.internal.ui.DropInNavigator
import com.adyen.checkout.dropin.internal.ui.DropInScaffold
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.element.ListItem
import com.adyen.checkout.ui.internal.element.ProgressBar
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.SubHeadlineEmphasized
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun StoredPaymentMethodManagementScreen(
    navigator: DropInNavigator,
    viewModel: StoredPaymentMethodManagementViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    StoredPaymentMethodManagementContent(
        navigator = navigator,
        viewState = viewState,
        onRemoveItem = { viewModel.removeStoredPaymentMethod(it.id) },
    )
}

@Composable
private fun StoredPaymentMethodManagementContent(
    navigator: DropInNavigator,
    viewState: StoredPaymentMethodManagementViewState,
    onRemoveItem: (StoredPaymentMethodManagementViewState.ListItem) -> Unit,
) {
    DropInScaffold(
        navigationIcon = {
            NavigationBackButton(onClick = { navigator.back() })
        },
        title = resolveString(CheckoutLocalizationKey.DROP_IN_STORED_PAYMENT_METHOD_MANAGEMENT_TITLE),
    ) { innerPadding ->
        var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
        val listState = rememberLazyListState()

        // The error is the first item after the description, so scroll up to make sure it is seen.
        LaunchedEffect(viewState.showRemovalError) {
            if (viewState.showRemovalError) listState.animateScrollToItem(0)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item {
                Body(
                    text = resolveString(CheckoutLocalizationKey.DROP_IN_STORED_PAYMENT_METHOD_MANAGEMENT_DESCRIPTION),
                    color = CheckoutThemeProvider.colors.textSecondary,
                    modifier = Modifier
                        .padding(
                            start = Dimensions.Spacing.Large,
                            end = Dimensions.Spacing.Large,
                            bottom = Dimensions.Spacing.Large,
                        ),
                )
            }

            if (viewState.showRemovalError) removalError()

            section(
                title = CheckoutLocalizationKey.DROP_IN_STORED_PAYMENT_METHOD_MANAGEMENT_CARDS_SECTION_TITLE,
                storedPaymentMethods = viewState.cards,
                onItemClick = { selectedItemId = it.id },
            )

            section(
                title = CheckoutLocalizationKey.DROP_IN_STORED_PAYMENT_METHOD_MANAGEMENT_OTHERS_SECTION_TITLE,
                storedPaymentMethods = viewState.others,
                onItemClick = { selectedItemId = it.id },
            )
        }

        // Looked up from the view state, so the dialog closes if the item is no longer listed.
        val selectedItem = (viewState.cards + viewState.others).firstOrNull { it.id == selectedItemId }
        selectedItem?.let { item ->
            ConfirmationDialog(
                confirmationText = resolveString(
                    CheckoutLocalizationKey.DROP_IN_STORED_PAYMENT_METHOD_MANAGEMENT_REMOVE_CONFIRMATION,
                    item.title,
                ),
                onConfirmationClick = {
                    onRemoveItem(item)
                    selectedItemId = null
                },
                cancellationText = resolveString(CheckoutLocalizationKey.GENERAL_CANCEL),
                onDismissRequest = { selectedItemId = null },
            )
        }
    }
}

private const val REMOVAL_ERROR_KEY = "removal_error"

private fun LazyListScope.removalError() {
    item(key = REMOVAL_ERROR_KEY) {
        val color = CheckoutThemeProvider.colors.destructive
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
            modifier = Modifier
                .padding(
                    start = Dimensions.Spacing.Large,
                    end = Dimensions.Spacing.Large,
                    bottom = Dimensions.Spacing.Large,
                )
                .animateItem(),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_warning),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(Dimensions.IconSize.small),
            )
            Body(
                text = resolveString(CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_MANAGEMENT_REMOVAL_ERROR),
                color = color,
            )
        }
    }
}

private fun LazyListScope.section(
    title: CheckoutLocalizationKey,
    storedPaymentMethods: List<StoredPaymentMethodManagementViewState.ListItem>,
    onItemClick: (StoredPaymentMethodManagementViewState.ListItem) -> Unit,
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
    item: StoredPaymentMethodManagementViewState.ListItem,
    onClick: (StoredPaymentMethodManagementViewState.ListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        leadingIcon = {
            if (item.isRemoving) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(Dimensions.LogoSize.medium),
                ) {
                    ProgressBar(modifier = Modifier.size(Dimensions.LogoSize.medium.height))
                }
            } else {
                CheckoutNetworkLogo(
                    txVariant = item.icon,
                    modifier = Modifier.size(Dimensions.LogoSize.medium),
                )
            }
        },
        title = item.title,
        subtitle = item.subtitle,
        trailingContent = {
            val colors = CheckoutThemeProvider.colors
            Body(
                text = resolveString(CheckoutLocalizationKey.DROP_IN_STORED_PAYMENT_METHOD_MANAGEMENT_REMOVE),
                color = if (item.isRemoving) colors.textOnDisabled else colors.destructive,
            )
        },
        onClick = { onClick(item) },
        modifier = modifier,
        // An item that is being removed can't be removed again, so its confirmation dialog doesn't open.
        enabled = !item.isRemoving,
    )
}

@Preview(showBackground = true)
@Composable
private fun StoredPaymentMethodManagementContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        val viewState = StoredPaymentMethodManagementViewState(
            cards = listOf(
                StoredPaymentMethodManagementViewState.ListItem(
                    id = "1",
                    icon = "mc",
                    title = "•••• 1234",
                    subtitle = "Mastercard",
                    isRemoving = false,
                ),
                StoredPaymentMethodManagementViewState.ListItem(
                    id = "2",
                    icon = "visa",
                    title = "•••• 2567",
                    subtitle = "Visa",
                    isRemoving = true,
                ),
            ),
            others = listOf(
                StoredPaymentMethodManagementViewState.ListItem(
                    id = "3",
                    icon = "paybybank",
                    title = "•••• 1234",
                    subtitle = "WELLS FARGO",
                    isRemoving = false,
                ),
                StoredPaymentMethodManagementViewState.ListItem(
                    id = "4",
                    icon = "wechatpay",
                    title = "@someName",
                    subtitle = "WeChat Pay",
                    isRemoving = false,
                ),
            ),
            showRemovalError = true,
        )

        val persister = SavedStateBackStackPersister(SavedStateHandle())
        StoredPaymentMethodManagementContent(
            navigator = DropInNavigator(persister),
            viewState = viewState,
            onRemoveItem = {},
        )
    }
}

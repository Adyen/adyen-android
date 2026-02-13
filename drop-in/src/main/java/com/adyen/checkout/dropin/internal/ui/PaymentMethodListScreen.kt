/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.dropin.internal.ui.PaymentMethodListViewState.PaymentMethodItem
import com.adyen.checkout.ui.internal.element.ListItem
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.BodyEmphasized
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
    PaymentMethodListContent(navigator, viewState, navigator::navigateTo)
}

@Composable
private fun PaymentMethodListContent(
    navigator: DropInNavigator,
    viewState: PaymentMethodListViewState,
    onPaymentMethodClick: (PaymentMethodNavKey) -> Unit,
) {
    DropInScaffold(
        navigationIcon = {
            IconButton(
                onClick = { navigator.back() },
            ) {
                Icon(Icons.Default.Close, resolveString(CheckoutLocalizationKey.GENERAL_CLOSE))
            }
        },
        title = viewState.amount,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Body(
                text = resolveString(CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_DESCRIPTION),
                color = CheckoutThemeProvider.colors.textSecondary,
                modifier = Modifier
                    .padding(
                        start = Dimensions.Spacing.Large,
                        top = Dimensions.Spacing.ExtraSmall,
                        end = Dimensions.Spacing.Large,
                        bottom = Dimensions.Spacing.Medium,
                    ),
            )

            viewState.storedPaymentMethodSection?.let {
                Section(
                    title = it.title,
                    actionText = it.action,
                    items = it.options,
                    onActionClick = { navigator.navigateTo(StoredPaymentMethodsNavKey) },
                    onPaymentMethodClick = { pm ->
                        onPaymentMethodClick(PaymentMethodNavKey(DropInPaymentFlowType.StoredPaymentMethod(pm.id)))
                    },
                )

                Spacer(Modifier.size(Dimensions.Spacing.Small))
            }

            viewState.paymentOptionsSection?.let {
                Section(
                    title = it.title,
                    actionText = it.action,
                    items = it.options,
                    onPaymentMethodClick = { pm ->
                        onPaymentMethodClick(PaymentMethodNavKey(DropInPaymentFlowType.RegularPaymentMethod(pm.id)))
                    },
                )
            }
        }
    }
}

@Composable
private fun Section(
    title: CheckoutLocalizationKey,
    actionText: CheckoutLocalizationKey?,
    items: List<PaymentMethodItem>,
    onPaymentMethodClick: (PaymentMethodItem) -> Unit,
    onActionClick: (() -> Unit)? = null,
) {
    Column {
        SectionHeader(
            title = resolveString(title),
            actionText = actionText?.let { resolveString(it) },
            onActionClick = onActionClick,
        )

        PaymentMethodItemList(
            paymentMethodItems = items,
            onItemClick = onPaymentMethodClick,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        SubHeadlineEmphasized(
            text = title,
            modifier = Modifier.padding(horizontal = Dimensions.Spacing.Large, vertical = Dimensions.Spacing.Medium),
        )

        actionText?.let {
            TextButton(
                text = actionText,
                onClick = { onActionClick?.invoke() },
            )
        }
    }
}

@Composable
private fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BodyEmphasized(
        text = text,
        color = CheckoutThemeProvider.colors.highlight,
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = null,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = Dimensions.Spacing.Large, vertical = Dimensions.Spacing.Medium),
    )
}

@Composable
private fun PaymentMethodItemList(
    paymentMethodItems: List<PaymentMethodItem>,
    onItemClick: (PaymentMethodItem) -> Unit,
) {
    Column {
        paymentMethodItems.forEach { item ->
            ListItem(
                leadingIcon = {
                    CheckoutNetworkLogo(
                        txVariant = item.icon,
                        modifier = Modifier.size(Dimensions.LogoSize.medium),
                    )
                },
                title = item.title,
                subtitle = item.subtitle,
                onClick = { onItemClick(item) },
                modifier = Modifier.padding(horizontal = Dimensions.Spacing.ExtraSmall),
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
                id = "advantage",
                icon = "mc",
                title = "•••• 0023",
                subtitle = "AAdvantage card",
            ),
            PaymentMethodItem(
                id = "wechat",
                icon = "wechat",
                title = "@someName",
                subtitle = "WeChat Pay",
            ),
        )

        val paymentMethods = listOf(
            PaymentMethodItem(
                id = "scheme",
                icon = "card",
                title = "Cards",
            ),
            PaymentMethodItem(
                id = "klarna",
                icon = "klarna",
                title = "Klarna pay in 30 days",
            ),
            PaymentMethodItem(
                id = "ideal",
                icon = "ideal",
                title = "iDEAL",
            ),
        )

        val paymentOptionsTitle =
            CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_PAYMENT_OPTIONS_SECTION_TITLE_WITH_FAVORITES
        PaymentMethodListContent(
            navigator = DropInNavigator(),
            viewState = PaymentMethodListViewState(
                amount = "$140.38",
                storedPaymentMethodSection = PaymentMethodListViewState.PaymentMethodListSection(
                    title = CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_FAVORITES_SECTION_TITLE,
                    action = CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_FAVORITES_SECTION_ACTION,
                    options = storedPaymentMethods,
                ),
                paymentOptionsSection = PaymentMethodListViewState.PaymentMethodListSection(
                    title = paymentOptionsTitle,
                    action = null,
                    options = paymentMethods,
                ),
            ),
            onPaymentMethodClick = {},
        )
    }
}

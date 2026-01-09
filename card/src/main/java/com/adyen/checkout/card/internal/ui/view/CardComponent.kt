/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.state.isAmex
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.element.SwitchContainer
import com.adyen.checkout.ui.internal.element.button.PayButton
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun CardComponent(
    viewState: CardViewState,
    onIntent: (CardIntent) -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ComponentScaffold(
        modifier = modifier,
        footer = {
            PayButton(onClick = onSubmitClick, isLoading = viewState.isLoading)
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.ExtraLarge),
        ) {
            CardDetailsSection(
                viewState = viewState,
                onIntent = onIntent,
            )

            viewState.dualBrandData?.let { dualBrandData ->
                DualBrandSelector(
                    dualBrandData = dualBrandData,
                    onBrandSelected = { onIntent(CardIntent.SelectBrand(it)) },
                )
            }
        }
    }
}

@Composable
private fun CardDetailsSection(
    viewState: CardViewState,
    onIntent: (CardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
    ) {
        CardNumberField(
            cardNumberState = viewState.cardNumber,
            supportedCardBrands = viewState.supportedCardBrands,
            isSupportedCardBrandsShown = viewState.isSupportedCardBrandsShown,
            detectedCardBrands = viewState.detectedCardBrands,
            isAmex = viewState.isAmex,
            onIntent = onIntent,
        )
        if (viewState.expiryDate != null) {
            ExpiryDateField(
                expiryDateState = viewState.expiryDate,
                onIntent = onIntent,
            )
        }
        if (viewState.securityCode != null) {
            SecurityCodeField(
                securityCodeState = viewState.securityCode,
                isAmex = viewState.isAmex,
                onIntent = onIntent,
            )
        }
        if (viewState.holderName != null) {
            HolderNameField(
                holderNameState = viewState.holderName,
                onIntent = onIntent,
            )
        }
        if (viewState.isStorePaymentFieldVisible) {
            SwitchContainer(
                checked = viewState.storePaymentMethod,
                onCheckedChange = { onIntent(CardIntent.UpdateStorePaymentMethod(it)) },
            ) {
                Body(resolveString(CheckoutLocalizationKey.CARD_STORE_PAYMENT_METHOD))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CardComponentPreview() {
    CardComponent(
        viewState = CardViewState(
            cardNumber = TextInputViewState(
                text = "5555444433331111",
            ),
            expiryDate = TextInputViewState(
                text = "12/34",
            ),
            securityCode = TextInputViewState(
                text = "737",
            ),
            holderName = TextInputViewState(
                text = "J. Smith",
            ),
            storePaymentMethod = false,
            isStorePaymentFieldVisible = true,
            supportedCardBrands = emptyList(),
            isSupportedCardBrandsShown = false,
            isLoading = false,
            detectedCardBrands = listOf(CardBrand(CardType.MASTERCARD.txVariant)),
            dualBrandData = null,
        ),
        onIntent = {},
        onSubmitClick = {},
    )
}

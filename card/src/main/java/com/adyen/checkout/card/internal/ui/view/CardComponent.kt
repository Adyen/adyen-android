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
import com.adyen.checkout.card.internal.ui.state.CardChangeListener
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.state.isAmex
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState
import com.adyen.checkout.ui.internal.ComponentScaffold
import com.adyen.checkout.ui.internal.Dimensions
import com.adyen.checkout.ui.internal.PayButton

@Composable
internal fun CardComponent(
    viewState: CardViewState,
    changeListener: CardChangeListener,
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
            verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
        ) {
            CardNumberField(
                cardNumberState = viewState.cardNumber,
                supportedCardBrands = viewState.supportedCardBrands,
                isSupportedCardBrandsShown = viewState.isSupportedCardBrandsShown,
                detectedCardBrands = viewState.detectedCardBrands,
                isAmex = viewState.isAmex,
                onCardNumberChanged = changeListener::onCardNumberChanged,
                onCardNumberFocusChanged = changeListener::onCardNumberFocusChanged,
            )
            ExpiryDateField(
                expiryDateState = viewState.expiryDate,
                onExpiryDateChanged = changeListener::onExpiryDateChanged,
                onExpiryDateFocusChanged = changeListener::onExpiryDateFocusChanged,
            )
            SecurityCodeField(
                securityCodeState = viewState.securityCode,
                onSecurityCodeChanged = changeListener::onSecurityCodeChanged,
                onSecurityCodeFocusChanged = changeListener::onSecurityCodeFocusChanged,
                isAmex = viewState.isAmex,
            )
        }
    }
    // TODO - Card Full UI
}

@Preview(showBackground = true)
@Composable
private fun CardComponentPreview() {
    CardComponent(
        viewState = CardViewState(
            cardNumber = TextInputState(
                "5555444433331111",
            ),
            expiryDate = TextInputState(
                text = "12/34",
            ),
            securityCode = TextInputState(
                text = "737",
            ),
            supportedCardBrands = emptyList(),
            isSupportedCardBrandsShown = false,
            isLoading = false,
            detectedCardBrands = listOf(CardBrand(CardType.MASTERCARD.txVariant)),
            dualBrandData = null,
        ),
        changeListener = object : CardChangeListener {
            override fun onCardNumberChanged(newCardNumber: String) = Unit

            override fun onCardNumberFocusChanged(hasFocus: Boolean) = Unit

            override fun onExpiryDateChanged(newExpiryDate: String) = Unit

            override fun onExpiryDateFocusChanged(hasFocus: Boolean) = Unit

            override fun onSecurityCodeChanged(newSecurityCode: String) = Unit

            override fun onSecurityCodeFocusChanged(hasFocus: Boolean) = Unit
        },
        onSubmitClick = {},
    )
}

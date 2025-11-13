/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/11/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.adyen.checkout.card.R
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.helper.CardNumberValidator
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState
import com.adyen.checkout.ui.internal.CheckoutTextField
import com.adyen.checkout.ui.internal.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.Dimensions

@Composable
internal fun CardNumberField(
    cardNumberState: TextInputState,
    supportedCardBrands: List<CardBrand>,
    isSupportedCardBrandsShown: Boolean,
    detectedBrand: CardBrand?,
    isAmex: Boolean?,
    onCardNumberChanged: (String) -> Unit,
    onCardNumberFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        CardNumberInputField(
            cardNumberState = cardNumberState,
            isAmex = isAmex,
            detectedBrand = detectedBrand,
            onCardNumberChanged = onCardNumberChanged,
            onCardNumberFocusChanged = onCardNumberFocusChanged,
        )

        CardBrandsList(
            cardBrands = supportedCardBrands,
            visible = isSupportedCardBrandsShown,
        )
    }
}

@Composable
private fun CardNumberInputField(
    cardNumberState: TextInputState,
    isAmex: Boolean?,
    detectedBrand: CardBrand?,
    onCardNumberChanged: (String) -> Unit,
    onCardNumberFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showCardNumberError =
        cardNumberState.errorMessage != null && cardNumberState.showError
    val supportingTextCardNumber = if (showCardNumberError) {
        cardNumberState.errorMessage?.let { resolveString(it) }
    } else {
        null
    }

    val outputTransformation = remember(isAmex) {
        CardNumberOutputTransformation(isAmex = isAmex ?: false)
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onCardNumberFocusChanged(focusState.isFocused)
            },
        label = resolveString(CheckoutLocalizationKey.CARD_NUMBER),
        initialValue = cardNumberState.text,
        isError = showCardNumberError,
        supportingText = supportingTextCardNumber,
        onValueChange = { value ->
            onCardNumberChanged(value)
        },
        inputTransformation = DigitOnlyInputTransformation().maxLength(
            maxLength = CardNumberValidator.MAXIMUM_CARD_NUMBER_LENGTH,
        ),
        outputTransformation = outputTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shouldFocus = cardNumberState.isFocused,
        trailingIcon = {
            CheckoutNetworkLogo(
                modifier = Modifier
                    .height(16.dp)
                    .width(24.dp)
                    .clip(RoundedCornerShape(Dimensions.CornerRadius)),
                txVariant = detectedBrand?.txVariant.orEmpty(),
                placeholder = R.drawable.ic_card_placeholder,
                errorFallback = R.drawable.ic_card_placeholder,
            )
        },
    )
}

@Composable
private fun CardBrandsList(
    cardBrands: List<CardBrand>,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = visible,
    ) {
        FlowRow(
            modifier = Modifier.padding(top = Dimensions.ExtraSmall),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.ExtraSmall),
            verticalArrangement = Arrangement.spacedBy(Dimensions.ExtraSmall),
        ) {
            for (cardBrand in cardBrands) {
                CheckoutNetworkLogo(
                    modifier = Modifier
                        .size(24.dp, 16.dp)
                        .dropShadow(
                            shape = RoundedCornerShape(Dimensions.CornerRadius),
                            shadow = Shadow(
                                radius = 1.dp,
                                offset = DpOffset(x = 0.dp, 2.dp),
                                color = CheckoutThemeProvider.colors.container,
                            ),
                        )
                        .clip(RoundedCornerShape(Dimensions.CornerRadius)),
                    txVariant = cardBrand.txVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CardNumberFieldPreview() {
    CardNumberField(
        cardNumberState = TextInputState(
            "5555444433331111",
        ),
        supportedCardBrands = listOf(
            CardBrand(CardType.MASTERCARD.txVariant),
            CardBrand(CardType.VISA.txVariant),
            CardBrand(CardType.AMERICAN_EXPRESS.txVariant),
        ),
        isSupportedCardBrandsShown = true,
        detectedBrand = CardBrand(CardType.MASTERCARD.txVariant),
        isAmex = false,
        onCardNumberChanged = {},
        onCardNumberFocusChanged = {},
    )
}

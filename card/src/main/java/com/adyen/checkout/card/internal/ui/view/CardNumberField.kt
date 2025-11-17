/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/11/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.card.R
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.helper.CardNumberValidator
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun CardNumberField(
    cardNumberState: TextInputState,
    supportedCardBrands: List<CardBrand>,
    isSupportedCardBrandsShown: Boolean,
    detectedCardBrands: List<CardBrand>,
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
            detectedCardBrands = detectedCardBrands,
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
    detectedCardBrands: List<CardBrand>,
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
            CardNumberFieldIcon(state = cardNumberState, detectedBrands = detectedCardBrands)
        },
    )
}

@Composable
private fun DetectedBrandsList(
    detectedCardBrands: List<CardBrand>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.ExtraSmall),
    ) {
        if (detectedCardBrands.isEmpty()) {
            BrandLogo(txVariant = null)
        } else {
            detectedCardBrands.take(2).forEach { cardBrand ->
                BrandLogo(cardBrand.txVariant)
            }
        }
    }
}

@Composable
private fun BrandLogo(
    txVariant: String?,
    modifier: Modifier = Modifier,
) {
    CheckoutNetworkLogo(
        modifier = modifier.size(Dimensions.LogoSize.small),
        txVariant = txVariant.orEmpty(),
        placeholder = R.drawable.ic_card_placeholder,
        errorFallback = R.drawable.ic_card_placeholder,
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
                BrandLogo(cardBrand.txVariant)
            }
        }
    }
}

@Composable
private fun CardNumberFieldIcon(
    state: TextInputState,
    detectedBrands: List<CardBrand>,
    modifier: Modifier = Modifier,
) {
    val isInvalid = state.errorMessage != null && state.showError

    AnimatedContent(targetState = isInvalid, modifier = modifier) { isInvalid ->
        if (isInvalid) {
            Icon(
                imageVector = ImageVector.vectorResource(com.adyen.checkout.test.R.drawable.ic_warning),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        } else {
            DetectedBrandsList(detectedBrands)
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
        detectedCardBrands = listOf(CardBrand(CardType.MASTERCARD.txVariant)),
        isAmex = false,
        onCardNumberChanged = {},
        onCardNumberFocusChanged = {},
    )
}

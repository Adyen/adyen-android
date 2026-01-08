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
import com.adyen.checkout.card.internal.ui.model.CardNumberTrailingIcon
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.helper.CardNumberValidator
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.helper.getThemedIcon
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun CardNumberField(
    cardNumberState: TextInputViewState,
    supportedCardBrands: List<CardBrand>,
    isSupportedCardBrandsShown: Boolean,
    detectedCardBrands: List<CardBrand>,
    isAmex: Boolean?,
    onIntent: (CardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        CardNumberInputField(
            cardNumberState = cardNumberState,
            isAmex = isAmex,
            detectedCardBrands = detectedCardBrands,
            onIntent = onIntent,
        )

        CardBrandsList(
            cardBrands = supportedCardBrands,
            visible = isSupportedCardBrandsShown,
        )
    }
}

@Composable
private fun CardNumberInputField(
    cardNumberState: TextInputViewState,
    isAmex: Boolean?,
    detectedCardBrands: List<CardBrand>,
    onIntent: (CardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingTextCardNumber = cardNumberState.supportingText?.let { resolveString(it) }

    val outputTransformation = remember(isAmex) {
        CardNumberOutputTransformation(isAmex = isAmex ?: false)
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onIntent(CardIntent.UpdateCardNumberFocus(focusState.isFocused))
            },
        label = resolveString(CheckoutLocalizationKey.CARD_NUMBER),
        initialValue = cardNumberState.text,
        isError = cardNumberState.isError,
        supportingText = supportingTextCardNumber,
        onValueChange = { value ->
            onIntent(CardIntent.UpdateCardNumber(value))
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
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.ExtraSmall),
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
    val placeholderResId = getThemedIcon(
        backgroundColor = CheckoutThemeProvider.elements.textField.backgroundColor,
        lightDrawableId = R.drawable.ic_card_placeholder_light,
        darkDrawableId = R.drawable.ic_card_placeholder_dark,
    )

    CheckoutNetworkLogo(
        modifier = modifier.size(Dimensions.LogoSize.small),
        txVariant = txVariant.orEmpty(),
        placeholder = placeholderResId,
        errorFallback = placeholderResId,
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
            modifier = Modifier.padding(top = Dimensions.Spacing.ExtraSmall),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.ExtraSmall),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.ExtraSmall),
        ) {
            for (cardBrand in cardBrands) {
                BrandLogo(cardBrand.txVariant)
            }
        }
    }
}

@Composable
private fun CardNumberFieldIcon(
    state: TextInputViewState,
    detectedBrands: List<CardBrand>,
    modifier: Modifier = Modifier,
) {
    val trailingIcon = state.trailingIcon as? CardNumberTrailingIcon
    AnimatedContent(targetState = trailingIcon, modifier = modifier) { trailingIcon ->
        when (trailingIcon) {
            CardNumberTrailingIcon.Warning -> Icon(
                modifier = Modifier.size(Dimensions.LogoSize.smallSquare),
                imageVector = ImageVector.vectorResource(com.adyen.checkout.test.R.drawable.ic_warning),
                contentDescription = null,
                tint = Color.Unspecified,
            )
            else -> DetectedBrandsList(detectedBrands)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CardNumberFieldPreview() {
    CardNumberField(
        cardNumberState = TextInputViewState(
            text = "5555444433331111",
        ),
        supportedCardBrands = listOf(
            CardBrand(CardType.MASTERCARD.txVariant),
            CardBrand(CardType.VISA.txVariant),
            CardBrand(CardType.AMERICAN_EXPRESS.txVariant),
        ),
        isSupportedCardBrandsShown = true,
        detectedCardBrands = listOf(CardBrand(CardType.MASTERCARD.txVariant)),
        isAmex = false,
        onIntent = {},
    )
}

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/11/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.ui.model.CardNumberTrailingIcon
import com.adyen.checkout.card.internal.ui.state.CardBrandViewState
import com.adyen.checkout.card.internal.ui.state.CardNumberFormat
import com.adyen.checkout.card.internal.ui.state.SelectableCardBrandItem
import com.adyen.checkout.card.internal.ui.state.SupportedCardBrandsViewState
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.internal.properties.CardNumberProperties.CARD_NUMBER_MAXIMUM_LENGTH
import com.adyen.checkout.core.common.internal.properties.CardNumberProperties.CARD_NUMBER_SEPARATOR
import com.adyen.checkout.core.common.internal.ui.CheckoutTextFieldTrailingIcon
import com.adyen.checkout.core.common.internal.ui.toImeAction
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TrailingIcon
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.element.input.rememberTextFieldStateWithCurrentValue
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun CardNumberField(
    cardNumberState: TextInputViewState,
    supportedCardBrandsViewState: SupportedCardBrandsViewState,
    cardBrandViewState: CardBrandViewState,
    cardNumberFormat: CardNumberFormat,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onFocusRequestConsumed: () -> Unit,
    onScanButtonClick: () -> Unit,
    onBrandSelect: (CardBrand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        CardNumberInputField(
            cardNumberState = cardNumberState,
            cardNumberFormat = cardNumberFormat,
            cardBrandViewState = cardBrandViewState,
            onValueChange = onValueChange,
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
            onScanButtonClick = onScanButtonClick,
            onBrandSelect = onBrandSelect,
        )

        CardBrandsList(
            supportedCardBrandsViewState = supportedCardBrandsViewState,
        )
    }
}

@Composable
private fun CardNumberInputField(
    cardNumberState: TextInputViewState,
    cardNumberFormat: CardNumberFormat,
    cardBrandViewState: CardBrandViewState,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onFocusRequestConsumed: () -> Unit,
    onScanButtonClick: () -> Unit,
    onBrandSelect: (CardBrand) -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingTextCardNumber = cardNumberState.supportingText?.let { resolveString(it) }

    val inputTransformation = remember {
        DigitOnlyInputTransformation(
            allowedSeparators = listOf(CARD_NUMBER_SEPARATOR),
            maxLengthWithoutSeparators = CARD_NUMBER_MAXIMUM_LENGTH,
        )
    }
    val outputTransformation = remember(cardNumberFormat) {
        CardNumberOutputTransformation(cardNumberFormat = cardNumberFormat)
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            },
        label = resolveString(CheckoutLocalizationKey.CARD_NUMBER),
        state = rememberTextFieldStateWithCurrentValue(cardNumberState.text),
        isError = cardNumberState.isError,
        supportingText = supportingTextCardNumber,
        onValueChange = onValueChange,
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        isFocusRequested = cardNumberState.isFocusRequested,
        onFocusRequestConsumed = onFocusRequestConsumed,
        imeAction = cardNumberState.keyboardAction.toImeAction(),
        trailingIcon = {
            CardNumberTrailingIcon(
                trailingIcon = cardNumberState.trailingIcon,
                cardBrandViewState = cardBrandViewState,
                onScanButtonClick = onScanButtonClick,
                onBrandSelect = onBrandSelect,
            )
        },
    )
}

@Composable
private fun CardNumberTrailingIcon(
    trailingIcon: TrailingIcon,
    cardBrandViewState: CardBrandViewState,
    onScanButtonClick: () -> Unit,
    onBrandSelect: (CardBrand) -> Unit,
) {
    CheckoutTextFieldTrailingIcon(trailingIcon) { state ->
        // unexpected state - the view state producer should set the correct type
        if (state !is CardNumberTrailingIcon) return@CheckoutTextFieldTrailingIcon

        when (state) {
            CardNumberTrailingIcon.ScanButton -> {
                IconButton(onClick = onScanButtonClick) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_camera),
                        contentDescription = null,
                        tint = CheckoutThemeProvider.colors.highlight,
                    )
                }
            }

            CardNumberTrailingIcon.BrandLogos -> DetectedBrandsList(cardBrandViewState, onBrandSelect)
        }
    }
}

@Suppress("LongMethod")
@Preview
@Composable
private fun CardNumberFieldPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        // empty input
        CardNumberField(
            cardNumberState = TextInputViewState(
                text = "",
                customTrailingIcon = CardNumberTrailingIcon.ScanButton,
            ),
            supportedCardBrandsViewState = SupportedCardBrandsViewState(
                supportedCardBrands = listOf(
                    CardBrand.MASTERCARD,
                    CardBrand.VISA,
                    CardBrand.AMERICAN_EXPRESS,
                ),
                isVisible = true,
            ),
            cardBrandViewState = CardBrandViewState.Placeholder,
            cardNumberFormat = CardNumberFormat.DEFAULT,
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
            onScanButtonClick = {},
            onBrandSelect = {},
        )

        // single detected card brand
        CardNumberField(
            cardNumberState = TextInputViewState(
                text = "5555444433331111",
                customTrailingIcon = CardNumberTrailingIcon.BrandLogos,
            ),
            supportedCardBrandsViewState = SupportedCardBrandsViewState(
                supportedCardBrands = emptyList(),
                isVisible = false,
            ),
            cardBrandViewState = CardBrandViewState.SingleBrand(CardBrand.MASTERCARD),
            cardNumberFormat = CardNumberFormat.DEFAULT,
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
            onScanButtonClick = {},
            onBrandSelect = {},
        )

        // Dual brand card logos + amex format
        CardNumberField(
            cardNumberState = TextInputViewState(
                text = "1234123456123451234",
                customTrailingIcon = CardNumberTrailingIcon.BrandLogos,
            ),
            supportedCardBrandsViewState = SupportedCardBrandsViewState(
                supportedCardBrands = emptyList(),
                isVisible = false,
            ),
            cardBrandViewState = CardBrandViewState.DualBrand(
                brands = listOf(
                    CardBrand.AMERICAN_EXPRESS,
                    CardBrand.MASTERCARD,
                ),
            ),
            cardNumberFormat = CardNumberFormat.AMEX,
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
            onScanButtonClick = {},
            onBrandSelect = {},
        )

        // Selectable dual brand card logos
        CardNumberField(
            cardNumberState = TextInputViewState(
                text = "5555444433330002",
                customTrailingIcon = CardNumberTrailingIcon.BrandLogos,
                supportingText = CheckoutLocalizationKey.CARD_DUAL_BRAND_SELECTOR_DESCRIPTION,
            ),
            supportedCardBrandsViewState = SupportedCardBrandsViewState(
                supportedCardBrands = emptyList(),
                isVisible = false,
            ),
            cardBrandViewState = CardBrandViewState.SelectableDualBrand(
                brands = listOf(
                    SelectableCardBrandItem(
                        brand = CardBrand.VISA,
                        isSelected = true,
                    ),
                    SelectableCardBrandItem(
                        brand = CardBrand.MASTERCARD,
                        isSelected = false,
                    ),
                ),
            ),
            cardNumberFormat = CardNumberFormat.DEFAULT,
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
            onScanButtonClick = {},
            onBrandSelect = {},
        )

        // error state
        CardNumberField(
            cardNumberState = TextInputViewState(
                text = "1234",
                isError = true,
                supportingText = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
            ),
            supportedCardBrandsViewState = SupportedCardBrandsViewState(
                supportedCardBrands = emptyList(),
                isVisible = false,
            ),
            cardBrandViewState = CardBrandViewState.Placeholder,
            cardNumberFormat = CardNumberFormat.DEFAULT,
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
            onScanButtonClick = {},
            onBrandSelect = {},
        )
    }
}

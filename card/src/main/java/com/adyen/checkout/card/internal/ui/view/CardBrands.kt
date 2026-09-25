/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.ui.state.CardBrandViewState
import com.adyen.checkout.card.internal.ui.state.SelectableCardBrandItem
import com.adyen.checkout.card.internal.ui.state.SupportedCardBrandsViewState
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.ui.internal.helper.getThemedIcon
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun DetectedBrandsList(
    cardBrandViewState: CardBrandViewState,
    onBrandSelect: (CardBrand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.ExtraSmall),
    ) {
        when (cardBrandViewState) {
            is CardBrandViewState.Placeholder -> BrandLogo(txVariant = null)
            is CardBrandViewState.SingleBrand -> BrandLogo(cardBrandViewState.brand.txVariant)
            is CardBrandViewState.DualBrand -> DualBrandLogos(cardBrandViewState.brands)

            is CardBrandViewState.SelectableDualBrand -> SelectableDualBrandLogos(
                brands = cardBrandViewState.brands,
                onBrandSelect = onBrandSelect,
            )
        }
    }
}

@Composable
private fun DualBrandLogos(
    brands: List<CardBrand>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = Dimensions.Spacing.Small),
    ) {
        brands.forEach { brand ->
            BrandLogo(
                txVariant = brand.txVariant,
                modifier = Modifier.padding(Dimensions.Spacing.ExtraSmall),
            )
        }
    }
}

// Each brand has a larger click target than its displayed logo to provide an accessible touch area.
@Composable
private fun SelectableDualBrandLogos(
    brands: List<SelectableCardBrandItem>,
    onBrandSelect: (CardBrand) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Two rows are rendered on top of each other, one for display and another for the click targets
    Box(modifier = modifier.height(Dimensions.MinTouchTarget)) {
        // Render the logos at their visual size.
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = Dimensions.Spacing.Small)
                .background(
                    color = CheckoutThemeProvider.colors.container,
                    shape = RoundedCornerShape(Dimensions.CornerRadius),
                )
                .padding(Dimensions.Spacing.ExtraSmall),
        ) {
            brands.forEach { brandItem ->
                SelectableBrandDisplay(
                    brandItem = brandItem,
                )
            }
        }

        // Split the full width into equal click targets for each brand.
        Row(
            modifier = Modifier.matchParentSize(),
        ) {
            brands.forEach { brandItem ->
                SelectableBrandClickTarget(
                    isSelected = brandItem.isSelected,
                    onClick = { onBrandSelect(brandItem.brand) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun SelectableBrandDisplay(
    brandItem: SelectableCardBrandItem,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimensions.CornerRadius)

    BrandLogo(
        txVariant = brandItem.brand.txVariant,
        modifier = modifier
            .clip(shape)
            .then(
                if (brandItem.isSelected) {
                    Modifier
                        .border(
                            width = 1.dp,
                            color = CheckoutThemeProvider.colors.outline,
                            shape = shape,
                        )
                        .background(color = CheckoutThemeProvider.colors.background)
                } else {
                    Modifier
                },
            )
            .padding(Dimensions.Spacing.ExtraSmall)
            .clip(shape),
    )
}

@Composable
private fun SelectableBrandClickTarget(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .semantics {
                role = Role.RadioButton
                selected = isSelected
            }
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onClick,
            ),
    )
}

@Composable
internal fun CardBrandsList(
    supportedCardBrandsViewState: SupportedCardBrandsViewState,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = supportedCardBrandsViewState.isVisible,
    ) {
        FlowRow(
            modifier = Modifier.padding(top = Dimensions.Spacing.ExtraSmall),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.ExtraSmall),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.ExtraSmall),
        ) {
            for (cardBrand in supportedCardBrandsViewState.supportedCardBrands) {
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

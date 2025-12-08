/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/11/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.card.internal.ui.model.CardBrandItem
import com.adyen.checkout.card.internal.ui.model.DualBrandData
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.element.SelectableListItem
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.BodyEmphasized
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun DualBrandSelector(
    dualBrandData: DualBrandData,
    onBrandSelected: (CardBrand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
    ) {
        TitleSection()
        BrandsListSection(
            dualBrandData = dualBrandData,
            onBrandSelected = onBrandSelected,
        )
    }
}

@Composable
private fun TitleSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.ExtraSmall),
    ) {
        BodyEmphasized(resolveString(CheckoutLocalizationKey.CARD_DUAL_BRAND_SELECTOR_TITLE))
        Body(resolveString(CheckoutLocalizationKey.CARD_DUAL_BRAND_SELECTOR_DESCRIPTION))
    }
}

@Composable
private fun BrandsListSection(
    dualBrandData: DualBrandData,
    onBrandSelected: (CardBrand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Small),
    ) {
        BrandOption(dualBrandData.brandOptionFirst, onBrandSelected)
        BrandOption(dualBrandData.brandOptionSecond, onBrandSelected)
    }
}

@Composable
private fun BrandOption(
    brandItem: CardBrandItem,
    onBrandSelected: (CardBrand) -> Unit,
) {
    SelectableListItem(
        title = brandItem.name,
        onClick = { onBrandSelected.invoke(brandItem.brand) },
        isSelected = brandItem.isSelected,
        leadingIcon = {
            CheckoutNetworkLogo(
                modifier = Modifier.size(Dimensions.LogoSize.medium),
                txVariant = brandItem.brand.txVariant,
            )
        },
    )
}

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
import com.adyen.checkout.card.internal.ui.model.DualBrandData
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.Body
import com.adyen.checkout.ui.internal.BodyEmphasized
import com.adyen.checkout.ui.internal.Dimensions
import com.adyen.checkout.ui.internal.ListOption

@Composable
internal fun DualBrandSelector(
    dualBrandData: DualBrandData?,
    modifier: Modifier = Modifier,
) {
    if (dualBrandData == null || !dualBrandData.selectable || dualBrandData.brandOptions.size < 2) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
    ) {
        TitleSection()
        BrandsListSection(
            dualBrandData = dualBrandData,
            onBrandSelected = {},
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
        dualBrandData.brandOptions.take(2).map { brandItem ->
            ListOption(
                title = brandItem.name,
                onClick = { onBrandSelected.invoke(brandItem.brand) },
                isSelected = brandItem.isSelected,
                leadingIcon = {
                    CheckoutNetworkLogo(
                        modifier = Modifier.size(Dimensions.LogoSize.large),
                        txVariant = brandItem.brand.txVariant,
                    )
                },
            )
        }
    }
}

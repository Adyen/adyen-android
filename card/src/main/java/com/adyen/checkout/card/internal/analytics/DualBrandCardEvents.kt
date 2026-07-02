/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 29/6/2026.
 */

package com.adyen.checkout.card.internal.analytics

import com.adyen.checkout.card.internal.ui.state.CardBrandData
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.CardBrand

internal object DualBrandCardEvents {

    fun dualBrandSelectionDisplayed(
        component: String,
        selectedBrand: CardBrand,
        brandOptions: List<CardBrandData>,
    ) = GenericEvents.displayed(
        component = component,
        target = DUAL_BRAND_ANALYTICS_TARGET,
        brand = selectedBrand.txVariant,
        configData = mapOf("dualBrands" to brandOptions.joinToString(",") { it.cardBrand.txVariant }),
    )

    fun brandSelected(
        component: String,
        selectedBrand: CardBrand
    ) = GenericEvents.selected(
        component = component,
        target = DUAL_BRAND_ANALYTICS_TARGET,
        brand = selectedBrand.txVariant,
    )

    private const val DUAL_BRAND_ANALYTICS_TARGET = "dual_brand_button"
}

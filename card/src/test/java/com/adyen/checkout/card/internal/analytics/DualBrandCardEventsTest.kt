/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 29/6/2026.
 */

package com.adyen.checkout.card.internal.analytics

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.ui.state.CardBrandData
import com.adyen.checkout.core.analytics.internal.AnalyticsEvent
import com.adyen.checkout.core.analytics.internal.DirectAnalyticsEventCreation
import com.adyen.checkout.core.common.CardBrand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(DirectAnalyticsEventCreation::class)
internal class DualBrandCardEventsTest {

    @Test
    fun `when dualBrandSelectionDisplayed is called then event has correct type, target, brand and configData`() {
        // GIVEN
        val component = "scheme"
        val selectedBrand = CardBrand("visa")
        val brandOptions = listOf(
            createCardBrandData(CardBrand("visa")),
            createCardBrandData(CardBrand("cartebancaire")),
        )

        // WHEN
        val event = DualBrandCardEvents.dualBrandSelectionDisplayed(
            component = component,
            selectedBrand = selectedBrand,
            brandOptions = brandOptions,
        )

        // THEN
        val expected = AnalyticsEvent.Info(
            component = component,
            type = AnalyticsEvent.Info.Type.DISPLAYED,
            target = "dual_brand_button",
            brand = "visa",
            configData = mapOf("dualBrands" to "visa,cartebancaire"),
        )
        assertEquals(expected.component, event.component)
        assertEquals(expected.type, event.type)
        assertEquals(expected.target, event.target)
        assertEquals(expected.brand, event.brand)
        assertEquals(expected.configData, event.configData)
    }

    @Test
    fun `when brandSelected is called then event has correct type, target and brand`() {
        // GIVEN
        val component = "scheme"
        val selectedBrand = CardBrand("cartebancaire")

        // WHEN
        val event = DualBrandCardEvents.brandSelected(
            component = component,
            selectedBrand = selectedBrand,
        )

        // THEN
        val expected = AnalyticsEvent.Info(
            component = component,
            type = AnalyticsEvent.Info.Type.SELECTED,
            target = "dual_brand_button",
            brand = "cartebancaire",
        )
        assertEquals(expected.component, event.component)
        assertEquals(expected.type, event.type)
        assertEquals(expected.target, event.target)
        assertEquals(expected.brand, event.brand)
    }

    private fun createCardBrandData(cardBrand: CardBrand) = CardBrandData(
        cardBrand = cardBrand,
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        panLength = null,
        paymentMethodVariant = null,
        localizedBrand = null,
    )
}

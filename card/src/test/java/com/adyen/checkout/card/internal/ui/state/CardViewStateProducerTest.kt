/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by andriim on 22/1/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.DualBrandedCardHandler
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardViewStateProducerTest {

    private lateinit var producer: CardViewStateProducer

    @BeforeEach
    fun beforeEach() {
        producer = CardViewStateProducer(
            dualBrandedCardHandler = DualBrandedCardHandler(),
        )
    }

    // UC5: Brand Detection Hides Placeholder (No Error)
    @Test
    fun `when no card brand is detected, then supported card brands should be shown`() {
        // GIVEN
        val componentState = createComponentState(
            detectedCardTypes = emptyList(),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertTrue(viewState.isSupportedCardBrandsShown)
    }

    // UC5: Brand Detection Hides Placeholder (No Error)
    @Test
    fun `when supported card brand is detected, then supported card brands should be hidden`() {
        // GIVEN
        val componentState = createComponentState(
            detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("visa"),
                    isSupported = true,
                ),
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.isSupportedCardBrandsShown)
    }

    // UC5: Brand Detection Hides Placeholder (No Error)
    @Test
    fun `when unsupported card brand is detected, then supported card brands should be shown`() {
        // GIVEN
        val componentState = createComponentState(
            detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("unknown"),
                    isSupported = false,
                ),
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertTrue(viewState.isSupportedCardBrandsShown)
    }

    private fun createComponentState(
        detectedCardTypes: List<DetectedCardType> = emptyList(),
    ) = CardComponentState(
        cardNumber = TextInputComponentState(),
        expiryDate = TextInputComponentState(),
        securityCode = TextInputComponentState(),
        holderName = TextInputComponentState(),
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = emptyList(),
        isLoading = false,
        detectedCardTypes = detectedCardTypes,
        selectedCardBrand = null,
    )

    private fun createDetectedCardType(
        cardBrand: CardBrand = CardBrand("visa"),
        isSupported: Boolean = true,
    ) = DetectedCardType(
        cardBrand = cardBrand,
        isReliable = true,
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        isSupported = isSupported,
        panLength = 16,
        paymentMethodVariant = null,
        localizedBrand = null,
    )
}

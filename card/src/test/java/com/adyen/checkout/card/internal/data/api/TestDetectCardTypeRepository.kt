/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 8/8/2022.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.CardType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Test implementation of [DetectCardTypeRepository].
 */
internal class TestDetectCardTypeRepository : DetectCardTypeRepository {

    private val _detectedCardTypesFlow: MutableSharedFlow<List<DetectedCardType>> =
        MutableSharedFlow(extraBufferCapacity = 1)
    override val detectedCardTypesFlow: Flow<List<DetectedCardType>> = _detectedCardTypesFlow

    var detectionResult: TestDetectedCardType = TestDetectedCardType.DETECTED_LOCALLY

    @Suppress("LongParameterList")
    override fun detectCardType(
        cardNumber: String,
        publicKey: String?,
        supportedCardBrands: List<CardBrand>,
        clientKey: String,
        coroutineScope: CoroutineScope,
        type: String?
    ) {
        val detectedCardTypes = when (detectionResult) {
            TestDetectedCardType.ERROR -> null
            TestDetectedCardType.DETECTED_LOCALLY -> getDetectedCardTypesLocal(supportedCardBrands)
            TestDetectedCardType.FETCHED_FROM_NETWORK -> getDetectedCardTypesNetwork(supportedCardBrands)
            TestDetectedCardType.DUAL_BRANDED -> getDetectedCardTypesDualBranded(supportedCardBrands)
            TestDetectedCardType.EMPTY -> emptyList()
        } ?: return

        _detectedCardTypesFlow.tryEmit(detectedCardTypes)
    }

    fun getDetectedCardTypesLocal(supportedCardTypes: List<CardBrand>): List<DetectedCardType> {
        val cardBrand = CardBrand(cardType = CardType.VISA)
        return listOf(
            DetectedCardType(
                cardBrand = cardBrand,
                isReliable = false,
                enableLuhnCheck = true,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = supportedCardTypes.contains(cardBrand),
                panLength = null,
                paymentMethodVariant = null,
            ),
        )
    }

    fun getDetectedCardTypesNetwork(supportedCardTypes: List<CardBrand>): List<DetectedCardType> {
        val cardBrand = CardBrand(cardType = CardType.MASTERCARD)
        return listOf(
            DetectedCardType(
                cardBrand = cardBrand,
                isReliable = true,
                enableLuhnCheck = true,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = supportedCardTypes.contains(cardBrand),
                panLength = 16,
                paymentMethodVariant = "mccredit",
            ),
        )
    }

    fun getDetectedCardTypesDualBranded(supportedCardBrands: List<CardBrand>): List<DetectedCardType> {
        val cardBrandFirst = CardBrand(cardType = CardType.BCMC)
        val cardBrandSecond = CardBrand(cardType = CardType.MAESTRO)
        return listOf(
            DetectedCardType(
                cardBrand = cardBrandFirst,
                isReliable = true,
                enableLuhnCheck = true,
                cvcPolicy = Brand.FieldPolicy.HIDDEN,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = supportedCardBrands.contains(cardBrandFirst),
                panLength = 16,
                paymentMethodVariant = "mccredit",
            ),
            DetectedCardType(
                cardBrand = cardBrandSecond,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.OPTIONAL,
                expiryDatePolicy = Brand.FieldPolicy.HIDDEN,
                isSupported = supportedCardBrands.contains(cardBrandSecond),
                panLength = 16,
                paymentMethodVariant = "maestrouk",
            ),
        )
    }
}

internal enum class TestDetectedCardType {
    ERROR,
    DETECTED_LOCALLY,
    FETCHED_FROM_NETWORK,
    DUAL_BRANDED,
    EMPTY,
}

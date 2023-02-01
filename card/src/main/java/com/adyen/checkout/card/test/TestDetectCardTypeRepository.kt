/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 8/8/2022.
 */

package com.adyen.checkout.card.test

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardBrand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.repository.DetectCardTypeRepository
import com.adyen.checkout.card.test.TestDetectCardTypeRepository.TestDetectedCardType.DETECTED_LOCALLY
import com.adyen.checkout.card.test.TestDetectCardTypeRepository.TestDetectedCardType.DUAL_BRANDED
import com.adyen.checkout.card.test.TestDetectCardTypeRepository.TestDetectedCardType.EMPTY
import com.adyen.checkout.card.test.TestDetectCardTypeRepository.TestDetectedCardType.ERROR
import com.adyen.checkout.card.test.TestDetectCardTypeRepository.TestDetectedCardType.FETCHED_FROM_NETWORK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Test implementation of [DetectCardTypeRepository]. This class should never be used except in test code.
 */
// TODO move to test fixtures once it becomes supported on Android
@RestrictTo(RestrictTo.Scope.TESTS)
internal class TestDetectCardTypeRepository : DetectCardTypeRepository {

    private val _detectedCardTypesFlow: MutableSharedFlow<List<DetectedCardType>> =
        MutableSharedFlow(extraBufferCapacity = 1)
    override val detectedCardTypesFlow: Flow<List<DetectedCardType>> = _detectedCardTypesFlow

    var detectionResult: TestDetectedCardType = DETECTED_LOCALLY

    @Suppress("LongParameterList")
    override fun detectCardType(
        cardNumber: String,
        publicKey: String?,
        supportedCardBrands: List<CardBrand>,
        clientKey: String,
        coroutineScope: CoroutineScope,
    ) {
        val detectedCardTypes = when (detectionResult) {
            ERROR -> null
            DETECTED_LOCALLY -> getDetectedCardTypesLocal(supportedCardBrands)
            FETCHED_FROM_NETWORK -> getDetectedCardTypesNetwork(supportedCardBrands)
            DUAL_BRANDED -> getDetectedCardTypesDualBranded(supportedCardBrands)
            EMPTY -> emptyList()
        } ?: return

        _detectedCardTypesFlow.tryEmit(detectedCardTypes)
    }

    enum class TestDetectedCardType {
        ERROR,
        DETECTED_LOCALLY,
        FETCHED_FROM_NETWORK,
        DUAL_BRANDED,
        EMPTY,
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
            )
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
            )
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
            ),
            DetectedCardType(
                cardBrand = cardBrandSecond,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.OPTIONAL,
                expiryDatePolicy = Brand.FieldPolicy.HIDDEN,
                isSupported = supportedCardBrands.contains(cardBrandSecond),
                panLength = 16,
            )
        )
    }
}

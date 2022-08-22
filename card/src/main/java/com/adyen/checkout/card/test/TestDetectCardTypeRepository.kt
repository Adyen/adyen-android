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
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.repository.DetectCardTypeRepository
import com.adyen.checkout.card.test.TestDetectCardTypeRepository.TestDetectedCardType.DETECTED_LOCALLY
import com.adyen.checkout.card.test.TestDetectCardTypeRepository.TestDetectedCardType.DUAL_BRANDED
import com.adyen.checkout.card.test.TestDetectCardTypeRepository.TestDetectedCardType.ERROR
import com.adyen.checkout.card.test.TestDetectCardTypeRepository.TestDetectedCardType.FETCHED_FROM_NETWORK
import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.core.api.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Test implementation of [DetectCardTypeRepository]. This class should never be used except in test code.
 */
// TODO move to test fixtures once it becomes supported on Android
@RestrictTo(RestrictTo.Scope.TESTS)
internal class TestDetectCardTypeRepository : DetectCardTypeRepository {

    private val _detectedCardTypesFlow: MutableSharedFlow<List<DetectedCardType>> = MutableSingleEventSharedFlow()
    override val detectedCardTypesFlow: Flow<List<DetectedCardType>> = _detectedCardTypesFlow

    var detectionResult: TestDetectedCardType = DETECTED_LOCALLY

    @Suppress("LongParameterList")
    override fun detectCardType(
        cardNumber: String,
        publicKey: String?,
        supportedCardTypes: List<CardType>,
        environment: Environment,
        clientKey: String,
        coroutineScope: CoroutineScope,
    ) {
        val detectedCardTypes = when (detectionResult) {
            ERROR -> null
            DETECTED_LOCALLY -> getDetectedCardTypesLocal(supportedCardTypes)
            FETCHED_FROM_NETWORK -> getDetectedCardTypesNetwork(supportedCardTypes)
            DUAL_BRANDED -> getDetectedCardTypesDualBranded(supportedCardTypes)
        } ?: return

        _detectedCardTypesFlow.tryEmit(detectedCardTypes)
    }

    enum class TestDetectedCardType {
        ERROR,
        DETECTED_LOCALLY,
        FETCHED_FROM_NETWORK,
        DUAL_BRANDED,
    }

    fun getDetectedCardTypesLocal(supportedCardTypes: List<CardType>): List<DetectedCardType> {
        val cardType = CardType.VISA
        return listOf(
            DetectedCardType(
                cardType = cardType,
                isReliable = false,
                enableLuhnCheck = true,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = supportedCardTypes.contains(cardType)
            )
        )
    }

    fun getDetectedCardTypesNetwork(supportedCardTypes: List<CardType>): List<DetectedCardType> {
        val cardType = CardType.MASTERCARD
        return listOf(
            DetectedCardType(
                cardType = cardType,
                isReliable = true,
                enableLuhnCheck = true,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = supportedCardTypes.contains(cardType)
            )
        )
    }

    fun getDetectedCardTypesDualBranded(supportedCardTypes: List<CardType>): List<DetectedCardType> {
        val cardTypeFirst = CardType.BCMC
        val cardTypeSecond = CardType.MAESTRO
        return listOf(
            DetectedCardType(
                cardType = cardTypeFirst,
                isReliable = true,
                enableLuhnCheck = true,
                cvcPolicy = Brand.FieldPolicy.HIDDEN,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = supportedCardTypes.contains(cardTypeFirst)
            ),
            DetectedCardType(
                cardType = cardTypeSecond,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.OPTIONAL,
                expiryDatePolicy = Brand.FieldPolicy.HIDDEN,
                isSupported = supportedCardTypes.contains(cardTypeSecond)
            )
        )
    }
}

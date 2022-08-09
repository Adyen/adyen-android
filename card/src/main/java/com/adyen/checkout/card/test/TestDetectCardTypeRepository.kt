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
import com.adyen.checkout.card.test.TestDetectCardTypeRepository.TestDetectedCardType.FETCHED_FROM_NETWORK
import com.adyen.checkout.core.api.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Test implementation of [DetectCardTypeRepository]. This class should never be used except in test code.
 */
// TODO move to test fixtures once it becomes supported on Android
@RestrictTo(RestrictTo.Scope.TESTS)
internal class TestDetectCardTypeRepository : DetectCardTypeRepository {

    private val _detectedCardTypesFlow: MutableSharedFlow<List<DetectedCardType>> =
        MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST)
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
            DETECTED_LOCALLY -> listOf(getDetectedCardTypeLocal(supportedCardTypes))
            FETCHED_FROM_NETWORK -> listOf(getDetectedCardTypeNetwork(supportedCardTypes))
        }

        _detectedCardTypesFlow.tryEmit(detectedCardTypes)
    }

    enum class TestDetectedCardType {
        DETECTED_LOCALLY,
        FETCHED_FROM_NETWORK,
    }

    fun getDetectedCardTypeLocal(supportedCardTypes: List<CardType>): DetectedCardType {
        val cardType = CardType.VISA
        return DetectedCardType(
            cardType = cardType,
            isReliable = false,
            enableLuhnCheck = true,
            cvcPolicy = Brand.FieldPolicy.REQUIRED,
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            isSupported = supportedCardTypes.contains(cardType)
        )
    }

    fun getDetectedCardTypeNetwork(supportedCardTypes: List<CardType>): DetectedCardType {
        val cardType = CardType.MASTERCARD
        return DetectedCardType(
            cardType = cardType,
            isReliable = true,
            enableLuhnCheck = true,
            cvcPolicy = Brand.FieldPolicy.REQUIRED,
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            isSupported = supportedCardTypes.contains(cardType)
        )
    }
}

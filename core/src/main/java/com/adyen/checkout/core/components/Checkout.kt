/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.common.internal.data.api.DefaultPublicKeyRepository
import com.adyen.checkout.core.common.internal.data.api.PublicKeyService
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.CheckoutSessionResult
import com.adyen.checkout.core.sessions.SessionModel
import com.adyen.checkout.core.sessions.internal.CheckoutSessionProvider

object Checkout {

    suspend fun initialize(
        sessionModel: SessionModel,
        checkoutConfiguration: CheckoutConfiguration,
        checkoutCallbacks: CheckoutCallbacks,
    ): Result {
        // TODO - Fetch checkoutAttemptId
        val checkoutSession = getCheckoutSession(sessionModel, checkoutConfiguration)
        val publicKey = fetchPublicKey(checkoutConfiguration)
        return when {
            checkoutSession == null -> {
                Result.Error("Failed to initialize sessions.")
            }
            else -> Result.Success(
                checkoutContext = CheckoutContext.Sessions(
                    checkoutSession = checkoutSession,
                    checkoutConfiguration = checkoutConfiguration,
                    checkoutCallbacks = checkoutCallbacks,
                    publicKey = publicKey,
                ),
            )
        }
    }

    suspend fun initialize(
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        checkoutConfiguration: CheckoutConfiguration,
        checkoutCallbacks: CheckoutCallbacks,
    ): Result {
        val publicKey = fetchPublicKey(checkoutConfiguration)
        // TODO - Fetch checkoutAttemptId
        return Result.Success(
            CheckoutContext.Advanced(
                paymentMethodsApiResponse = paymentMethodsApiResponse,
                checkoutConfiguration = checkoutConfiguration,
                checkoutCallbacks = checkoutCallbacks,
                publicKey = publicKey
            ),
        )
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        checkoutConfiguration: CheckoutConfiguration,
    ): CheckoutSession? {
        return when (
            val result = CheckoutSessionProvider.createSession(sessionModel, checkoutConfiguration)
        ) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> null
        }
    }

    private suspend fun fetchPublicKey(checkoutConfiguration: CheckoutConfiguration): String? {
        val httpClient = HttpClientFactory.getHttpClient(checkoutConfiguration.environment)
        val publicKeyRepository = DefaultPublicKeyRepository(PublicKeyService(httpClient))
        publicKeyRepository.fetchPublicKey(
            environment = checkoutConfiguration.environment,
            clientKey = checkoutConfiguration.clientKey,
        ).fold(
            onSuccess = { key ->
                adyenLog(AdyenLogLevel.DEBUG) { "Public key fetched" }
                return key
            },
            onFailure = { e ->
                adyenLog(AdyenLogLevel.ERROR) { "Unable to fetch public key" }

                // TODO - Public Key. Analytics.
//                val event = GenericEvents.error(paymentMethod.type.orEmpty(), ErrorEvent.API_PUBLIC_KEY)
//                analyticsManager.trackEvent(event)
                return null
            },
        )
    }

    sealed interface Result {
        data class Success(val checkoutContext: CheckoutContext) : Result
        data class Error(val errorReason: String) : Result
    }
}

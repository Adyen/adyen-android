/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/11/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.analytics.internal.data.remote.api.AnalyticsService
import com.adyen.checkout.core.analytics.internal.data.remote.model.AnalyticsSetupRequest
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.common.internal.data.api.DefaultPublicKeyRepository
import com.adyen.checkout.core.common.internal.data.api.PublicKeyService
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.CheckoutSessionResult
import com.adyen.checkout.core.sessions.SessionResponse
import com.adyen.checkout.core.sessions.internal.CheckoutSessionProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CheckoutInitializer {

    suspend fun initialize(
        checkoutConfiguration: CheckoutConfiguration,
        sessionResponse: SessionResponse?,
    ): InitializationData = supervisorScope {
        val checkoutSessionDeferred = async {
            sessionResponse?.let { getCheckoutSession(it, checkoutConfiguration) }
        }
        val publicKeyDeferred = async { fetchPublicKey(checkoutConfiguration) }
        val checkoutAttemptIdDeferred = async { fetchCheckoutAttemptId(checkoutConfiguration) }

        InitializationData(
            checkoutSession = checkoutSessionDeferred.await(),
            publicKey = publicKeyDeferred.await(),
            checkoutAttemptId = checkoutAttemptIdDeferred.await(),
        )
    }

    private suspend fun getCheckoutSession(
        sessionResponse: SessionResponse,
        checkoutConfiguration: CheckoutConfiguration,
    ): CheckoutSession? {
        return when (
            val result = CheckoutSessionProvider.createSession(sessionResponse, checkoutConfiguration)
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
            onFailure = {
                adyenLog(AdyenLogLevel.ERROR) { "Unable to fetch public key" }
                return null
            },
        )
    }

    private suspend fun fetchCheckoutAttemptId(
        checkoutConfiguration: CheckoutConfiguration,
    ): String? {
        val httpClient = HttpClientFactory.getAnalyticsHttpClient(checkoutConfiguration.environment)
        val analyticsService = AnalyticsService(httpClient)

        analyticsService.fetchCheckoutAttemptId(
            request = AnalyticsSetupRequest(),
            clientKey = checkoutConfiguration.clientKey,
        ).fold(
            onSuccess = { response ->
                adyenLog(AdyenLogLevel.DEBUG) { "Checkout attempt ID fetched" }
                return response.checkoutAttemptId
            },
            onFailure = {
                adyenLog(AdyenLogLevel.ERROR) { "Unable to fetch checkout attempt ID" }
                return null
            },
        )
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class InitializationData(
    val checkoutSession: CheckoutSession?,
    val publicKey: String?,
    val checkoutAttemptId: String?,
)

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/11/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.common.internal.data.api.DefaultPublicKeyRepository
import com.adyen.checkout.core.common.internal.data.api.PublicKeyService
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.CheckoutSessionResult
import com.adyen.checkout.core.sessions.SessionModel
import com.adyen.checkout.core.sessions.internal.CheckoutSessionProvider

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CheckoutInitializer {

    suspend fun initialize(
        checkoutConfiguration: CheckoutConfiguration,
        sessionModel: SessionModel?,
    ): InitializationData {
        // TODO - Fetch checkoutAttemptId
        val checkoutSession = sessionModel?.let { getCheckoutSession(sessionModel, checkoutConfiguration) }
        val publicKey = fetchPublicKey(checkoutConfiguration)
        return InitializationData(
            checkoutSession = checkoutSession,
            publicKey = publicKey,
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
                return null
            },
        )
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class InitializationData(
    val checkoutSession: CheckoutSession?,
    val publicKey: String?,
)

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.internal.CheckoutInitializer
import com.adyen.checkout.core.components.internal.validate
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.sessions.SessionResponse

/**
 * Entry point to set up a checkout.
 *
 * Use one of the [setup] methods to initialize the SDK for a specific integration flow and obtain a
 * [CheckoutContext] that can then be passed to a [CheckoutController].
 */
object Checkout {

    /**
     * Sets up a checkout using the sessions flow.
     *
     * You only need to integrate with the `/sessions` endpoint to create a session and the SDK will
     * automatically handle the rest of the payment flow.
     *
     * @param sessionResponse The response of the `/sessions` endpoint, deserialized into a [SessionResponse].
     * @param configuration The [CheckoutConfiguration] to use for this checkout.
     * @return A [Result.Success] with a [CheckoutContext.Sessions] on success, or a [Result.Error] on failure.
     */
    suspend fun setup(
        sessionResponse: SessionResponse,
        configuration: CheckoutConfiguration,
    ): Result<CheckoutContext.Sessions> {
        configuration.validate()?.let { error ->
            return Result.Error(error)
        }

        val initializationData = CheckoutInitializer.initialize(
            checkoutConfiguration = configuration,
            sessionResponse = sessionResponse,
        )

        return when {
            initializationData.checkoutSession == null -> Result.Error(
                CheckoutError(
                    code = CheckoutError.ErrorCode.SESSION_SETUP_FAILURE,
                    message = "Failed to initialize sessions.",
                ),
            )

            else -> Result.Success(
                checkoutContext = CheckoutContext.Sessions(
                    checkoutSession = initializationData.checkoutSession,
                    checkoutConfiguration = configuration,
                    checkoutAttemptId = initializationData.checkoutAttemptId,
                    publicKey = initializationData.publicKey,
                ),
            )
        }
    }

    /**
     * Sets up a checkout using the advanced flow.
     *
     * With the advanced flow you make the network calls to the Checkout API yourself, through the callbacks
     * provided to the [CheckoutController].
     *
     * @param paymentMethods The available payment methods, from the `/paymentMethods` endpoint.
     * @param configuration The [CheckoutConfiguration] to use for this checkout.
     * @return A [Result.Success] with a [CheckoutContext.Advanced] on success, or a [Result.Error] on failure.
     */
    suspend fun setup(
        paymentMethods: PaymentMethods,
        configuration: CheckoutConfiguration,
    ): Result<CheckoutContext.Advanced> {
        configuration.validate()?.let { error ->
            return Result.Error(error)
        }

        val initializationData = CheckoutInitializer.initialize(
            checkoutConfiguration = configuration,
            sessionResponse = null,
        )

        return Result.Success(
            CheckoutContext.Advanced(
                paymentMethods = paymentMethods,
                checkoutConfiguration = configuration,
                checkoutAttemptId = initializationData.checkoutAttemptId,
                publicKey = initializationData.publicKey,
            ),
        )
    }

    /**
     * Sets up a checkout that only handles an action, without collecting payment details first.
     *
     * @param action The [Action] to be handled.
     * @param configuration The [CheckoutConfiguration] to use for this checkout.
     * @return A [Result.Success] with a [CheckoutContext.ActionOnly] on success, or a [Result.Error] on failure.
     */
    suspend fun setup(
        action: Action,
        configuration: CheckoutConfiguration,
    ): Result<CheckoutContext.ActionOnly> {
        configuration.validate()?.let { error ->
            return Result.Error(error)
        }

        val initializationData = CheckoutInitializer.initialize(
            checkoutConfiguration = configuration,
            sessionResponse = null,
        )

        return Result.Success(
            CheckoutContext.ActionOnly(
                action = action,
                checkoutConfiguration = configuration,
                checkoutAttemptId = initializationData.checkoutAttemptId,
                publicKey = initializationData.publicKey,
            ),
        )
    }

    /**
     * The result of a [setup] call.
     */
    sealed interface Result<T : CheckoutContext> {
        /**
         * The setup succeeded.
         *
         * @param checkoutContext The resulting [CheckoutContext] to pass to a [CheckoutController].
         */
        data class Success<T : CheckoutContext>(val checkoutContext: T) : Result<T>

        /**
         * The setup failed.
         *
         * @param error The [CheckoutError] describing what went wrong.
         */
        data class Error<T : CheckoutContext>(val error: CheckoutError) : Result<T>
    }
}

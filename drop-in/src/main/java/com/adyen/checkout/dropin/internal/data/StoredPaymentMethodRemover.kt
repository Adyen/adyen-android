/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/10/2026.
 */

package com.adyen.checkout.dropin.internal.data

import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.dropin.internal.data.api.DisableTokenRepository

/**
 * Removes a stored payment method of the shopper on the backend.
 */
internal fun interface StoredPaymentMethodRemover {

    suspend fun remove(storedPaymentMethod: StoredPaymentMethod): Result<Unit>
}

internal class DefaultStoredPaymentMethodRemover(
    private val checkoutContext: CheckoutContext,
    private val disableTokenRepository: DisableTokenRepository,
) : StoredPaymentMethodRemover {

    override suspend fun remove(storedPaymentMethod: StoredPaymentMethod): Result<Unit> {
        return when (checkoutContext) {
            is CheckoutContext.Sessions -> disableTokenRepository.removeStoredPaymentMethod(
                checkoutSession = checkoutContext.checkoutSession,
                storedPaymentMethodId = storedPaymentMethod.id,
            )

            // TODO - Call DropInService.onRemoveStoredPaymentMethod once it is added
            is CheckoutContext.Advanced -> Result.failure(
                UnsupportedOperationException("Removing stored payment methods is not supported in the advanced flow."),
            )

            is CheckoutContext.ActionOnly -> error("Unsupported context: $checkoutContext")
        }
    }
}

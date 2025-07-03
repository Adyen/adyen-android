/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/6/2025.
 */

package com.adyen.checkout.core.internal

import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.internal.ui.PaymentDelegate
import com.adyen.checkout.core.sessions.internal.model.SessionParams
import kotlinx.coroutines.CoroutineScope

internal interface PaymentMethodFactory<CS : BaseComponentState, T : PaymentDelegate<CS>> {

    /**
     * Creates a [PaymentDelegate].
     *
     * @param coroutineScope Coroutine Scope.
     * @param checkoutConfiguration Checkout Configuration.
     * @param componentSessionParams Configuration from Sessions.
     *
     * @return A [PaymentDelegate] instance.
     */
    fun create(
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,

        // TODO - Remove dependency to SessionParams. Investigate separating ComponentParams into two:
        //  CheckoutParams and PaymentMethodParams to reflect the structure in CheckoutConfiguration.
        componentSessionParams: SessionParams?,
    ): T
}

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/6/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.sessions.internal.model.SessionParams
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PaymentMethodFactory<CS : BasePaymentComponentState, T : PaymentComponent<CS>> {

    /**
     * Creates a [PaymentComponent].
     *
     * @param coroutineScope Coroutine Scope.
     * @param checkoutConfiguration Checkout Configuration.
     * @param commonComponentParams Configuration from the component.
     * @param componentSessionParams Configuration from Sessions.
     *
     * @return A [PaymentComponent] instance.
     */
    fun create(
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        commonComponentParams: CommonComponentParams,

        // TODO - Remove dependency to SessionParams. Investigate separating ComponentParams into two:
        //  CheckoutParams and PaymentMethodParams to reflect the structure in CheckoutConfiguration.
        componentSessionParams: SessionParams?,
    ): T
}

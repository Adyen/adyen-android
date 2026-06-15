/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/4/2026.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.components.data.BeforeSubmitData
import com.adyen.checkout.core.error.CheckoutError

/**
 * Callbacks used during a payment using the sessions flow.
 *
 * @param onComplete Called when the payment is completed.
 * @param onFailure Called when an error occurs.
 * @param onBeforeSubmit Called before the payment is submitted.
 */
class SessionCheckoutCallbacks(
    internal val onComplete: (result: SessionCheckoutResult) -> Unit,
    internal val onFailure: (CheckoutError) -> Unit,
    internal val onBeforeSubmit: (suspend (data: BeforeSubmitData) -> BeforeSubmitResult)? = null,
    additionalCallbacksBlock: CheckoutCallbacks.() -> Unit = {},
) : CheckoutCallbacks(additionalCallbacksBlock)

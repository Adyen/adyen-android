/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/5/2026.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.error.CheckoutError

/**
 * Callbacks used when only handling an action, without collecting payment details first.
 *
 * @param onAdditionalDetails Called when additional details are required to complete the action. Make a network call
 * to the `/payments/details` endpoint of the Checkout API through your server, and return the result as an
 * [AdditionalDetailsResult].
 * @param onFailure Called when an error occurs.
 * @param onComplete Called when the payment is completed.
 * @param additionalCallbacksBlock An optional block to register payment-method-specific [CheckoutAdditionalCallback]s.
 */
class ActionOnlyCheckoutCallbacks(
    internal val onAdditionalDetails: suspend (data: ActionComponentData) -> AdditionalDetailsResult,
    internal val onFailure: (CheckoutError) -> Unit,
    internal val onComplete: (result: AdvancedCheckoutResult) -> Unit = {},
    additionalCallbacksBlock: CheckoutCallbacks.() -> Unit = {},
) : CheckoutCallbacks(additionalCallbacksBlock)

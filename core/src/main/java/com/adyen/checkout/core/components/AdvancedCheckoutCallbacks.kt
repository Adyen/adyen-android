/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/4/2026.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.error.CheckoutError

class AdvancedCheckoutCallbacks(
    internal val onSubmit: suspend (data: PaymentComponentData<*>) -> SubmitResult,
    internal val onAdditionalDetails: suspend (data: ActionComponentData) -> AdditionalDetailsResult,
    internal val onError: (CheckoutError) -> Unit,
    additionalCallbacksBlock: CheckoutCallbacks.() -> Unit = {},
) : CheckoutCallbacks(additionalCallbacksBlock)

/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/4/2026.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.error.CheckoutError

class SessionCheckoutCallbacks(
    internal val onFinished: () -> Unit,
    internal val onError: (CheckoutError) -> Unit,
    internal val beforeSubmit: (suspend (data: PaymentComponentData<*>) -> Unit)? = null,
    additionalCallbacksBlock: CheckoutCallbacks.() -> Unit = {},
) : CheckoutCallbacks(additionalCallbacksBlock)

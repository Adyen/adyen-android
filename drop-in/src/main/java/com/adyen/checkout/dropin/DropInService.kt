/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/1/2026.
 */

package com.adyen.checkout.dropin

import androidx.lifecycle.LifecycleService
import com.adyen.checkout.core.components.CheckoutResult

abstract class DropInService : LifecycleService() {

    abstract suspend fun onSubmit(): CheckoutResult
}

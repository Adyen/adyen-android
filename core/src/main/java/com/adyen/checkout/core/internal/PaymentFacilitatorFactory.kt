/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/5/2025.
 */

package com.adyen.checkout.core.internal

import kotlinx.coroutines.CoroutineScope

internal interface PaymentFacilitatorFactory {
    fun create(txVariant: String, coroutineScope: CoroutineScope): PaymentFacilitator
}

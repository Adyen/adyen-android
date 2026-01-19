/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/1/2026.
 */

package com.adyen.checkout.dropin.internal.service

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.CheckoutResult

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal interface DropInInteractor {

    suspend fun onSubmit(): CheckoutResult
}

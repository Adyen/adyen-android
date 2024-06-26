/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2024.
 */

package com.adyen.checkout.components.core.internal.ui

import androidx.annotation.RestrictTo
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AvailabilityCheckingDelegate {

    val availabilityFlow: Flow<Boolean>

    fun checkAvailability()
}

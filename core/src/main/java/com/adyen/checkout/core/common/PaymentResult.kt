/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/8/2025.
 */

package com.adyen.checkout.core.common

import android.os.Parcelable
import com.adyen.checkout.core.components.data.OrderResponse
import kotlinx.parcelize.Parcelize

// TODO - Kdocs
@Parcelize
data class PaymentResult(
    val resultCode: String,
    val sessionId: String?,
    val sessionResult: String?,
    // TODO - Check whether sessionData is required or not?
    val sessionData: String?,
    // TODO - Check whether order is required or not
    val order: OrderResponse?,
) : Parcelable

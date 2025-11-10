/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.sessions

import android.os.Parcelable
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.data.Order
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import kotlinx.parcelize.Parcelize

// TODO - KDocs
@Parcelize
data class CheckoutSession(
    val sessionSetupResponse: SessionSetupResponse,
    val order: Order?,
    val environment: Environment,
    val clientKey: String,
) : Parcelable

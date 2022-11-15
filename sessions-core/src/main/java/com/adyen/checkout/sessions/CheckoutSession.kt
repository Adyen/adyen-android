/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.sessions

import com.adyen.checkout.sessions.model.setup.SessionSetupResponse

// TODO docs
data class CheckoutSession(
    val sessionSetupResponse: SessionSetupResponse
)

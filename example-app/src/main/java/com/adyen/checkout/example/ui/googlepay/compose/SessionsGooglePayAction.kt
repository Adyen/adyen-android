/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/12/2023.
 */

package com.adyen.checkout.example.ui.googlepay.compose

import com.adyen.checkout.components.core.action.Action

internal data class SessionsGooglePayAction(
    val componentData: SessionsGooglePayComponentData,
    val action: Action,
)

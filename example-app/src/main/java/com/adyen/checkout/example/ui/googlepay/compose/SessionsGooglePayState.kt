/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2023.
 */

package com.adyen.checkout.example.ui.googlepay.compose

import android.content.Intent
import androidx.compose.runtime.Immutable
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.ui.googlepay.GooglePayActivityResult

@Immutable
internal data class SessionsGooglePayState(
    val uiState: SessionsGooglePayUIState,
    val componentData: SessionsGooglePayComponentData? = null,
    val startGooglePay: SessionsStartGooglePayData? = null,
    val activityResult: GooglePayActivityResult? = null,
    val action: Action? = null,
    val newIntent: Intent? = null,
)

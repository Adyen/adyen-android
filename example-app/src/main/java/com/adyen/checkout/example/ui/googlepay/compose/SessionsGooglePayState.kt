/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2023.
 */

package com.adyen.checkout.example.ui.googlepay.compose

import androidx.compose.runtime.Immutable
import com.adyen.checkout.example.ui.compose.ResultState

internal sealed class SessionsGooglePayState {

    @Immutable
    data object Loading : SessionsGooglePayState()

    @Immutable
    data object ShowButton : SessionsGooglePayState()

    @Immutable
    data class FinalResult(val finalResult: ResultState) : SessionsGooglePayState()
}

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

internal sealed class SessionsGooglePayUIState {

    @Immutable
    data object Loading : SessionsGooglePayUIState()

    @Immutable
    data class ShowButton(val componentData: SessionsGooglePayComponentData) : SessionsGooglePayUIState()

    @Immutable
    data class ShowComponent(val componentData: SessionsGooglePayComponentData) : SessionsGooglePayUIState()

    @Immutable
    data class FinalResult(val finalResult: ResultState) : SessionsGooglePayUIState()
}

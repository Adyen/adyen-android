/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 23/6/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

internal sealed class GooglePayViewEvent {

    /**
     * Trigger to launch the Google Pay payment sheet. Carries no payload: the [GooglePayComponent]'s
     * [androidx.compose.runtime.Composable] content builds the request and launches the sheet.
     */
    data object Pay : GooglePayViewEvent()
}

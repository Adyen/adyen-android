/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/11/2023.
 */

package com.adyen.checkout.example.ui.card

import androidx.compose.runtime.Immutable
import com.adyen.checkout.components.core.AddressLookupResult
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.ui.compose.ResultState

@Immutable
internal data class SessionsCardUiState(
    val checkoutConfiguration: CheckoutConfiguration,
    val isLoading: Boolean = false,
    val oneTimeMessage: String? = null,
    val componentData: SessionsCardComponentData? = null,
    val action: Action? = null,
    val addressLookupOptions: List<LookupAddress>? = null,
    val addressLookupResult: AddressLookupResult? = null,
    val finalResult: ResultState? = null,
)

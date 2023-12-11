/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/12/2023.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.internal.data.model.LookupAddress

internal sealed class AddressLookupState {
    object Initial : AddressLookupState()
    object Loading : AddressLookupState()
    object Form : AddressLookupState()
    data class SearchResult(val options: List<LookupAddress>) : AddressLookupState()
    object Error : AddressLookupState()
}

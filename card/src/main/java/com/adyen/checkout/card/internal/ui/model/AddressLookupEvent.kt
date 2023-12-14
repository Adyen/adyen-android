/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/12/2023.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.internal.data.model.LookupAddress

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class AddressLookupEvent {
    data class Query(val query: String) : AddressLookupEvent()
    object Manual : AddressLookupEvent()
    object ClearQuery : AddressLookupEvent()
    data class SearchResult(val addressLookupOptions: List<LookupAddress>) : AddressLookupEvent()
    data class OptionSelected(val lookupAddress: LookupAddress, val loading: Boolean) : AddressLookupEvent()
}

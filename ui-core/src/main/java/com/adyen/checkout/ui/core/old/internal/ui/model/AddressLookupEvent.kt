/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class AddressLookupEvent {
    data class Initialize(val address: AddressInputModel) : AddressLookupEvent()
    data class Query(val query: String) : AddressLookupEvent()
    object Manual : AddressLookupEvent()
    object ClearQuery : AddressLookupEvent()
    data class SearchResult(val addressLookupOptions: List<LookupAddress>) : AddressLookupEvent()
    data class OptionSelected(val lookupAddress: LookupAddress, val loading: Boolean) : AddressLookupEvent()
    object InvalidUI : AddressLookupEvent()
    data class ErrorResult(val message: String? = null) : AddressLookupEvent()
}

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.ui.core.old.internal.ui.view.LookupOption

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class AddressLookupState {
    object Initial : AddressLookupState()
    object Loading : AddressLookupState()
    data class Form(val selectedAddress: AddressInputModel?) : AddressLookupState()
    data class SearchResult(val query: String, val options: List<LookupOption>) : AddressLookupState()
    data class Error(val query: String) : AddressLookupState()
    object InvalidUI : AddressLookupState()
}

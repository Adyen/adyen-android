/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/12/2023.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.internal.data.model.LookupAddress
import com.adyen.checkout.ui.core.internal.ui.model.AddressInputModel

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class AddressLookupState {
    object Initial : AddressLookupState()
    object Loading : AddressLookupState()
    data class Form(val selectedAddress: AddressInputModel?) : AddressLookupState()
    data class SearchResult(val query: String, val options: List<LookupAddress>) : AddressLookupState()
    object Error : AddressLookupState()
}

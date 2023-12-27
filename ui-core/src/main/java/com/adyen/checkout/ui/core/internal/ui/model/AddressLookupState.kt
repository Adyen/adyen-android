/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/12/2023.
 */

package com.adyen.checkout.ui.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.AddressInputModel
import com.adyen.checkout.ui.core.internal.ui.view.LookupOption

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class AddressLookupState {
    object Initial : AddressLookupState()
    object Loading : AddressLookupState()
    data class Form(val selectedAddress: AddressInputModel?) : AddressLookupState()
    data class SearchResult(val query: String, val options: List<LookupOption>) : AddressLookupState()
    object Error : AddressLookupState()
    object InvalidUI : AddressLookupState()
}

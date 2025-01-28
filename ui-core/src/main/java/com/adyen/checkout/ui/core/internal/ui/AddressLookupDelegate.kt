/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/12/2023.
 */

package com.adyen.checkout.ui.core.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.AddressLookupCallback
import com.adyen.checkout.components.core.AddressLookupResult
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.ui.core.internal.ui.model.AddressLookupState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AddressLookupDelegate {

    val addressDelegate: AddressDelegate

    val addressLookupStateFlow: Flow<AddressLookupState>
    val addressLookupSubmitFlow: Flow<AddressInputModel>
    val addressLookupErrorPopupFlow: Flow<String?>

    fun initialize(coroutineScope: CoroutineScope, addressInputModel: AddressInputModel)
    fun updateAddressLookupOptions(options: List<LookupAddress>)
    fun setAddressLookupResult(addressLookupResult: AddressLookupResult)
    fun setAddressLookupCallback(addressLookupCallback: AddressLookupCallback)
    fun onAddressQueryChanged(query: String)
    fun onAddressLookupCompletion(lookupAddress: LookupAddress): Boolean
    fun onManualEntryModeSelected()
    fun submitAddress()

    fun clear()
}

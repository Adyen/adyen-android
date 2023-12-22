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
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.ui.core.internal.ui.model.AddressLookupEvent
import com.adyen.checkout.ui.core.internal.ui.model.AddressLookupInputData
import com.adyen.checkout.ui.core.internal.ui.model.AddressLookupState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AddressLookupDelegate {

    val addressLookupStateFlow: Flow<AddressLookupState>
    val addressLookupEventChannel: Channel<AddressLookupEvent>

    val addressDelegate: AddressDelegate

    fun startAddressLookup()
    fun updateAddressLookupOptions(options: List<LookupAddress>)
    fun setAddressLookupResult(lookupAddress: LookupAddress)
    fun updateAddressLookupInputData(update: AddressLookupInputData.() -> Unit)
    fun setAddressLookupCallback(addressLookupCallback: AddressLookupCallback)
    fun onAddressQueryChanged(query: String)
    fun onAddressLookupCompleted(lookupAddress: LookupAddress): Boolean
    fun onManualEntryModeSelected()
}

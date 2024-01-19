/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/1/2024.
 */

package com.adyen.checkout.components.core.internal

import com.adyen.checkout.components.core.AddressLookupCallback
import com.adyen.checkout.components.core.AddressLookupResult
import com.adyen.checkout.components.core.LookupAddress

/**
 * A Component that performs Address Lookup functionality should implement this interface.
 */
interface AddressLookupComponent {

    /**
     * Set a callback that will be triggered to perform address lookup actions.
     *
     * @param addressLookupCallback The callback that will be triggered to perform address lookup options such as
     * query changes, completion of the lookup.
     */
    fun setAddressLookupCallback(addressLookupCallback: AddressLookupCallback)

    /**
     * Updates the address options that will be displayed to the shopper in
     * [com.adyen.checkout.ui.core.internal.ui.view.AddressLookupView] as part of [CardComponent].
     *
     * @param options Address option list to be displayed.
     */
    fun updateAddressLookupOptions(options: List<LookupAddress>)

    /**
     * Set the result of address completion call.
     *
     * @param addressLookupResult The result.
     */
    fun setAddressLookupResult(addressLookupResult: AddressLookupResult)
}

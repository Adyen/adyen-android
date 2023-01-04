/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 3/1/2023.
 */

package com.adyen.checkout.card


import kotlinx.coroutines.flow.Flow

interface AddressDelegate {
    val addressOutputData: AddressOutputData
    val addressOutputDataFlow: Flow<AddressOutputData>
    fun updateAddressInputData(update: AddressInputModel.() -> Unit)
}

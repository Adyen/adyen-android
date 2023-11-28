/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/11/2023.
 */

package com.adyen.checkout.card.internal.data.model

import com.adyen.checkout.ui.core.internal.ui.model.AddressInputModel

data class LookupAddress(
    val id: String,
    val address: AddressInputModel
)

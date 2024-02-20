/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/1/2024.
 */

package com.adyen.checkout.example.data.mock.model

import com.adyen.checkout.components.core.LookupAddress

data class MockAddressLookupResponse(
    val options: List<LookupAddress>
)

/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/1/2024.
 */

package com.adyen.checkout.example.data.mock.model

import com.adyen.checkout.components.core.LookupAddress
import javax.inject.Inject

data class MockAddressLookupResponse @Inject constructor(
    val options: List<LookupAddress>
)

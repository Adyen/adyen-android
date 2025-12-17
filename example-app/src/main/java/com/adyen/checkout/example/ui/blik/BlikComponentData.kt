/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/1/2023.
 */

package com.adyen.checkout.example.ui.blik

import com.adyen.checkout.blik.old.BlikComponentState
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.PaymentMethod

data class BlikComponentData(
    val paymentMethod: PaymentMethod,
    val callback: ComponentCallback<BlikComponentState>,
)

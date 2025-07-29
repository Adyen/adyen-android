/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/1/2023.
 */

package com.adyen.checkout.example.ui.card

import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.PaymentMethod

data class CardComponentData(
    val paymentMethod: PaymentMethod,
    val callback: ComponentCallback<CardComponentState>,
)

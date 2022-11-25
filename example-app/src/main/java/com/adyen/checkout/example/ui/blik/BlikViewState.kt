/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 17/11/2022.
 */

package com.adyen.checkout.example.ui.blik

import com.adyen.checkout.components.model.paymentmethods.PaymentMethod

sealed class BlikViewState {

    object Loading : BlikViewState()

    data class ShowComponent(val paymentMethod: PaymentMethod) : BlikViewState()

    object Error : BlikViewState()
}

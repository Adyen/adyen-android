/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/7/2022.
 */

package com.adyen.checkout.card

import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.ui.AddressDelegate
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.UIStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.flow.Flow

interface CardDelegate :
    PaymentComponentDelegate<CardComponentState>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate,
    AddressDelegate {

    val outputData: CardOutputData

    val outputDataFlow: Flow<CardOutputData>

    val componentStateFlow: Flow<CardComponentState>

    val exceptionFlow: Flow<CheckoutException>

    fun updateInputData(update: CardInputData.() -> Unit)
}

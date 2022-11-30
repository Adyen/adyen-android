/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/7/2022.
 */

package com.adyen.checkout.giftcard

import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.UiStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.flow.Flow

interface GiftCardDelegate :
    PaymentComponentDelegate<GiftCardComponentState>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UiStateDelegate {

    val outputData: GiftCardOutputData

    val outputDataFlow: Flow<GiftCardOutputData>

    val componentStateFlow: Flow<GiftCardComponentState>

    val exceptionFlow: Flow<CheckoutException>

    fun updateInputData(update: GiftCardInputData.() -> Unit)
}

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/7/2022.
 */

package com.adyen.checkout.giftcard.internal.ui

import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardInputData
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardOutputData
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface GiftCardDelegate :
    PaymentComponentDelegate<GiftCardComponentState>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    val outputData: GiftCardOutputData

    val outputDataFlow: Flow<GiftCardOutputData>

    val componentStateFlow: Flow<GiftCardComponentState>

    val exceptionFlow: Flow<CheckoutException>

    fun updateInputData(update: GiftCardInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}

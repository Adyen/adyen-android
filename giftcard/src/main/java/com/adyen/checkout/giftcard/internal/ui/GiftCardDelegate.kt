/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/7/2022.
 */

package com.adyen.checkout.giftcard.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardInputData
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardOutputData
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface GiftCardDelegate :
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

    fun resolveBalanceResult(balanceResult: BalanceResult)

    fun resolveOrderResponse(orderResponse: OrderResponse)

    fun isPinRequired(): Boolean
}

/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/2/2023.
 */

package com.adyen.checkout.upi

import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

interface UpiDelegate :
    PaymentComponentDelegate<UpiComponentState>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    val outputData: UpiOutputData

    val outputDataFlow: Flow<UpiOutputData>

    val componentStateFlow: Flow<UpiComponentState>

    fun updateInputData(update: UpiInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}

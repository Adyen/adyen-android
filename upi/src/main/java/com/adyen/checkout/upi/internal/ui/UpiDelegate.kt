/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/2/2023.
 */

package com.adyen.checkout.upi.internal.ui

import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import com.adyen.checkout.upi.internal.ui.model.UpiInputData
import com.adyen.checkout.upi.internal.ui.model.UpiOutputData
import kotlinx.coroutines.flow.Flow

internal interface UpiDelegate :
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

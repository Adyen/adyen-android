/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/7/2022.
 */

package com.adyen.checkout.sepa.internal.ui

import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.sepa.SepaComponentState
import com.adyen.checkout.sepa.internal.ui.model.SepaInputData
import com.adyen.checkout.sepa.internal.ui.model.SepaOutputData
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.old.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface SepaDelegate :
    PaymentComponentDelegate<SepaComponentState>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    val outputData: SepaOutputData

    val outputDataFlow: Flow<SepaOutputData>

    val componentStateFlow: Flow<SepaComponentState>

    fun updateInputData(update: SepaInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}

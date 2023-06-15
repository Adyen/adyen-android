/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/6/2023.
 */

package com.adyen.checkout.atome.internal.ui

import com.adyen.checkout.atome.AtomeComponentState
import com.adyen.checkout.atome.internal.ui.model.AtomeInputData
import com.adyen.checkout.atome.internal.ui.model.AtomeOutputData
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.ui.core.internal.ui.AddressDelegate
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface AtomeDelegate :
    PaymentComponentDelegate<AtomeComponentState>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate,
    AddressDelegate {

    val outputData: AtomeOutputData

    val outputDataFlow: Flow<AtomeOutputData>

    val componentStateFlow: Flow<AtomeComponentState>

    fun updateInputData(update: AtomeInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}

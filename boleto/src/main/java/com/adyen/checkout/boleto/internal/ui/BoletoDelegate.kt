/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui

import com.adyen.checkout.boleto.BoletoComponentState
import com.adyen.checkout.boleto.internal.ui.model.BoletoInputData
import com.adyen.checkout.boleto.internal.ui.model.BoletoOutputData
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.ui.core.old.internal.ui.AddressDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.old.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface BoletoDelegate :
    PaymentComponentDelegate<BoletoComponentState>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate,
    AddressDelegate {

    val outputData: BoletoOutputData

    val outputDataFlow: Flow<BoletoOutputData>

    val componentStateFlow: Flow<BoletoComponentState>

    fun updateInputData(update: BoletoInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}

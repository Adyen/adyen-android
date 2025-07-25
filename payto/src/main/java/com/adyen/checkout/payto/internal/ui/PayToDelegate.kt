/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/2/2025.
 */

package com.adyen.checkout.payto.internal.ui

import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.payto.PayToComponentState
import com.adyen.checkout.payto.internal.ui.model.PayIdTypeModel
import com.adyen.checkout.payto.internal.ui.model.PayToInputData
import com.adyen.checkout.payto.internal.ui.model.PayToOutputData
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface PayToDelegate :
    PaymentComponentDelegate<PayToComponentState>,
    ViewProvidingDelegate {

    val outputData: PayToOutputData

    val outputDataFlow: Flow<PayToOutputData>

    val componentStateFlow: Flow<PayToComponentState>

    fun getPayIdTypes(): List<PayIdTypeModel>

    fun updateInputData(update: PayToInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}

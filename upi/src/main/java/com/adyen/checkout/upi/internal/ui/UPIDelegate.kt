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
import com.adyen.checkout.upi.UPIComponentState
import com.adyen.checkout.upi.internal.ui.model.UPIInputData
import com.adyen.checkout.upi.internal.ui.model.UPIOutputData
import kotlinx.coroutines.flow.Flow

internal interface UPIDelegate :
    PaymentComponentDelegate<UPIComponentState>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    val outputData: UPIOutputData

    val outputDataFlow: Flow<UPIOutputData>

    val componentStateFlow: Flow<UPIComponentState>

    fun updateInputData(update: UPIInputData.() -> Unit)

    fun highlightValidationErrors()

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}

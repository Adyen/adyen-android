/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.old.internal.ui

import com.adyen.checkout.blik.old.BlikComponentState
import com.adyen.checkout.blik.old.internal.ui.model.BlikInputData
import com.adyen.checkout.blik.old.internal.ui.model.BlikOutputData
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.old.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface BlikDelegate :
    PaymentComponentDelegate<BlikComponentState>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    val outputData: BlikOutputData

    val outputDataFlow: Flow<BlikOutputData>

    val componentStateFlow: Flow<BlikComponentState>

    fun updateInputData(update: BlikInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}

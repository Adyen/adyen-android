/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/7/2022.
 */

package com.adyen.checkout.blik.internal.ui

import com.adyen.checkout.blik.BlikComponentState
import com.adyen.checkout.blik.internal.ui.model.BlikInputData
import com.adyen.checkout.blik.internal.ui.model.BlikOutputData
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

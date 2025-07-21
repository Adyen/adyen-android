/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/7/2022.
 */

package com.adyen.checkout.bacs.internal.ui

import com.adyen.checkout.bacs.BacsDirectDebitComponentState
import com.adyen.checkout.bacs.BacsDirectDebitMode
import com.adyen.checkout.bacs.internal.ui.model.BacsDirectDebitInputData
import com.adyen.checkout.bacs.internal.ui.model.BacsDirectDebitOutputData
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.old.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface BacsDirectDebitDelegate :
    PaymentComponentDelegate<BacsDirectDebitComponentState>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    override val componentParams: ButtonComponentParams

    val outputData: BacsDirectDebitOutputData

    val outputDataFlow: Flow<BacsDirectDebitOutputData>

    val componentStateFlow: Flow<BacsDirectDebitComponentState>

    fun setMode(mode: BacsDirectDebitMode): Boolean

    fun updateInputData(update: BacsDirectDebitInputData.() -> Unit)

    fun handleBackPress(): Boolean

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}

/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui

import com.adyen.checkout.ach.ACHDirectDebitComponentState
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitInputData
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitOutputData
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.ui.core.old.internal.ui.AddressDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface ACHDirectDebitDelegate :
    PaymentComponentDelegate<ACHDirectDebitComponentState>,
    ViewProvidingDelegate,
    AddressDelegate {
    val outputData: ACHDirectDebitOutputData

    val outputDataFlow: Flow<ACHDirectDebitOutputData>

    val componentStateFlow: Flow<ACHDirectDebitComponentState>

    val exceptionFlow: Flow<CheckoutException>

    fun updateInputData(update: ACHDirectDebitInputData.() -> Unit)
}

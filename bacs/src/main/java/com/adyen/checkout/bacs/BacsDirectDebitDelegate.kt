/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/7/2022.
 */

package com.adyen.checkout.bacs

import com.adyen.checkout.components.base.PaymentMethodDelegate
import kotlinx.coroutines.flow.Flow

interface BacsDirectDebitDelegate :
    PaymentMethodDelegate<
        BacsDirectDebitConfiguration,
        BacsDirectDebitInputData,
        BacsDirectDebitOutputData,
        BacsDirectDebitComponentState
        > {

    val outputDataFlow: Flow<BacsDirectDebitOutputData?>
    val componentStateFlow: Flow<BacsDirectDebitComponentState?>
}

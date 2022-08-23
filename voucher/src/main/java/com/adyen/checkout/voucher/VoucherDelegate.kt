/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.voucher

import com.adyen.checkout.components.model.payments.response.VoucherAction
import kotlinx.coroutines.flow.Flow

interface VoucherDelegate {

    val outputDataFlow: Flow<VoucherOutputData?>

    val outputData: VoucherOutputData?

    fun handleAction(action: VoucherAction)
}

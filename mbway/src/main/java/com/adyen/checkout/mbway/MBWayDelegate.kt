/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/7/2022.
 */

package com.adyen.checkout.mbway

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod
import kotlinx.coroutines.flow.Flow

interface MBWayDelegate :
    PaymentMethodDelegate<
        MBWayConfiguration,
        MBWayInputData,
        MBWayOutputData,
        PaymentComponentState<MBWayPaymentMethod>
        > {

    val outputDataFlow: Flow<MBWayOutputData?>

    val componentStateFlow: Flow<PaymentComponentState<MBWayPaymentMethod>?>
}

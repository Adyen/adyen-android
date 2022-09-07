/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.voucher

import android.app.Activity
import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.components.model.payments.response.VoucherAction
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultVoucherDelegate : VoucherDelegate {

    private val _outputDataFlow = MutableStateFlow<VoucherOutputData?>(null)
    override val outputDataFlow: Flow<VoucherOutputData?> = _outputDataFlow

    private val _exceptionFlow: MutableSharedFlow<CheckoutException> = MutableSingleEventSharedFlow()
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    override val outputData: VoucherOutputData? get() = _outputDataFlow.value

    override fun handleAction(action: VoucherAction, activity: Activity) {
        _outputDataFlow.tryEmit(
            VoucherOutputData(
                isValid = true,
                paymentMethodType = action.paymentMethodType,
                downloadUrl = action.url
            )
        )
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}

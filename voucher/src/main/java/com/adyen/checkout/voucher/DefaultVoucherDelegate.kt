/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.voucher

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.base.GenericComponentParams
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.VoucherAction
import com.adyen.checkout.components.repository.ActionObserverRepository
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class DefaultVoucherDelegate(
    private val observerRepository: ActionObserverRepository,
    override val componentParams: GenericComponentParams,
) : VoucherDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<VoucherOutputData> = _outputDataFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val outputData: VoucherOutputData get() = _outputDataFlow.value

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(VoucherComponentViewType)

    override fun getViewProvider(): ViewProvider = VoucherViewProvider

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) {
        observerRepository.addObservers(
            detailsFlow = null,
            exceptionFlow = exceptionFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun handleAction(action: Action, activity: Activity) {
        if (action !is VoucherAction) {
            exceptionChannel.trySend(ComponentException("Unsupported action"))
            return
        }

        _outputDataFlow.tryEmit(
            VoucherOutputData(
                isValid = true,
                paymentMethodType = action.paymentMethodType,
                downloadUrl = action.url
            )
        )
    }

    private fun createOutputData() = VoucherOutputData(
        isValid = false,
        paymentMethodType = null,
        downloadUrl = null
    )

    override fun onCleared() {
        removeObserver()
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}

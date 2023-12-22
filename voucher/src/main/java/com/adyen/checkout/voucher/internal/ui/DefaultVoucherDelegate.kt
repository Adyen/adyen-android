/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.voucher.internal.ui

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.util.PdfOpener
import com.adyen.checkout.voucher.internal.ui.model.VoucherOutputData
import com.adyen.checkout.voucher.internal.ui.model.VoucherPaymentMethodConfig
import com.adyen.checkout.voucher.internal.ui.model.getInformationFields
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class DefaultVoucherDelegate(
    private val observerRepository: ActionObserverRepository,
    override val componentParams: GenericComponentParams,
    private val pdfOpener: PdfOpener,
) : VoucherDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<VoucherOutputData> = _outputDataFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val outputData: VoucherOutputData get() = _outputDataFlow.value

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override fun initialize(coroutineScope: CoroutineScope) {
        // no ops
    }

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

        val config = VoucherPaymentMethodConfig.getByPaymentMethodType(action.paymentMethodType)
        if (config == null) {
            exceptionChannel.trySend(
                ComponentException("Payment method ${action.paymentMethodType} not supported for this action")
            )
            return
        }

        _viewFlow.tryEmit(config.viewType)

        createOutputData(action, config)
    }

    private fun createOutputData(
        action: VoucherAction,
        config: VoucherPaymentMethodConfig
    ) {
        val informationFields = config.getInformationFields(action, componentParams.shopperLocale)
        val outputData = VoucherOutputData(
            isValid = true,
            paymentMethodType = action.paymentMethodType,
            // TODO: remove action.url when it's fixed from backend side
            downloadUrl = action.downloadUrl ?: action.url,
            expiresAt = action.expiresAt,
            reference = action.reference,
            totalAmount = action.totalAmount,
            introductionTextResource = config.introductionTextResource,
            informationFields = informationFields
        )
        _outputDataFlow.tryEmit(outputData)
    }

    private fun createOutputData() = VoucherOutputData(
        isValid = false,
        paymentMethodType = null,
        downloadUrl = null,
        expiresAt = null,
        reference = null,
        totalAmount = null,
        introductionTextResource = null,
        informationFields = null,
    )

    override fun downloadVoucher(context: Context) {
        try {
            pdfOpener.open(context, outputData.downloadUrl ?: "")
        } catch (e: IllegalStateException) {
            exceptionChannel.trySend(CheckoutException(e.message ?: "", e.cause))
        }
    }

    override fun onCleared() {
        removeObserver()
    }
}

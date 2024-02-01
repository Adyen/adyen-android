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
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PermissionRequestParams
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.util.DateUtils
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.exception.PermissionRequestException
import com.adyen.checkout.core.internal.ui.PermissionHandlerCallback
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.util.ImageSaver
import com.adyen.checkout.ui.core.internal.util.PdfOpener
import com.adyen.checkout.voucher.internal.ui.model.VoucherOutputData
import com.adyen.checkout.voucher.internal.ui.model.VoucherPaymentMethodConfig
import com.adyen.checkout.voucher.internal.ui.model.VoucherStoreAction
import com.adyen.checkout.voucher.internal.ui.model.VoucherUIEvent
import com.adyen.checkout.voucher.internal.ui.model.getInformationFields
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date

internal class DefaultVoucherDelegate(
    private val observerRepository: ActionObserverRepository,
    override val componentParams: GenericComponentParams,
    private val pdfOpener: PdfOpener,
    private val imageSaver: ImageSaver,
) : VoucherDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<VoucherOutputData> = _outputDataFlow

    private val permissionChannel: Channel<PermissionRequestParams> = bufferedChannel()
    override val permissionFlow: Flow<PermissionRequestParams> = permissionChannel.receiveAsFlow()

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val outputData: VoucherOutputData get() = _outputDataFlow.value

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private val eventChannel: Channel<VoucherUIEvent> = bufferedChannel()
    override val eventFlow: Flow<VoucherUIEvent> = eventChannel.receiveAsFlow()

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) {
        observerRepository.addObservers(
            detailsFlow = null,
            permissionFlow = permissionFlow,
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
        // TODO: remove action.url when it's fixed from backend side
        val downloadUrl = action.downloadUrl ?: action.url
        val storeAction = downloadUrl?.let { url ->
            VoucherStoreAction.DownloadPdf(url)
        } ?: VoucherStoreAction.SaveAsImage
        val informationFields = config.getInformationFields(action, componentParams.shopperLocale)

        val outputData = VoucherOutputData(
            isValid = true,
            paymentMethodType = action.paymentMethodType,
            introductionTextResource = config.introductionTextResource,
            expiresAt = action.expiresAt,
            reference = action.reference,
            totalAmount = action.totalAmount,
            storeAction = storeAction,
            informationFields = informationFields,
        )
        _outputDataFlow.tryEmit(outputData)
    }

    private fun createOutputData() = VoucherOutputData(
        isValid = false,
        paymentMethodType = null,
        introductionTextResource = null,
        expiresAt = null,
        reference = null,
        totalAmount = null,
        storeAction = null,
        informationFields = null,
    )

    override fun downloadVoucher(context: Context) {
        val downloadUrl = (outputData.storeAction as? VoucherStoreAction.DownloadPdf)?.downloadUrl ?: ""
        try {
            pdfOpener.open(context, downloadUrl)
        } catch (e: IllegalStateException) {
            exceptionChannel.trySend(CheckoutException(e.message ?: "", e.cause))
        }
    }

    override fun saveVoucherAsImage(context: Context, view: View) {
        val paymentMethodType = outputData.paymentMethodType ?: ""
        val timestamp = DateUtils.formatDateToString(Date(), componentParams.shopperLocale)
        val imageName = String.format(IMAGE_NAME_FORMAT, paymentMethodType, timestamp)

        coroutineScope.launch {
            imageSaver.saveImageFromView(
                context = context,
                permissionHandler = this@DefaultVoucherDelegate,
                view = view,
                fileName = imageName
            ).fold(
                onSuccess = {
                    eventChannel.trySend(VoucherUIEvent.Success)
                },
                onFailure = { throwable ->
                    when (throwable) {
                        is PermissionRequestException -> eventChannel.trySend(VoucherUIEvent.PermissionDenied)
                        else -> eventChannel.trySend(VoucherUIEvent.Failure(throwable))
                    }
                }
            )
        }
    }

    override fun requestPermission(context: Context, requiredPermission: String, callback: PermissionHandlerCallback) {
        val requestParams = PermissionRequestParams(requiredPermission, callback)
        permissionChannel.trySend(requestParams)
    }

    override fun onCleared() {
        removeObserver()
        _coroutineScope = null
    }

    companion object {
        private const val IMAGE_NAME_FORMAT = "%s-%s.png"
    }
}

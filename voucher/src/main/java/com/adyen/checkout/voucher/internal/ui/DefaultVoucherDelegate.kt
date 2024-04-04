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
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PermissionRequestData
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.util.DateUtils
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.PermissionHandlerCallback
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.exception.PermissionRequestException
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
import java.util.Calendar

@Suppress("TooManyFunctions")
internal class DefaultVoucherDelegate(
    private val observerRepository: ActionObserverRepository,
    override val savedStateHandle: SavedStateHandle,
    override val componentParams: GenericComponentParams,
    private val pdfOpener: PdfOpener,
    private val imageSaver: ImageSaver,
) : VoucherDelegate, SavedStateHandleContainer {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<VoucherOutputData> = _outputDataFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    private val permissionChannel: Channel<PermissionRequestData> = bufferedChannel()
    override val permissionFlow: Flow<PermissionRequestData> = permissionChannel.receiveAsFlow()

    override val outputData: VoucherOutputData get() = _outputDataFlow.value

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private val eventChannel: Channel<VoucherUIEvent> = bufferedChannel()
    override val eventFlow: Flow<VoucherUIEvent> = eventChannel.receiveAsFlow()

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var action: Action? by SavedStateHandleProperty(ACTION_KEY)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        restoreState()
    }

    private fun restoreState() {
        adyenLog(AdyenLogLevel.DEBUG) { "Restoring state" }
        val action: Action? = action
        if (action != null) {
            handleAction(action)
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) {
        observerRepository.addObservers(
            detailsFlow = null,
            exceptionFlow = exceptionFlow,
            permissionFlow = permissionFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun handleAction(action: Action, activity: Activity) {
        this.action = action
        handleAction(action)
    }

    private fun handleAction(action: Action) {
        if (action !is VoucherAction) {
            exceptionChannel.trySend(ComponentException("Unsupported action"))
            return
        }

        val config = VoucherPaymentMethodConfig.getByPaymentMethodType(action.paymentMethodType)
        if (config == null) {
            exceptionChannel.trySend(
                ComponentException("Payment method ${action.paymentMethodType} not supported for this action"),
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
        // TODO remove action.url when it's fixed from backend side
        val downloadUrl = action.downloadUrl ?: action.url
        val storeAction = downloadUrl?.let { url ->
            VoucherStoreAction.DownloadPdf(url)
        } ?: VoucherStoreAction.SaveAsImage
        val informationFields = config.getInformationFields(action, componentParams.shopperLocale)

        val outputData = VoucherOutputData(
            isValid = true,
            paymentMethodType = action.paymentMethodType,
            introductionTextResource = config.introductionTextResource,
            reference = action.reference,
            totalAmount = action.totalAmount,
            storeAction = storeAction,
            instructionUrl = action.instructionsUrl,
            informationFields = informationFields,
        )
        _outputDataFlow.tryEmit(outputData)
    }

    private fun createOutputData() = VoucherOutputData(
        isValid = false,
        paymentMethodType = null,
        introductionTextResource = null,
        reference = null,
        totalAmount = null,
        storeAction = null,
        instructionUrl = null,
        informationFields = null,
    )

    override fun downloadVoucher(context: Context) {
        val downloadUrl = (outputData.storeAction as? VoucherStoreAction.DownloadPdf)?.downloadUrl ?: ""
        try {
            pdfOpener.open(context, downloadUrl)
        } catch (e: IllegalStateException) {
            exceptionChannel.trySend(ComponentException(e.message ?: "", e.cause))
        }
    }

    override fun saveVoucherAsImage(context: Context, view: View) {
        val paymentMethodType = outputData.paymentMethodType ?: ""
        val timestamp = DateUtils.formatDateToString(Calendar.getInstance())
        val imageName = String.format(IMAGE_NAME_FORMAT, paymentMethodType, timestamp)

        coroutineScope.launch {
            imageSaver.saveImageFromView(
                context = context,
                permissionHandler = this@DefaultVoucherDelegate,
                view = view,
                fileName = imageName,
            ).fold(
                onSuccess = {
                    eventChannel.trySend(VoucherUIEvent.Success)
                },
                onFailure = { throwable ->
                    when (throwable) {
                        is PermissionRequestException -> eventChannel.trySend(VoucherUIEvent.PermissionDenied)
                        else -> eventChannel.trySend(VoucherUIEvent.Failure(throwable))
                    }
                },
            )
        }
    }

    override fun requestPermission(context: Context, requiredPermission: String, callback: PermissionHandlerCallback) {
        val requestData = PermissionRequestData(requiredPermission, callback)
        permissionChannel.trySend(requestData)
    }

    override fun onCleared() {
        removeObserver()
        _coroutineScope = null
    }

    companion object {
        private const val IMAGE_NAME_FORMAT = "%s-%s.png"
        private const val ACTION_KEY = "ACTION_KEY"
    }
}

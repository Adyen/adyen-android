/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.voucher.internal.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.view.View
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.exception.PermissionException
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ImageSaver
import com.adyen.checkout.ui.core.internal.util.PdfOpener
import com.adyen.checkout.voucher.internal.ui.model.VoucherOutputData
import com.adyen.checkout.voucher.internal.ui.model.VoucherPaymentMethodConfig
import com.adyen.checkout.voucher.internal.ui.model.VoucherStoreAction
import com.adyen.checkout.voucher.internal.ui.model.getInformationFields
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class DefaultVoucherDelegate(
    private val observerRepository: ActionObserverRepository,
    override val componentParams: GenericComponentParams,
    private val pdfOpener: PdfOpener,
    private val imageSaver: ImageSaver,
) : VoucherDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<VoucherOutputData> = _outputDataFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val outputData: VoucherOutputData get() = _outputDataFlow.value

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

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
        // TODO: Look for an option to directly save the image after user accepts the permission
        val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, requiredPermission) != PackageManager.PERMISSION_GRANTED
        ) {
            exceptionChannel.trySend(
                PermissionException(
                    errorMessage = "$requiredPermission permission is not granted",
                    requiredPermission = requiredPermission
                )
            )
            return
        }

        saveImageFromView(context, view)
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private fun saveImageFromView(context: Context, view: View) {
        val timestamp = System.currentTimeMillis()
        val imageDirectory = "${Environment.DIRECTORY_PICTURES}/$IMAGE_RELATIVE_PATH"
        val imageName = String.format(IMAGE_NAME_FORMAT, timestamp)

        coroutineScope.launch {
            imageSaver.saveImageFromView(context, view, imageDirectory, imageName).fold(
                onSuccess = {
                    // TODO: To be implemented
                },
                onFailure = {
                    // TODO: To be implemented
                }
            )
        }
    }

    override fun onCleared() {
        removeObserver()
        _coroutineScope = null
    }

    companion object {
        private const val IMAGE_RELATIVE_PATH = "Voucher"
        private const val IMAGE_NAME_FORMAT = "Voucher-%s.png"
    }
}

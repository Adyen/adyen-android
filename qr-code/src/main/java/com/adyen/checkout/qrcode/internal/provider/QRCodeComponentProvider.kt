/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.qrcode.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.QrCodeAction
import com.adyen.checkout.components.core.ActionComponentCallback
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.DefaultActionComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.data.api.DefaultStatusRepository
import com.adyen.checkout.components.core.internal.data.api.StatusService
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.internal.util.FileDownloader
import com.adyen.checkout.qrcode.QRCodeComponent
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.qrcode.internal.QRCodeCountDownTimer
import com.adyen.checkout.qrcode.internal.ui.DefaultQRCodeDelegate
import com.adyen.checkout.qrcode.internal.ui.QRCodeDelegate
import com.adyen.checkout.ui.core.internal.DefaultRedirectHandler

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class QRCodeComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
) : ActionComponentProvider<QRCodeComponent, QRCodeConfiguration, QRCodeDelegate> {

    private val componentParamsMapper = GenericComponentParamsMapper(overrideComponentParams, overrideSessionParams)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: QRCodeConfiguration,
        callback: ActionComponentCallback,
        key: String?,
    ): QRCodeComponent {
        val qrCodeFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val qrCodeDelegate = getDelegate(configuration, savedStateHandle, application)
            QRCodeComponent(
                delegate = qrCodeDelegate,
                actionComponentEventHandler = DefaultActionComponentEventHandler(callback)
            )
        }
        return ViewModelProvider(viewModelStoreOwner, qrCodeFactory)[key, QRCodeComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.actionComponentEventHandler::onActionComponentEvent)
            }
    }

    override fun getDelegate(
        configuration: QRCodeConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): QRCodeDelegate {
        val componentParams = componentParamsMapper.mapToParams(configuration, null)
        val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
        val statusService = StatusService(httpClient)
        val statusRepository = DefaultStatusRepository(statusService, configuration.clientKey)
        val countDownTimer = QRCodeCountDownTimer()
        val redirectHandler = DefaultRedirectHandler()
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)

        return DefaultQRCodeDelegate(
            observerRepository = ActionObserverRepository(),
            componentParams = componentParams,
            statusRepository = statusRepository,
            statusCountDownTimer = countDownTimer,
            redirectHandler = redirectHandler,
            paymentDataRepository = paymentDataRepository,
            fileDownloader = FileDownloader(application)
        )
    }

    override val supportedActionTypes: List<String>
        get() = listOf(QrCodeAction.ACTION_TYPE)

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type)
    }

    override fun providesDetails(action: Action): Boolean {
        return true
    }
}

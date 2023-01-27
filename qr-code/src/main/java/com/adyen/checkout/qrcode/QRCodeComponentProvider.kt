/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.qrcode

import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.ActionComponentCallback
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.base.DefaultActionComponentEventHandler
import com.adyen.checkout.components.base.GenericComponentParamsMapper
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.handler.DefaultRedirectHandler
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.components.repository.ActionObserverRepository
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.components.status.DefaultStatusRepository
import com.adyen.checkout.components.status.api.StatusService
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.util.FileDownloader

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class QRCodeComponentProvider(
    overrideComponentParams: ComponentParams? = null
) : ActionComponentProvider<QRCodeComponent, QRCodeConfiguration, QRCodeDelegate> {

    private val componentParamsMapper = GenericComponentParamsMapper(overrideComponentParams)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: QRCodeConfiguration,
        callback: ActionComponentCallback,
        defaultArgs: Bundle?,
        key: String?,
    ): QRCodeComponent {
        val qrCodeFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
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
        val componentParams = componentParamsMapper.mapToParams(configuration)
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

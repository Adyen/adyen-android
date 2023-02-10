/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.wechatpay

import android.app.Application
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
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.SdkAction
import com.adyen.checkout.components.repository.ActionObserverRepository
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class WeChatPayActionComponentProvider(
    private val overrideComponentParams: ComponentParams? = null
) : ActionComponentProvider<WeChatPayActionComponent, WeChatPayActionConfiguration, WeChatDelegate> {

    private val componentParamsMapper = GenericComponentParamsMapper()

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: WeChatPayActionConfiguration,
        callback: ActionComponentCallback,
        key: String?,
    ): WeChatPayActionComponent {
        val weChatFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val weChatDelegate = getDelegate(configuration, savedStateHandle, application)
            WeChatPayActionComponent(
                delegate = weChatDelegate,
                actionComponentEventHandler = DefaultActionComponentEventHandler(callback)
            )
        }

        return ViewModelProvider(viewModelStoreOwner, weChatFactory)[key, WeChatPayActionComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.actionComponentEventHandler::onActionComponentEvent)
            }
    }

    override fun getDelegate(
        configuration: WeChatPayActionConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): WeChatDelegate {
        val componentParams = componentParamsMapper.mapToParams(configuration, overrideComponentParams)
        val iwxApi: IWXAPI = WXAPIFactory.createWXAPI(application, null, true)
        val requestGenerator = WeChatPayRequestGenerator()
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        return DefaultWeChatDelegate(
            observerRepository = ActionObserverRepository(),
            componentParams = componentParams,
            iwxApi = iwxApi,
            payRequestGenerator = requestGenerator,
            paymentDataRepository = paymentDataRepository
        )
    }

    override val supportedActionTypes: List<String>
        get() = listOf(SdkAction.ACTION_TYPE)

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type) && PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun providesDetails(action: Action): Boolean {
        return true
    }

    companion object {
        private val PAYMENT_METHODS = listOf(PaymentMethodTypes.WECHAT_PAY_SDK)
    }
}

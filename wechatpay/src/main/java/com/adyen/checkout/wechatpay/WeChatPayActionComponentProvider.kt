/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.wechatpay

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.SdkAction
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

private val PAYMENT_METHODS = listOf(PaymentMethodTypes.WECHAT_PAY_SDK)

class WeChatPayActionComponentProvider :
    ActionComponentProvider<WeChatPayActionComponent, WeChatPayActionConfiguration, WeChatDelegate> {

    override fun <T> get(
        owner: T,
        application: Application,
        configuration: WeChatPayActionConfiguration
    ): WeChatPayActionComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, application, configuration, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: WeChatPayActionConfiguration,
        defaultArgs: Bundle?
    ): WeChatPayActionComponent {
        val weChatFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            val weChatDelegate = getDelegate(configuration, savedStateHandle, application)
            WeChatPayActionComponent(
                savedStateHandle = savedStateHandle,
                application = application,
                configuration = configuration,
                weChatDelegate = weChatDelegate,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, weChatFactory).get(WeChatPayActionComponent::class.java)
    }

    override fun getDelegate(
        configuration: WeChatPayActionConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): WeChatDelegate {
        val iwxApi: IWXAPI = WXAPIFactory.createWXAPI(application, null, true)
        val requestGenerator = WeChatPayRequestGenerator()
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        return DefaultWeChatDelegate(iwxApi, requestGenerator, paymentDataRepository)
    }

    override val supportedActionTypes: List<String>
        get() = listOf(SdkAction.ACTION_TYPE)

    @Deprecated(
        message = "You can safely remove this method, it will always return true as all action components require " +
            "a configuration.",
        replaceWith = ReplaceWith("true")
    )
    override fun requiresConfiguration(): Boolean = true

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type) && PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun requiresView(action: Action): Boolean = false

    override fun providesDetails(): Boolean {
        return true
    }
}

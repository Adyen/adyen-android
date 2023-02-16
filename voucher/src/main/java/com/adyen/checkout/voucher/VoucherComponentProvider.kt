/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/11/2021.
 */

package com.adyen.checkout.voucher

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
import com.adyen.checkout.components.model.payments.response.VoucherAction
import com.adyen.checkout.components.repository.ActionObserverRepository
import com.adyen.checkout.components.util.PaymentMethodTypes

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class VoucherComponentProvider(
    private val overrideComponentParams: ComponentParams? = null
) : ActionComponentProvider<VoucherComponent, VoucherConfiguration, VoucherDelegate> {

    private val componentParamsMapper = GenericComponentParamsMapper()

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: VoucherConfiguration,
        callback: ActionComponentCallback,
        key: String?,
    ): VoucherComponent {
        val voucherFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val voucherDelegate = getDelegate(configuration, savedStateHandle, application)
            VoucherComponent(
                delegate = voucherDelegate,
                actionComponentEventHandler = DefaultActionComponentEventHandler(callback),
            )
        }
        return ViewModelProvider(viewModelStoreOwner, voucherFactory)[key, VoucherComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.actionComponentEventHandler::onActionComponentEvent)
            }
    }

    override fun getDelegate(
        configuration: VoucherConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): VoucherDelegate {
        val componentParams = componentParamsMapper.mapToParams(configuration, overrideComponentParams)
        return DefaultVoucherDelegate(ActionObserverRepository(), componentParams)
    }

    override val supportedActionTypes: List<String>
        get() = listOf(VoucherAction.ACTION_TYPE)

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type) && PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun providesDetails(action: Action): Boolean {
        return false
    }

    companion object {
        private val PAYMENT_METHODS = listOf(PaymentMethodTypes.BACS)
    }
}

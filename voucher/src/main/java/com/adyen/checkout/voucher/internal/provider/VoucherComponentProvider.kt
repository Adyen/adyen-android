/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/11/2021.
 */

package com.adyen.checkout.voucher.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.core.ActionComponentCallback
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.DefaultActionComponentEventHandler
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.voucher.VoucherComponent
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.voucher.internal.ui.DefaultVoucherDelegate
import com.adyen.checkout.voucher.internal.ui.VoucherDelegate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class VoucherComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
) : ActionComponentProvider<VoucherComponent, VoucherConfiguration, VoucherDelegate> {

    private val componentParamsMapper = GenericComponentParamsMapper(overrideComponentParams, overrideSessionParams)

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
        val componentParams = componentParamsMapper.mapToParams(configuration, null)
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
        private val PAYMENT_METHODS = listOf(
            PaymentMethodTypes.BACS,
            PaymentMethodTypes.BOLETOBANCARIO,
            PaymentMethodTypes.BOLETOBANCARIO_BANCODOBRASIL,
            PaymentMethodTypes.BOLETOBANCARIO_BRADESCO,
            PaymentMethodTypes.BOLETOBANCARIO_HSBC,
            PaymentMethodTypes.BOLETOBANCARIO_ITAU,
            PaymentMethodTypes.BOLETOBANCARIO_SANTANDER,
            PaymentMethodTypes.BOLETO_PRIMEIRO_PAY
        )
    }
}

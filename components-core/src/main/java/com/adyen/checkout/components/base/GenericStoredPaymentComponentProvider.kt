/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 8/12/2020.
 */

package com.adyen.checkout.components.base

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.PaymentComponentViewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod

class GenericStoredPaymentComponentProvider<
    BaseComponentT : BasePaymentComponent<*, *, *, *>,
    ConfigurationT : Configuration
    >(private val componentClass: Class<BaseComponentT>) : StoredPaymentComponentProvider<BaseComponentT, ConfigurationT> {

    override fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT
    ): BaseComponentT {
        val factory = PaymentComponentViewModelFactory(GenericStoredPaymentDelegate(storedPaymentMethod), configuration)
        return ViewModelProvider(viewModelStoreOwner, factory)[componentClass]
    }

    override fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT
    ): BaseComponentT {
        val factory = PaymentComponentViewModelFactory(GenericPaymentMethodDelegate(paymentMethod), configuration)
        return ViewModelProvider(viewModelStoreOwner, factory)[componentClass]
    }

    override fun isAvailable(
        applicationContext: Application,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        callback: ComponentAvailableCallback<ConfigurationT>
    ) {
        callback.onAvailabilityResult(true, paymentMethod, configuration)
    }
}

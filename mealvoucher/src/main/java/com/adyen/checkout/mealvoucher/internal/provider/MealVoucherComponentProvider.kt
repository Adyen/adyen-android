/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/7/2024.
 */

package com.adyen.checkout.mealvoucher.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.LocaleProvider
import com.adyen.checkout.mealvoucher.MealVoucherComponent
import com.adyen.checkout.mealvoucher.MealVoucherComponentCallback
import com.adyen.checkout.mealvoucher.MealVoucherComponentState
import com.adyen.checkout.mealvoucher.MealVoucherConfiguration
import com.adyen.checkout.mealvoucher.SessionsMealVoucherComponentCallback
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.internal.provider.SessionPaymentComponentProvider

class MealVoucherComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
// TODO remove suppress annotation
@Suppress("UnusedPrivateProperty")
constructor(
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val analyticsManager: AnalyticsManager? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) :
    PaymentComponentProvider<
        MealVoucherComponent,
        MealVoucherConfiguration,
        MealVoucherComponentState,
        MealVoucherComponentCallback,
        >,
    SessionPaymentComponentProvider<
        MealVoucherComponent,
        MealVoucherConfiguration,
        MealVoucherComponentState,
        SessionsMealVoucherComponentCallback,
        > {
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: MealVoucherComponentCallback,
        order: Order?,
        key: String?
    ): MealVoucherComponent {
        TODO("Not yet implemented")
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: MealVoucherConfiguration,
        application: Application,
        componentCallback: MealVoucherComponentCallback,
        order: Order?,
        key: String?
    ): MealVoucherComponent {
        TODO("Not yet implemented")
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: SessionsMealVoucherComponentCallback,
        key: String?
    ): MealVoucherComponent {
        TODO("Not yet implemented")
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        configuration: MealVoucherConfiguration,
        application: Application,
        componentCallback: SessionsMealVoucherComponentCallback,
        key: String?
    ): MealVoucherComponent {
        TODO("Not yet implemented")
    }

    @Suppress("UnusedPrivateMember")
    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        // TODO create PAYMENT_METHOD_TYPES after extending MealVoucherComponent from GiftCardComponent
        return MealVoucherComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}

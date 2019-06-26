/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/4/2019.
 */

package com.adyen.checkout.dropin

import android.content.Context
import android.support.v4.app.FragmentActivity
import com.adyen.checkout.base.ComponentView
import com.adyen.checkout.base.PaymentComponent
import com.adyen.checkout.base.PaymentComponentProvider
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.CardView
import com.adyen.checkout.dotpay.DotpayComponent
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.dotpay.DotpayRecyclerView
import com.adyen.checkout.entercash.EntercashComponent
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.entercash.EntercashRecyclerView
import com.adyen.checkout.eps.EPSComponent
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.eps.EPSSpinnerView
import com.adyen.checkout.ideal.IdealComponent
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.ideal.IdealRecyclerView
import com.adyen.checkout.molpay.MolpayComponent
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.molpay.MolpayRecyclerView
import com.adyen.checkout.openbanking.OpenBankingComponent
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.openbanking.OpenBankingRecyclerView

/**
 * Provides [PaymentComponent] and [ComponentView] based on parsing the [PaymentMethod] object.
 */
internal class ComponentParsingProvider {

    companion object {
        /**
         * Provides a [PaymentComponent] from a [PaymentComponentProvider] using the [PaymentMethod] reference.
         *
         * @param activity The Activity which the PaymentComponent will be bound to.
         * @param paymentMethod The payment method to be parsed.
         */
        internal fun getComponentFor(activity: FragmentActivity, paymentMethod: PaymentMethod): PaymentComponent? {
            val dropInConfig = DropIn.INSTANCE.configuration

            val component = when (paymentMethod.type) {
                PaymentMethodTypes.IDEAL -> {
                    val idealConfig = dropInConfig.getConfigurationFor(PaymentMethodTypes.IDEAL)
                            ?: IdealConfiguration(dropInConfig.shopperLocale, dropInConfig.displayMetrics, dropInConfig.environment)
                    IdealComponent.PROVIDER.get(activity, paymentMethod, idealConfig)
                }
                PaymentMethodTypes.MOLPAY -> {
                    val molpayConfig = dropInConfig.getConfigurationFor(PaymentMethodTypes.MOLPAY)
                            ?: MolpayConfiguration(dropInConfig.shopperLocale, dropInConfig.displayMetrics, dropInConfig.environment)
                    MolpayComponent.PROVIDER.get(activity, paymentMethod, molpayConfig)
                }
                PaymentMethodTypes.EPS -> {
                    val epsConfig = dropInConfig.getConfigurationFor(PaymentMethodTypes.EPS)
                            ?: EPSConfiguration(dropInConfig.shopperLocale, dropInConfig.displayMetrics, dropInConfig.environment)
                    EPSComponent.PROVIDER.get(activity, paymentMethod, epsConfig)
                }
                PaymentMethodTypes.OPEN_BANKING -> {
                    val openBankingConfig = dropInConfig.getConfigurationFor(PaymentMethodTypes.OPEN_BANKING)
                            ?: OpenBankingConfiguration(dropInConfig.shopperLocale, dropInConfig.displayMetrics, dropInConfig.environment)
                    OpenBankingComponent.PROVIDER.get(activity, paymentMethod, openBankingConfig)
                }
                PaymentMethodTypes.DOTPAY -> {
                    val dotpayConfig = dropInConfig.getConfigurationFor(PaymentMethodTypes.DOTPAY)
                            ?: DotpayConfiguration(dropInConfig.shopperLocale, dropInConfig.displayMetrics, dropInConfig.environment)
                    DotpayComponent.PROVIDER.get(activity, paymentMethod, dotpayConfig)
                }
                PaymentMethodTypes.ENTERCASH -> {
                    val entercashConfig = dropInConfig.getConfigurationFor(PaymentMethodTypes.ENTERCASH)
                            ?: EntercashConfiguration(dropInConfig.shopperLocale, dropInConfig.displayMetrics, dropInConfig.environment)
                    EntercashComponent.PROVIDER.get(activity, paymentMethod, entercashConfig)
                }
                PaymentMethodTypes.SCHEME -> {
                    // Fallback default will not work because we don't have the public key
                    val cardConfig = dropInConfig.getConfigurationFor(PaymentMethodTypes.SCHEME)
                            ?: CardConfiguration.getDefault(dropInConfig.shopperLocale, dropInConfig.displayMetrics,
                                    dropInConfig.environment, "")
                    CardComponent.PROVIDER.get(activity, paymentMethod, cardConfig)
                }
                else -> {
                    null
                }
            }
            component?.setCreatedForDropIn()
            return component
        }

        /**
         * Provides a [ComponentView] using the [PaymentMethod] reference.
         *
         * @param context The context used to create the View
         * @param paymentMethod The payment method to be parsed.
         */
        internal fun getViewFor(context: Context, paymentMethod: PaymentMethod): ComponentView<PaymentComponent>? {
            return when (paymentMethod.type) {
                PaymentMethodTypes.IDEAL -> {
                    // Choose view type for the component
                    @Suppress("UNCHECKED_CAST")
                    IdealRecyclerView(context) as ComponentView<PaymentComponent>
//                    IdealSpinnerView(context) as ComponentView<PaymentComponent>
                }
                PaymentMethodTypes.MOLPAY -> {
                    // Choose view type for the component
                    @Suppress("UNCHECKED_CAST")
                    MolpayRecyclerView(context) as ComponentView<PaymentComponent>
                }
                PaymentMethodTypes.EPS -> {
                    // Choose view type for the component
                    @Suppress("UNCHECKED_CAST")
                    EPSSpinnerView(context) as ComponentView<PaymentComponent>
                }
                PaymentMethodTypes.DOTPAY -> {
                    // Choose view type for the component
                    @Suppress("UNCHECKED_CAST")
                    DotpayRecyclerView(context) as ComponentView<PaymentComponent>
                }
                PaymentMethodTypes.OPEN_BANKING -> {
                    // Choose view type for the component
                    @Suppress("UNCHECKED_CAST")
                    OpenBankingRecyclerView(context) as ComponentView<PaymentComponent>
                }
                PaymentMethodTypes.ENTERCASH -> {
                    // Choose view type for the component
                    @Suppress("UNCHECKED_CAST")
                    EntercashRecyclerView(context) as ComponentView<PaymentComponent>
                }
                PaymentMethodTypes.SCHEME -> {
                    @Suppress("UNCHECKED_CAST")
                    CardView(context) as ComponentView<PaymentComponent>
                }
                else -> {
                    null
                }
            }
        }
    }
}

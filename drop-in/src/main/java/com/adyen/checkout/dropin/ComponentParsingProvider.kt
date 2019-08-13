/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/4/2019.
 */

package com.adyen.checkout.dropin

import android.app.Application
import android.content.Context
import android.support.v4.app.Fragment
import com.adyen.checkout.base.* // ktlint-disable no-wildcard-imports
import com.adyen.checkout.base.component.BaseConfigurationBuilder
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.CardView
import com.adyen.checkout.core.exeption.CheckoutException
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dotpay.DotpayComponent
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.dotpay.DotpayRecyclerView
import com.adyen.checkout.entercash.EntercashComponent
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.entercash.EntercashRecyclerView
import com.adyen.checkout.eps.EPSComponent
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.eps.EPSRecyclerView
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.ideal.IdealComponent
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.ideal.IdealRecyclerView
import com.adyen.checkout.molpay.MolpayComponent
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.molpay.MolpayRecyclerView
import com.adyen.checkout.openbanking.OpenBankingComponent
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.openbanking.OpenBankingRecyclerView

internal fun <T : Configuration> getDefaultConfigFor(@PaymentMethodTypes.SupportedPaymentMethod paymentMethod: String, context: Context): T {

    val specificRequirementConfigs = listOf(PaymentMethodTypes.SCHEME, PaymentMethodTypes.GOOGLE_PAY)

    if (specificRequirementConfigs.contains(paymentMethod)) {
        throw CheckoutException("Cannot provide default config for $paymentMethod. Please add it to the DropInConfiguration with required fields.")
    }

    // get default builder for Configuration type
    val builder: BaseConfigurationBuilder<out Configuration> = when (paymentMethod) {
        PaymentMethodTypes.IDEAL -> {
            IdealConfiguration.Builder(context)
        }
        PaymentMethodTypes.MOLPAY -> {
            MolpayConfiguration.Builder(context)
        }
        PaymentMethodTypes.EPS -> {
            EPSConfiguration.Builder(context)
        }
        PaymentMethodTypes.OPEN_BANKING -> {
            OpenBankingConfiguration.Builder(context)
        }
        PaymentMethodTypes.DOTPAY -> {
            DotpayConfiguration.Builder(context)
        }
        PaymentMethodTypes.ENTERCASH -> {
            EntercashConfiguration.Builder(context)
        }
        else -> {
            throw CheckoutException("Unable to find component for type - $paymentMethod")
        }
    }

    // set default values from DropIn
    val dropInConfig = DropIn.INSTANCE.configuration
    builder.setShopperLocale(dropInConfig.shopperLocale)
    builder.setEnvironment(dropInConfig.environment)

    @Suppress("UNCHECKED_CAST")
    return builder.build() as T
}

internal fun checkComponentAvailability(
    application: Application,
    paymentMethod: PaymentMethod,
    callback: ComponentAvailableCallback<in Configuration>
) {
    try {
        val dropInConfig = DropIn.INSTANCE.configuration
        val type = paymentMethod.type ?: throw CheckoutException("PaymentMethod is null")

        val provider = getProviderForType(type)
        val configuration = dropInConfig.getConfigurationFor<Configuration>(type, application)

        provider.isAvailable(application, paymentMethod, configuration, callback)
    } catch (e: CheckoutException) {
        Logger.e("CO.ComponentParsingProvider", "Unable to initiate ${paymentMethod.type}", e)
        callback.onAvailabilityResult(false, paymentMethod, null)
    }
}

@Suppress("ComplexMethod")
internal fun getProviderForType(type: String): PaymentComponentProvider<PaymentComponent, Configuration> {
    @Suppress("UNCHECKED_CAST")
    return when (type) {
        PaymentMethodTypes.IDEAL -> {
            IdealComponent.PROVIDER as PaymentComponentProvider<PaymentComponent, Configuration>
        }
        PaymentMethodTypes.MOLPAY -> {
            MolpayComponent.PROVIDER as PaymentComponentProvider<PaymentComponent, Configuration>
        }
        PaymentMethodTypes.EPS -> {
            EPSComponent.PROVIDER as PaymentComponentProvider<PaymentComponent, Configuration>
        }
        PaymentMethodTypes.OPEN_BANKING -> {
            OpenBankingComponent.PROVIDER as PaymentComponentProvider<PaymentComponent, Configuration>
        }
        PaymentMethodTypes.DOTPAY -> {
            DotpayComponent.PROVIDER as PaymentComponentProvider<PaymentComponent, Configuration>
        }
        PaymentMethodTypes.ENTERCASH -> {
            EntercashComponent.PROVIDER as PaymentComponentProvider<PaymentComponent, Configuration>
        }
        PaymentMethodTypes.SCHEME -> {
            CardComponent.PROVIDER as PaymentComponentProvider<PaymentComponent, Configuration>
        }
        PaymentMethodTypes.GOOGLE_PAY -> {
            GooglePayComponent.PROVIDER as PaymentComponentProvider<PaymentComponent, Configuration>
        }
        else -> {
            throw CheckoutException("Unable to find component for type - $type")
        }
    }
}

/**
 * Provides a [PaymentComponent] from a [PaymentComponentProvider] using the [PaymentMethod] reference.
 *
 * @param fragment The Activity/Fragment which the PaymentComponent lifecycle will be bound to.
 * @param paymentMethod The payment method to be parsed.
 * @throws CheckoutException In case a component cannot be created.
 */
@Suppress("ComplexMethod")
internal fun getComponentFor(fragment: Fragment, paymentMethod: PaymentMethod): PaymentComponent {
    val dropInConfig = DropIn.INSTANCE.configuration

    val component = when (paymentMethod.type) {
        PaymentMethodTypes.IDEAL -> {
            val idealConfig: IdealConfiguration = dropInConfig.getConfigurationFor(PaymentMethodTypes.IDEAL, fragment.context!!)
            IdealComponent.PROVIDER.get(fragment, paymentMethod, idealConfig)
        }
        PaymentMethodTypes.MOLPAY -> {
            val molpayConfig: MolpayConfiguration = dropInConfig.getConfigurationFor(PaymentMethodTypes.MOLPAY, fragment.context!!)
            MolpayComponent.PROVIDER.get(fragment, paymentMethod, molpayConfig)
        }
        PaymentMethodTypes.EPS -> {
            val epsConfig: EPSConfiguration = dropInConfig.getConfigurationFor(PaymentMethodTypes.EPS, fragment.context!!)
            EPSComponent.PROVIDER.get(fragment, paymentMethod, epsConfig)
        }
        PaymentMethodTypes.OPEN_BANKING -> {
            val openBankingConfig: OpenBankingConfiguration = dropInConfig.getConfigurationFor(PaymentMethodTypes.OPEN_BANKING, fragment.context!!)
            OpenBankingComponent.PROVIDER.get(fragment, paymentMethod, openBankingConfig)
        }
        PaymentMethodTypes.DOTPAY -> {
            val dotpayConfig: DotpayConfiguration = dropInConfig.getConfigurationFor(PaymentMethodTypes.DOTPAY, fragment.context!!)
            DotpayComponent.PROVIDER.get(fragment, paymentMethod, dotpayConfig)
        }
        PaymentMethodTypes.ENTERCASH -> {
            val entercashConfig: EntercashConfiguration = dropInConfig.getConfigurationFor(PaymentMethodTypes.ENTERCASH, fragment.context!!)
            EntercashComponent.PROVIDER.get(fragment, paymentMethod, entercashConfig)
        }
        PaymentMethodTypes.SCHEME -> {
            val cardConfig: CardConfiguration = dropInConfig.getConfigurationFor(PaymentMethodTypes.SCHEME, fragment.context!!)
            CardComponent.PROVIDER.get(fragment, paymentMethod, cardConfig)
        }
        PaymentMethodTypes.GOOGLE_PAY -> {
            val googlePayConfiguration: GooglePayConfiguration = dropInConfig.getConfigurationFor(PaymentMethodTypes.GOOGLE_PAY, fragment.context!!)
            GooglePayComponent.PROVIDER.get(fragment, paymentMethod, googlePayConfiguration)
        }
        else -> {
            throw CheckoutException("Unable to find component for type - ${paymentMethod.type}")
        }
    }
    component.setCreatedForDropIn()
    return component
}

/**
 * Provides a [ComponentView] to be used in Drop-in using the [PaymentMethod] reference.
 * View type is defined by our UI specifications.
 *
 * @param context The context used to create the View
 * @param paymentMethod The payment method to be parsed.
 */
internal fun getViewFor(context: Context, paymentMethod: PaymentMethod): ComponentView<PaymentComponent> {
    @Suppress("UNCHECKED_CAST")
    return when (paymentMethod.type) {
        PaymentMethodTypes.IDEAL -> {
            IdealRecyclerView(context) as ComponentView<PaymentComponent>
        }
        PaymentMethodTypes.MOLPAY -> {
            MolpayRecyclerView(context) as ComponentView<PaymentComponent>
        }
        PaymentMethodTypes.EPS -> {
            EPSRecyclerView(context) as ComponentView<PaymentComponent>
        }
        PaymentMethodTypes.DOTPAY -> {
            DotpayRecyclerView(context) as ComponentView<PaymentComponent>
        }
        PaymentMethodTypes.OPEN_BANKING -> {
            OpenBankingRecyclerView(context) as ComponentView<PaymentComponent>
        }
        PaymentMethodTypes.ENTERCASH -> {
            EntercashRecyclerView(context) as ComponentView<PaymentComponent>
        }
        PaymentMethodTypes.SCHEME -> {
            CardView(context) as ComponentView<PaymentComponent>
        }
        // GooglePay does not require a View in Drop-in
        else -> {
            throw CheckoutException("Unable to find view for type - ${paymentMethod.type}")
        }
    }
}

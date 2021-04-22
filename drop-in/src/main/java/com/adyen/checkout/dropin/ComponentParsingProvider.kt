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
import androidx.fragment.app.Fragment
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.await.AwaitView
import com.adyen.checkout.bcmc.BcmcComponent
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.bcmc.BcmcView
import com.adyen.checkout.blik.BlikComponent
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.blik.BlikView
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.CardView
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.ComponentView
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.util.ActionTypes
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
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
import com.adyen.checkout.mbway.MBWayComponent
import com.adyen.checkout.mbway.MBWayConfiguration
import com.adyen.checkout.mbway.MBWayView
import com.adyen.checkout.molpay.MolpayComponent
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.molpay.MolpayRecyclerView
import com.adyen.checkout.openbanking.OpenBankingComponent
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.openbanking.OpenBankingRecyclerView
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.qrcode.QRCodeView
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.sepa.SepaComponent
import com.adyen.checkout.sepa.SepaConfiguration
import com.adyen.checkout.sepa.SepaView
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration

object ComponentParsingProvider {
    val TAG = LogUtil.getTag()
}

@Suppress("ComplexMethod", "LongMethod")
internal fun <T : Configuration> getDefaultConfigForPaymentMethod(
    paymentMethod: String,
    context: Context,
    dropInConfiguration: DropInConfiguration
): T {

    // TODO after fetching public key is enabled, build scheme if client key is present
    val specificRequirementConfigs = listOf(PaymentMethodTypes.SCHEME, PaymentMethodTypes.GOOGLE_PAY)

    if (specificRequirementConfigs.contains(paymentMethod)) {
        throw CheckoutException("Cannot provide default config for $paymentMethod. Please add it to the DropInConfiguration with required fields.")
    }

    val clientKey = dropInConfiguration.clientKey

    // get default builder for Configuration type
    val builder: BaseConfigurationBuilder<out Configuration> = when (paymentMethod) {
        PaymentMethodTypes.BLIK -> BlikConfiguration.Builder(context, clientKey)
        PaymentMethodTypes.DOTPAY -> DotpayConfiguration.Builder(context, clientKey)
        PaymentMethodTypes.ENTERCASH -> EntercashConfiguration.Builder(context, clientKey)
        PaymentMethodTypes.EPS -> EPSConfiguration.Builder(context, clientKey)
        PaymentMethodTypes.IDEAL -> IdealConfiguration.Builder(context, clientKey)
        PaymentMethodTypes.MB_WAY -> MBWayConfiguration.Builder(context, clientKey)
        PaymentMethodTypes.MOLPAY_THAILAND,
        PaymentMethodTypes.MOLPAY_MALAYSIA,
        PaymentMethodTypes.MOLPAY_VIETNAM -> MolpayConfiguration.Builder(context, clientKey)
        PaymentMethodTypes.OPEN_BANKING -> OpenBankingConfiguration.Builder(context, clientKey)
        PaymentMethodTypes.SEPA -> SepaConfiguration.Builder(context, clientKey)
        else -> throw CheckoutException("Unable to find component configuration for paymentMethod - $paymentMethod")
    }

    builder.setShopperLocale(dropInConfiguration.shopperLocale)
    builder.setEnvironment(dropInConfiguration.environment)

    @Suppress("UNCHECKED_CAST")
    return builder.build() as T
}

@Suppress("ComplexMethod", "LongMethod")
internal inline fun <reified T : Configuration> getDefaultConfigForAction(
    context: Context,
    dropInConfiguration: DropInConfiguration
): T {
    val clientKey = dropInConfiguration.clientKey

    // get default builder for Configuration type
    val builder: BaseConfigurationBuilder<out Configuration> = when (T::class) {
        AwaitConfiguration::class -> AwaitConfiguration.Builder(context, clientKey)
        RedirectConfiguration::class -> RedirectConfiguration.Builder(context, clientKey)
        QRCodeConfiguration::class -> QRCodeConfiguration.Builder(context, clientKey)
        Adyen3DS2Configuration::class -> Adyen3DS2Configuration.Builder(context, clientKey)
        WeChatPayActionConfiguration::class -> WeChatPayActionConfiguration.Builder(context, clientKey)
        else -> throw CheckoutException("Unable to find component configuration for class - ${T::class}")
    }

    builder.setShopperLocale(dropInConfiguration.shopperLocale)
    builder.setEnvironment(dropInConfiguration.environment)

    @Suppress("UNCHECKED_CAST")
    return builder.build() as T
}

internal fun checkComponentAvailability(
    application: Application,
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    callback: ComponentAvailableCallback<in Configuration>
) {
    try {
        Logger.v(ComponentParsingProvider.TAG, "Checking availability for type - ${paymentMethod.type}")

        val type = paymentMethod.type ?: throw CheckoutException("PaymentMethod type is null")

        val provider = getProviderForType(type)
        val configuration = dropInConfiguration.getConfigurationForPaymentMethod<Configuration>(type, application)

        provider.isAvailable(application, paymentMethod, configuration, callback)
    } catch (e: CheckoutException) {
        Logger.e(ComponentParsingProvider.TAG, "Unable to initiate ${paymentMethod.type}", e)
        callback.onAvailabilityResult(false, paymentMethod, null)
    }
}

@Suppress("ComplexMethod")
internal fun getProviderForType(type: String): PaymentComponentProvider<PaymentComponent<*, *>, Configuration> {
    @Suppress("UNCHECKED_CAST")
    return when (type) {
        PaymentMethodTypes.BCMC -> BcmcComponent.PROVIDER
        PaymentMethodTypes.BLIK -> BlikComponent.PROVIDER
        PaymentMethodTypes.DOTPAY -> DotpayComponent.PROVIDER
        PaymentMethodTypes.ENTERCASH -> EntercashComponent.PROVIDER
        PaymentMethodTypes.EPS -> EPSComponent.PROVIDER
        PaymentMethodTypes.GOOGLE_PAY -> GooglePayComponent.PROVIDER
        PaymentMethodTypes.IDEAL -> IdealComponent.PROVIDER
        PaymentMethodTypes.MB_WAY -> MBWayComponent.PROVIDER
        PaymentMethodTypes.MOLPAY_THAILAND,
        PaymentMethodTypes.MOLPAY_MALAYSIA,
        PaymentMethodTypes.MOLPAY_VIETNAM -> MolpayComponent.PROVIDER
        PaymentMethodTypes.OPEN_BANKING -> OpenBankingComponent.PROVIDER
        PaymentMethodTypes.SCHEME -> CardComponent.PROVIDER
        PaymentMethodTypes.SEPA -> SepaComponent.PROVIDER
        else -> {
            throw CheckoutException("Unable to find component for type - $type")
        }
    } as PaymentComponentProvider<PaymentComponent<*, *>, Configuration>
}

/**
 * Provides a [PaymentComponent] from a [PaymentComponentProvider] using the [StoredPaymentMethod] reference.
 *
 * @param fragment The Fragment which the PaymentComponent lifecycle will be bound to.
 * @param storedPaymentMethod The stored payment method to be parsed.
 * @throws CheckoutException In case a component cannot be created.
 */
internal fun getComponentFor(
    fragment: Fragment,
    storedPaymentMethod: StoredPaymentMethod,
    dropInConfiguration: DropInConfiguration
): PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration> {
    val context = fragment.requireContext()

    val component = when (storedPaymentMethod.type) {
        PaymentMethodTypes.SCHEME -> {
            val cardConfig: CardConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.SCHEME, context)
            CardComponent.PROVIDER.get(fragment, storedPaymentMethod, cardConfig)
        }
        PaymentMethodTypes.BLIK -> {
            val blikConfig: BlikConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.BLIK, context)
            BlikComponent.PROVIDER.get(fragment, storedPaymentMethod, blikConfig)
        }
        else -> {
            throw CheckoutException("Unable to find stored component for type - ${storedPaymentMethod.type}")
        }
    }
    component.setCreatedForDropIn()
    return component as PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration>
}

/**
 * Provides a [PaymentComponent] from a [PaymentComponentProvider] using the [PaymentMethod] reference.
 *
 * @param fragment The Fragment which the PaymentComponent lifecycle will be bound to.
 * @param paymentMethod The payment method to be parsed.
 * @throws CheckoutException In case a component cannot be created.
 */
@Suppress("ComplexMethod", "LongMethod")
internal fun getComponentFor(
    fragment: Fragment,
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration
): PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration> {
    val context = fragment.requireContext()

    val component = when (paymentMethod.type) {
        PaymentMethodTypes.BCMC -> {
            val bcmcConfiguration: BcmcConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.BCMC, context)
            BcmcComponent.PROVIDER.get(fragment, paymentMethod, bcmcConfiguration)
        }
        PaymentMethodTypes.BLIK -> {
            val blikConfiguration: BlikConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.BLIK, context)
            BlikComponent.PROVIDER.get(fragment, paymentMethod, blikConfiguration)
        }
        PaymentMethodTypes.DOTPAY -> {
            val dotpayConfig: DotpayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.DOTPAY, context)
            DotpayComponent.PROVIDER.get(fragment, paymentMethod, dotpayConfig)
        }
        PaymentMethodTypes.ENTERCASH -> {
            val entercashConfig: EntercashConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.ENTERCASH, context)
            EntercashComponent.PROVIDER.get(fragment, paymentMethod, entercashConfig)
        }
        PaymentMethodTypes.EPS -> {
            val epsConfig: EPSConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.EPS, context)
            EPSComponent.PROVIDER.get(fragment, paymentMethod, epsConfig)
        }
        PaymentMethodTypes.GOOGLE_PAY -> {
            val googlePayConfiguration: GooglePayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(
                PaymentMethodTypes.GOOGLE_PAY,
                context
            )
            GooglePayComponent.PROVIDER.get(fragment, paymentMethod, googlePayConfiguration)
        }
        PaymentMethodTypes.IDEAL -> {
            val idealConfig: IdealConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.IDEAL, context)
            IdealComponent.PROVIDER.get(fragment, paymentMethod, idealConfig)
        }
        PaymentMethodTypes.MB_WAY -> {
            val mbWayConfiguration: MBWayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.MB_WAY, context)
            MBWayComponent.PROVIDER.get(fragment, paymentMethod, mbWayConfiguration)
        }
        PaymentMethodTypes.MOLPAY_THAILAND -> {
            val molpayConfig: MolpayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.MOLPAY_THAILAND, context)
            MolpayComponent.PROVIDER.get(fragment, paymentMethod, molpayConfig)
        }
        PaymentMethodTypes.MOLPAY_MALAYSIA -> {
            val molpayConfig: MolpayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.MOLPAY_MALAYSIA, context)
            MolpayComponent.PROVIDER.get(fragment, paymentMethod, molpayConfig)
        }
        PaymentMethodTypes.MOLPAY_VIETNAM -> {
            val molpayConfig: MolpayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(
                PaymentMethodTypes.MOLPAY_VIETNAM,
                context
            )
            MolpayComponent.PROVIDER.get(fragment, paymentMethod, molpayConfig)
        }
        PaymentMethodTypes.OPEN_BANKING -> {
            val openBankingConfig: OpenBankingConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(
                PaymentMethodTypes.OPEN_BANKING,
                context
            )
            OpenBankingComponent.PROVIDER.get(fragment, paymentMethod, openBankingConfig)
        }
        PaymentMethodTypes.SCHEME -> {
            val cardConfig: CardConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.SCHEME, context)
            CardComponent.PROVIDER.get(fragment, paymentMethod, cardConfig)
        }
        PaymentMethodTypes.SEPA -> {
            val sepaConfiguration: SepaConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.SEPA, context)
            SepaComponent.PROVIDER.get(fragment, paymentMethod, sepaConfiguration)
        }

        else -> {
            throw CheckoutException("Unable to find component for type - ${paymentMethod.type}")
        }
    }
    component.setCreatedForDropIn()
    return component as PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration>
}

/**
 * Provides a [ComponentView] to be used in Drop-in using the [PaymentMethod] reference.
 * View type is defined by our UI specifications.
 *
 * @param context The context used to create the View
 * @param paymentType The type identifying the method to be parsed.
 */
@Suppress("ComplexMethod")
internal fun getViewFor(
    context: Context,
    paymentType: String
): ComponentView<in OutputData, ViewableComponent<*, *, *>> {
    @Suppress("UNCHECKED_CAST")
    return when (paymentType) {
        PaymentMethodTypes.BCMC -> BcmcView(context)
        PaymentMethodTypes.DOTPAY -> DotpayRecyclerView(context)
        PaymentMethodTypes.ENTERCASH -> EntercashRecyclerView(context)
        PaymentMethodTypes.EPS -> EPSRecyclerView(context)
        PaymentMethodTypes.IDEAL -> IdealRecyclerView(context)
        PaymentMethodTypes.MB_WAY -> MBWayView(context)
        PaymentMethodTypes.MOLPAY_THAILAND,
        PaymentMethodTypes.MOLPAY_MALAYSIA,
        PaymentMethodTypes.MOLPAY_VIETNAM -> MolpayRecyclerView(context)
        PaymentMethodTypes.OPEN_BANKING -> OpenBankingRecyclerView(context)
        PaymentMethodTypes.SCHEME -> CardView(context)
        PaymentMethodTypes.SEPA -> SepaView(context)
        PaymentMethodTypes.BLIK -> BlikView(context)
        // GooglePay and WeChatPay do not require a View in Drop-in
        ActionTypes.AWAIT -> AwaitView(context)
        ActionTypes.QR_CODE -> QRCodeView(context)
        else -> {
            throw CheckoutException("Unable to find view for type - $paymentType")
        }
        // TODO check if this generic approach can be improved
    } as ComponentView<in OutputData, ViewableComponent<*, *, *>>
}

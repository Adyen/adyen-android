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
import androidx.fragment.app.FragmentActivity
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitComponent
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
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.AlwaysAvailablePaymentMethod
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.ComponentView
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.PaymentMethodAvailabilityCheck
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.model.payments.response.Action
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
import com.adyen.checkout.googlepay.GooglePayProvider
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
import com.adyen.checkout.qrcode.QRCodeComponent
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.qrcode.QRCodeView
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.sepa.SepaComponent
import com.adyen.checkout.sepa.SepaConfiguration
import com.adyen.checkout.sepa.SepaView
import com.adyen.checkout.wechatpay.WeChatPayActionComponent
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration
import com.adyen.checkout.wechatpay.WeChatPayProvider

object ComponentParsingProvider {
    val TAG = LogUtil.getTag()
}

@Suppress("ComplexMethod", "LongMethod")
internal fun <T : Configuration> getDefaultConfigForPaymentMethod(
    paymentMethod: String,
    dropInConfiguration: DropInConfiguration
): T {
    val shopperLocale = dropInConfiguration.shopperLocale
    val environment = dropInConfiguration.environment
    val clientKey = dropInConfiguration.clientKey

    // get default builder for Configuration type
    val builder: BaseConfigurationBuilder<out Configuration> = when (paymentMethod) {
        PaymentMethodTypes.BCMC -> BcmcConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.BLIK -> BlikConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.DOTPAY -> DotpayConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.ENTERCASH -> EntercashConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.EPS -> EPSConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.GOOGLE_PAY,
        PaymentMethodTypes.GOOGLE_PAY_LEGACY -> {
            GooglePayConfiguration.Builder(shopperLocale, environment, clientKey).apply {
                if (!dropInConfiguration.amount.isEmpty) setAmount(dropInConfiguration.amount)
            }
        }
        PaymentMethodTypes.IDEAL -> IdealConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.MB_WAY -> MBWayConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.MOLPAY_THAILAND,
        PaymentMethodTypes.MOLPAY_MALAYSIA,
        PaymentMethodTypes.MOLPAY_VIETNAM -> MolpayConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.OPEN_BANKING -> OpenBankingConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.SEPA -> SepaConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.SCHEME -> CardConfiguration.Builder(shopperLocale, environment, clientKey)
        else -> throw CheckoutException("Unable to find component configuration for paymentMethod - $paymentMethod")
    }

    @Suppress("UNCHECKED_CAST")
    return builder.build() as T
}

@Suppress("ComplexMethod", "LongMethod")
internal inline fun <reified T : Configuration> getDefaultConfigForAction(
    dropInConfiguration: DropInConfiguration
): T {
    val shopperLocale = dropInConfiguration.shopperLocale
    val environment = dropInConfiguration.environment
    val clientKey = dropInConfiguration.clientKey

    // get default builder for Configuration type
    val builder: BaseConfigurationBuilder<out Configuration> = when (T::class) {
        AwaitConfiguration::class -> AwaitConfiguration.Builder(shopperLocale, environment, clientKey)
        RedirectConfiguration::class -> RedirectConfiguration.Builder(shopperLocale, environment, clientKey)
        QRCodeConfiguration::class -> QRCodeConfiguration.Builder(shopperLocale, environment, clientKey)
        Adyen3DS2Configuration::class -> Adyen3DS2Configuration.Builder(shopperLocale, environment, clientKey)
        WeChatPayActionConfiguration::class -> WeChatPayActionConfiguration.Builder(shopperLocale, environment, clientKey)
        else -> throw CheckoutException("Unable to find component configuration for class - ${T::class}")
    }

    @Suppress("UNCHECKED_CAST")
    return builder.build() as T
}

internal fun checkPaymentMethodAvailability(
    application: Application,
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    callback: ComponentAvailableCallback<in Configuration>
) {
    try {
        Logger.v(ComponentParsingProvider.TAG, "Checking availability for type - ${paymentMethod.type}")

        val type = paymentMethod.type ?: throw CheckoutException("PaymentMethod type is null")

        val availabilityCheck = getPaymentMethodAvailabilityCheck(type)
        val configuration = dropInConfiguration.getConfigurationForPaymentMethodOrNull<Configuration>(type)

        availabilityCheck.isAvailable(application, paymentMethod, configuration, callback)
    } catch (e: CheckoutException) {
        Logger.e(ComponentParsingProvider.TAG, "Unable to initiate ${paymentMethod.type}", e)
        callback.onAvailabilityResult(false, paymentMethod, null)
    }
}

/**
 * Provides the [PaymentMethodAvailabilityCheck] class for the specified [paymentMethodType], if available.
 */
internal fun getPaymentMethodAvailabilityCheck(paymentMethodType: String): PaymentMethodAvailabilityCheck<Configuration> {
    @Suppress("UNCHECKED_CAST")
    return when (paymentMethodType) {
        PaymentMethodTypes.GOOGLE_PAY,
        PaymentMethodTypes.GOOGLE_PAY_LEGACY -> GooglePayProvider()
        PaymentMethodTypes.WECHAT_PAY_SDK -> WeChatPayProvider()
        else -> AlwaysAvailablePaymentMethod()
    } as PaymentMethodAvailabilityCheck<Configuration>
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

    val component = when (storedPaymentMethod.type) {
        PaymentMethodTypes.SCHEME -> {
            val cardConfig: CardConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.SCHEME)
            CardComponent.PROVIDER.get(fragment, storedPaymentMethod, cardConfig)
        }
        PaymentMethodTypes.BLIK -> {
            val blikConfig: BlikConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.BLIK)
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

    val component = when (paymentMethod.type) {
        PaymentMethodTypes.BCMC -> {
            val bcmcConfiguration: BcmcConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.BCMC)
            BcmcComponent.PROVIDER.get(fragment, paymentMethod, bcmcConfiguration)
        }
        PaymentMethodTypes.BLIK -> {
            val blikConfiguration: BlikConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.BLIK)
            BlikComponent.PROVIDER.get(fragment, paymentMethod, blikConfiguration)
        }
        PaymentMethodTypes.DOTPAY -> {
            val dotpayConfig: DotpayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.DOTPAY)
            DotpayComponent.PROVIDER.get(fragment, paymentMethod, dotpayConfig)
        }
        PaymentMethodTypes.ENTERCASH -> {
            val entercashConfig: EntercashConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.ENTERCASH)
            EntercashComponent.PROVIDER.get(fragment, paymentMethod, entercashConfig)
        }
        PaymentMethodTypes.EPS -> {
            val epsConfig: EPSConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.EPS)
            EPSComponent.PROVIDER.get(fragment, paymentMethod, epsConfig)
        }
        PaymentMethodTypes.GOOGLE_PAY -> {
            val googlePayConfiguration: GooglePayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.GOOGLE_PAY)
            GooglePayComponent.PROVIDER.get(fragment, paymentMethod, googlePayConfiguration)
        }
        PaymentMethodTypes.GOOGLE_PAY_LEGACY -> {
            val googlePayConfiguration: GooglePayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(
                PaymentMethodTypes.GOOGLE_PAY_LEGACY
            )
            GooglePayComponent.PROVIDER.get(fragment, paymentMethod, googlePayConfiguration)
        }
        PaymentMethodTypes.IDEAL -> {
            val idealConfig: IdealConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.IDEAL)
            IdealComponent.PROVIDER.get(fragment, paymentMethod, idealConfig)
        }
        PaymentMethodTypes.MB_WAY -> {
            val mbWayConfiguration: MBWayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.MB_WAY)
            MBWayComponent.PROVIDER.get(fragment, paymentMethod, mbWayConfiguration)
        }
        PaymentMethodTypes.MOLPAY_THAILAND -> {
            val molpayConfig: MolpayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.MOLPAY_THAILAND)
            MolpayComponent.PROVIDER.get(fragment, paymentMethod, molpayConfig)
        }
        PaymentMethodTypes.MOLPAY_MALAYSIA -> {
            val molpayConfig: MolpayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.MOLPAY_MALAYSIA)
            MolpayComponent.PROVIDER.get(fragment, paymentMethod, molpayConfig)
        }
        PaymentMethodTypes.MOLPAY_VIETNAM -> {
            val molpayConfig: MolpayConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.MOLPAY_VIETNAM)
            MolpayComponent.PROVIDER.get(fragment, paymentMethod, molpayConfig)
        }
        PaymentMethodTypes.OPEN_BANKING -> {
            val openBankingConfig: OpenBankingConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.OPEN_BANKING)
            OpenBankingComponent.PROVIDER.get(fragment, paymentMethod, openBankingConfig)
        }
        PaymentMethodTypes.SCHEME -> {
            val cardConfig: CardConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.SCHEME)
            CardComponent.PROVIDER.get(fragment, paymentMethod, cardConfig)
        }
        PaymentMethodTypes.SEPA -> {
            val sepaConfiguration: SepaConfiguration = dropInConfiguration.getConfigurationForPaymentMethod(PaymentMethodTypes.SEPA)
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

/**
 * @param action The action to be handled
 *
 * @return The provider able to handle the action.
 */
internal fun getActionProviderFor(action: Action): ActionComponentProvider<out BaseActionComponent<out Configuration>, out Configuration>? {
    val allActionProviders = listOf(
        RedirectComponent.PROVIDER,
        Adyen3DS2Component.PROVIDER,
        WeChatPayActionComponent.PROVIDER,
        AwaitComponent.PROVIDER,
        QRCodeComponent.PROVIDER
    )
    return allActionProviders.firstOrNull { it.canHandleAction(action) }
}

/**
 * Provides a [BaseActionComponent] from an [ActionComponentProvider].
 *
 * @param activity The Action which the ActionComponent lifecycle will be bound to.
 * @param provider The component provider.
 * @throws CheckoutException In case a component cannot be created.
 */
@Suppress("ComplexMethod", "LongMethod")
internal fun getActionComponentFor(
    activity: FragmentActivity,
    provider: ActionComponentProvider<out BaseActionComponent<out Configuration>, out Configuration>,
    dropInConfiguration: DropInConfiguration
): BaseActionComponent<out Configuration> {
    return when (provider) {
        RedirectComponent.PROVIDER -> {
            RedirectComponent.PROVIDER.get(activity, activity.application, dropInConfiguration.getConfigurationForAction())
        }
        Adyen3DS2Component.PROVIDER -> {
            Adyen3DS2Component.PROVIDER.get(activity, activity.application, dropInConfiguration.getConfigurationForAction())
        }
        WeChatPayActionComponent.PROVIDER -> {
            WeChatPayActionComponent.PROVIDER.get(activity, activity.application, dropInConfiguration.getConfigurationForAction())
        }
        AwaitComponent.PROVIDER -> {
            AwaitComponent.PROVIDER.get(activity, activity.application, dropInConfiguration.getConfigurationForAction())
        }
        QRCodeComponent.PROVIDER -> {
            QRCodeComponent.PROVIDER.get(activity, activity.application, dropInConfiguration.getConfigurationForAction())
        }
        else -> {
            throw CheckoutException("Unable to find component for provider - $provider")
        }
    }
}

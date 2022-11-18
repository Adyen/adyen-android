/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/4/2019.
 */

@file:Suppress("TooManyFunctions")

package com.adyen.checkout.dropin

import android.app.Application
import androidx.fragment.app.Fragment
import com.adyen.checkout.action.GenericActionConfiguration
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.bacs.BacsDirectDebitComponentProvider
import com.adyen.checkout.bacs.BacsDirectDebitConfiguration
import com.adyen.checkout.bcmc.BcmcComponent
import com.adyen.checkout.bcmc.BcmcComponentProvider
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.blik.BlikComponent
import com.adyen.checkout.blik.BlikComponentProvider
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentProvider
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.AlwaysAvailablePaymentMethod
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.PaymentMethodAvailabilityCheck
import com.adyen.checkout.components.base.AmountConfiguration
import com.adyen.checkout.components.base.AmountConfigurationBuilder
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dotpay.DotpayComponent
import com.adyen.checkout.dotpay.DotpayComponentProvider
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.entercash.EntercashComponent
import com.adyen.checkout.entercash.EntercashComponentProvider
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.eps.EPSComponent
import com.adyen.checkout.eps.EPSComponentProvider
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardComponentProvider
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentProvider
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.ideal.IdealComponent
import com.adyen.checkout.ideal.IdealComponentProvider
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.instant.InstantPaymentComponent
import com.adyen.checkout.instant.InstantPaymentComponentProvider
import com.adyen.checkout.instant.InstantPaymentConfiguration
import com.adyen.checkout.mbway.MBWayComponent
import com.adyen.checkout.mbway.MBWayComponentProvider
import com.adyen.checkout.mbway.MBWayConfiguration
import com.adyen.checkout.molpay.MolpayComponent
import com.adyen.checkout.molpay.MolpayComponentProvider
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponent
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponentProvider
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZConfiguration
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponent
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponentProvider
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLConfiguration
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKComponent
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKComponentProvider
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKConfiguration
import com.adyen.checkout.openbanking.OpenBankingComponent
import com.adyen.checkout.openbanking.OpenBankingComponentProvider
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.paybybank.PayByBankComponent
import com.adyen.checkout.paybybank.PayByBankComponentProvider
import com.adyen.checkout.paybybank.PayByBankConfiguration
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.sepa.SepaComponent
import com.adyen.checkout.sepa.SepaComponentProvider
import com.adyen.checkout.sepa.SepaConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration
import com.adyen.checkout.wechatpay.WeChatPayProvider

private val TAG = LogUtil.getTag()

internal inline fun <reified T : Configuration> getConfigurationForPaymentMethod(
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    amount: Amount
): T {
    val paymentMethodType = paymentMethod.type ?: throw CheckoutException("Payment method type is null")
    val configuration: T =
        dropInConfiguration.getConfigurationForPaymentMethod(paymentMethodType) ?: getDefaultConfigForPaymentMethod(
            paymentMethod,
            dropInConfiguration
        )
    if (amount.isEmpty || configuration !is AmountConfiguration) return configuration
    return overrideConfigurationAmount(configuration, amount)
}

internal inline fun <reified T : Configuration> getConfigurationForPaymentMethod(
    storedPaymentMethod: StoredPaymentMethod,
    dropInConfiguration: DropInConfiguration,
    amount: Amount
): T {
    val storedPaymentMethodType = storedPaymentMethod.type ?: throw CheckoutException("Payment method type is null")
    val configuration: T = dropInConfiguration.getConfigurationForPaymentMethod(storedPaymentMethodType)
        ?: getDefaultConfigForPaymentMethod(
            storedPaymentMethod = storedPaymentMethod,
            dropInConfiguration = dropInConfiguration
        )
    if (amount.isEmpty || configuration !is AmountConfiguration) return configuration
    return overrideConfigurationAmount(configuration, amount)
}

private inline fun <reified T> overrideConfigurationAmount(
    configuration: Configuration,
    amount: Amount
): T where T : Configuration, T : AmountConfiguration {
    Logger.d(TAG, "Overriding ${configuration::class.java.simpleName} with $amount")
    return configuration.toBuilder().apply {
        (this as AmountConfigurationBuilder).setAmount(amount)
    }.build() as T
}

internal fun <T : Configuration> getDefaultConfigForPaymentMethod(
    storedPaymentMethod: StoredPaymentMethod,
    dropInConfiguration: DropInConfiguration
): T {
    val shopperLocale = dropInConfiguration.shopperLocale
    val environment = dropInConfiguration.environment
    val clientKey = dropInConfiguration.clientKey
    val builder: BaseConfigurationBuilder<out Configuration> = when {
        BlikComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) -> BlikConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        CardComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) -> CardConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        else -> throw CheckoutException(
            errorMessage = "Unable to find component configuration for storedPaymentMethod - $storedPaymentMethod"
        )
    }
    @Suppress("UNCHECKED_CAST")
    return builder.build() as T
}

@Suppress("ComplexMethod", "LongMethod")
internal fun <T : Configuration> getDefaultConfigForPaymentMethod(
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration
): T {
    val shopperLocale = dropInConfiguration.shopperLocale
    val environment = dropInConfiguration.environment
    val clientKey = dropInConfiguration.clientKey

    // get default builder for Configuration type
    val builder: BaseConfigurationBuilder<out Configuration> = when {
        BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) ->
            BacsDirectDebitConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )
        BcmcComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> BcmcConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        BlikComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> BlikConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        CardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> CardConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        DotpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> DotpayConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        EntercashComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> EntercashConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        EPSComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> EPSConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        GiftCardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> GiftCardConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        GooglePayComponentProvider(dropInConfiguration).isPaymentMethodSupported(paymentMethod) ->
            GooglePayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )
        IdealComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> IdealConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        InstantPaymentComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> InstantPaymentConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        MBWayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> MBWayConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        MolpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> MolpayConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        OnlineBankingCZComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) ->
            OnlineBankingCZConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )
        OnlineBankingPLComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) ->
            OnlineBankingPLConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )
        OnlineBankingSKComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) ->
            OnlineBankingSKConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )
        OpenBankingComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> OpenBankingConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        PayByBankComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> PayByBankConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        SepaComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> SepaConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey
        )
        else -> throw CheckoutException("Unable to find component configuration for paymentMethod - $paymentMethod")
    }

    @Suppress("UNCHECKED_CAST")
    return builder.build() as T
}

internal fun createGenericActionConfiguration(dropInConfiguration: DropInConfiguration): GenericActionConfiguration {
    return GenericActionConfiguration.Builder(
        dropInConfiguration.shopperLocale,
        dropInConfiguration.environment,
        dropInConfiguration.clientKey
    ).apply {
        dropInConfiguration.availableActionConfigs.entries.forEach { entry ->
            availableActionConfigs[entry.key] = entry.value
        }
    }.build()
}

private inline fun <reified T : Configuration> getConfigurationForPaymentMethodOrNull(
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    amount: Amount
): T? {
    return try {
        getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
    } catch (e: CheckoutException) {
        null
    }
}

internal fun checkPaymentMethodAvailability(
    application: Application,
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    amount: Amount,
    callback: ComponentAvailableCallback<in Configuration>
) {
    try {
        Logger.v(TAG, "Checking availability for type - ${paymentMethod.type}")

        val type = paymentMethod.type ?: throw CheckoutException("PaymentMethod type is null")

        val availabilityCheck = getPaymentMethodAvailabilityCheck(type)
        val configuration =
            getConfigurationForPaymentMethodOrNull<Configuration>(paymentMethod, dropInConfiguration, amount)

        availabilityCheck.isAvailable(application, paymentMethod, configuration, callback)
    } catch (e: CheckoutException) {
        Logger.e(TAG, "Unable to initiate ${paymentMethod.type}", e)
        callback.onAvailabilityResult(false, paymentMethod, null)
    }
}

/**
 * Provides the [PaymentMethodAvailabilityCheck] class for the specified [paymentMethodType], if available.
 */
internal fun getPaymentMethodAvailabilityCheck(
    paymentMethodType: String
): PaymentMethodAvailabilityCheck<Configuration> {
    @Suppress("UNCHECKED_CAST")
    return when (paymentMethodType) {
        PaymentMethodTypes.GOOGLE_PAY,
        PaymentMethodTypes.GOOGLE_PAY_LEGACY -> GooglePayComponentProvider()
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
    dropInConfiguration: DropInConfiguration,
    amount: Amount
): PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration> {
    val component = when {
        CardComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) -> {
            val cardConfig: CardConfiguration =
                getConfigurationForPaymentMethod(storedPaymentMethod, dropInConfiguration, amount)
            CardComponentProvider(dropInConfiguration).get(fragment, storedPaymentMethod, cardConfig)
        }
        BlikComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) -> {
            val blikConfig: BlikConfiguration =
                getConfigurationForPaymentMethod(storedPaymentMethod, dropInConfiguration, amount)
            BlikComponentProvider(dropInConfiguration).get(fragment, storedPaymentMethod, blikConfig)
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
    dropInConfiguration: DropInConfiguration,
    amount: Amount
): PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration> {
    val component = when {
        BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val bacsConfiguration: BacsDirectDebitConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            BacsDirectDebitComponentProvider(dropInConfiguration).get(fragment, paymentMethod, bacsConfiguration)
        }
        BcmcComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val bcmcConfiguration: BcmcConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            BcmcComponentProvider(dropInConfiguration).get(fragment, paymentMethod, bcmcConfiguration)
        }
        BlikComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val blikConfiguration: BlikConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            BlikComponentProvider(dropInConfiguration).get(fragment, paymentMethod, blikConfiguration)
        }
        CardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val cardConfig: CardConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            CardComponentProvider(dropInConfiguration).get(fragment, paymentMethod, cardConfig)
        }
        DotpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val dotpayConfig: DotpayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            DotpayComponentProvider(dropInConfiguration).get(fragment, paymentMethod, dotpayConfig)
        }
        EntercashComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val entercashConfig: EntercashConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            EntercashComponentProvider(dropInConfiguration).get(fragment, paymentMethod, entercashConfig)
        }
        EPSComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val epsConfig: EPSConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            EPSComponentProvider(dropInConfiguration).get(fragment, paymentMethod, epsConfig)
        }
        GiftCardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val giftcardConfiguration: GiftCardConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            GiftCardComponentProvider(dropInConfiguration).get(fragment, paymentMethod, giftcardConfiguration)
        }
        GooglePayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val googlePayConfiguration: GooglePayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            GooglePayComponent.PROVIDER.get(fragment, paymentMethod, googlePayConfiguration)
        }
        IdealComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val idealConfig: IdealConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            IdealComponentProvider(dropInConfiguration).get(fragment, paymentMethod, idealConfig)
        }
        InstantPaymentComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val instantPaymentConfiguration: InstantPaymentConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            InstantPaymentComponentProvider(dropInConfiguration).get(
                fragment,
                paymentMethod,
                instantPaymentConfiguration
            )
        }
        MBWayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val mbWayConfiguration: MBWayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            MBWayComponentProvider(dropInConfiguration).get(fragment, paymentMethod, mbWayConfiguration)
        }
        MolpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val molpayConfig: MolpayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            MolpayComponentProvider(dropInConfiguration).get(fragment, paymentMethod, molpayConfig)
        }
        OnlineBankingCZComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val onlineBankingCZConfig: OnlineBankingCZConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            OnlineBankingCZComponentProvider(dropInConfiguration).get(fragment, paymentMethod, onlineBankingCZConfig)
        }
        OnlineBankingPLComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val onlineBankingPLConfig: OnlineBankingPLConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            OnlineBankingPLComponentProvider(dropInConfiguration).get(fragment, paymentMethod, onlineBankingPLConfig)
        }
        OnlineBankingSKComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val onlineBankingSKConfig: OnlineBankingSKConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            OnlineBankingSKComponentProvider(dropInConfiguration).get(fragment, paymentMethod, onlineBankingSKConfig)
        }
        OpenBankingComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val openBankingConfig: OpenBankingConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            OpenBankingComponentProvider(dropInConfiguration).get(fragment, paymentMethod, openBankingConfig)
        }
        PayByBankComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val payByBankConfig: PayByBankConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            PayByBankComponentProvider(dropInConfiguration).get(fragment, paymentMethod, payByBankConfig)
        }
        SepaComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val sepaConfiguration: SepaConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, amount)
            SepaComponentProvider(dropInConfiguration).get(fragment, paymentMethod, sepaConfiguration)
        }
        else -> {
            throw CheckoutException("Unable to find component for type - ${paymentMethod.type}")
        }
    }

    component.setCreatedForDropIn()

    return component as PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration>
}

@Suppress("ComplexMethod")
private fun Configuration.toBuilder(): BaseConfigurationBuilder<out Configuration> {
    return when (this) {
        is Adyen3DS2Configuration -> Adyen3DS2Configuration.Builder(this)
        is AwaitConfiguration -> AwaitConfiguration.Builder(this)
        is BacsDirectDebitConfiguration -> BacsDirectDebitConfiguration.Builder(this)
        is BcmcConfiguration -> BcmcConfiguration.Builder(this)
        is BlikConfiguration -> BlikConfiguration.Builder(this)
        is CardConfiguration -> CardConfiguration.Builder(this)
        is DotpayConfiguration -> DotpayConfiguration.Builder(this)
        is DropInConfiguration -> DropInConfiguration.Builder(this)
        is EntercashConfiguration -> EntercashConfiguration.Builder(this)
        is EPSConfiguration -> EPSConfiguration.Builder(this)
        is GiftCardConfiguration -> GiftCardConfiguration.Builder(this)
        is GooglePayConfiguration -> GooglePayConfiguration.Builder(this)
        is IdealConfiguration -> IdealConfiguration.Builder(this)
        is InstantPaymentConfiguration -> InstantPaymentConfiguration.Builder(this)
        is MBWayConfiguration -> MBWayConfiguration.Builder(this)
        is MolpayConfiguration -> MolpayConfiguration.Builder(this)
        is OnlineBankingCZConfiguration -> OnlineBankingCZConfiguration.Builder(this)
        is OnlineBankingPLConfiguration -> OnlineBankingPLConfiguration.Builder(this)
        is OnlineBankingSKConfiguration -> OnlineBankingSKConfiguration.Builder(this)
        is OpenBankingConfiguration -> OpenBankingConfiguration.Builder(this)
        is PayByBankConfiguration -> PayByBankConfiguration.Builder(this)
        is QRCodeConfiguration -> QRCodeConfiguration.Builder(this)
        is RedirectConfiguration -> RedirectConfiguration.Builder(this)
        is SepaConfiguration -> SepaConfiguration.Builder(this)
        is VoucherConfiguration -> VoucherConfiguration.Builder(this)
        is WeChatPayActionConfiguration -> WeChatPayActionConfiguration.Builder(this)
        else -> throw CheckoutException("Unable to find builder for class - ${this::class}")
    }
}

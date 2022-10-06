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
import com.adyen.checkout.bacs.BacsDirectDebitConfiguration
import com.adyen.checkout.bcmc.BcmcComponent
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.blik.BlikComponent
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.card.CardComponent
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
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.entercash.EntercashComponent
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.eps.EPSComponent
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentProvider
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.ideal.IdealComponent
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.mbway.MBWayComponent
import com.adyen.checkout.mbway.MBWayConfiguration
import com.adyen.checkout.molpay.MolpayComponent
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponent
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZConfiguration
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponent
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLConfiguration
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKComponent
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKConfiguration
import com.adyen.checkout.openbanking.OpenBankingComponent
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.sepa.SepaComponent
import com.adyen.checkout.sepa.SepaConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration
import com.adyen.checkout.wechatpay.WeChatPayProvider

private val TAG = LogUtil.getTag()

internal inline fun <reified T : Configuration> getConfigurationForPaymentMethod(
    paymentMethod: String,
    dropInConfiguration: DropInConfiguration,
    amount: Amount
): T {
    val configuration: T =
        dropInConfiguration.getConfigurationForPaymentMethod(paymentMethod) ?: getDefaultConfigForPaymentMethod(
            paymentMethod,
            dropInConfiguration
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
        PaymentMethodTypes.BACS -> BacsDirectDebitConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.BCMC -> BcmcConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.BLIK -> BlikConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.DOTPAY -> DotpayConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.ENTERCASH -> EntercashConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.EPS -> EPSConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.GIFTCARD -> GiftCardConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.GOOGLE_PAY,
        PaymentMethodTypes.GOOGLE_PAY_LEGACY -> GooglePayConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.IDEAL -> IdealConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.MB_WAY -> MBWayConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.MOLPAY_THAILAND,
        PaymentMethodTypes.MOLPAY_MALAYSIA,
        PaymentMethodTypes.MOLPAY_VIETNAM -> MolpayConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.ONLINE_BANKING_CZ -> OnlineBankingCZConfiguration.Builder(
            shopperLocale,
            environment,
            clientKey
        )
        PaymentMethodTypes.ONLINE_BANKING_PL -> OnlineBankingPLConfiguration.Builder(
            shopperLocale,
            environment,
            clientKey
        )
        PaymentMethodTypes.ONLINE_BANKING_SK -> OnlineBankingSKConfiguration.Builder(
            shopperLocale,
            environment,
            clientKey
        )
        PaymentMethodTypes.OPEN_BANKING -> OpenBankingConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.SEPA -> SepaConfiguration.Builder(shopperLocale, environment, clientKey)
        PaymentMethodTypes.SCHEME -> CardConfiguration.Builder(shopperLocale, environment, clientKey)
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
    paymentMethod: String,
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
        val configuration = getConfigurationForPaymentMethodOrNull<Configuration>(type, dropInConfiguration, amount)

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
    val component = when (storedPaymentMethod.type) {
        PaymentMethodTypes.SCHEME -> {
            val cardConfig: CardConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.SCHEME, dropInConfiguration, amount)
            CardComponent.PROVIDER.get(fragment, storedPaymentMethod, cardConfig)
        }
        PaymentMethodTypes.BLIK -> {
            val blikConfig: BlikConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.BLIK, dropInConfiguration, amount)
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
    dropInConfiguration: DropInConfiguration,
    amount: Amount
): PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration> {
    val component = when (paymentMethod.type) {
        PaymentMethodTypes.BACS -> {
            val bacsConfiguration: BacsDirectDebitConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.BACS, dropInConfiguration, amount)
            BacsDirectDebitComponent.PROVIDER.get(fragment, paymentMethod, bacsConfiguration)
        }
        PaymentMethodTypes.BCMC -> {
            val bcmcConfiguration: BcmcConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.BCMC, dropInConfiguration, amount)
            BcmcComponent.PROVIDER.get(fragment, paymentMethod, bcmcConfiguration)
        }
        PaymentMethodTypes.BLIK -> {
            val blikConfiguration: BlikConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.BLIK, dropInConfiguration, amount)
            BlikComponent.PROVIDER.get(fragment, paymentMethod, blikConfiguration)
        }
        PaymentMethodTypes.DOTPAY -> {
            val dotpayConfig: DotpayConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.DOTPAY, dropInConfiguration, amount)
            DotpayComponent.PROVIDER.get(fragment, paymentMethod, dotpayConfig)
        }
        PaymentMethodTypes.ENTERCASH -> {
            val entercashConfig: EntercashConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.ENTERCASH, dropInConfiguration, amount)
            EntercashComponent.PROVIDER.get(fragment, paymentMethod, entercashConfig)
        }
        PaymentMethodTypes.EPS -> {
            val epsConfig: EPSConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.EPS, dropInConfiguration, amount)
            EPSComponent.PROVIDER.get(fragment, paymentMethod, epsConfig)
        }
        PaymentMethodTypes.GIFTCARD -> {
            val giftcardConfiguration: GiftCardConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.GIFTCARD, dropInConfiguration, amount)
            GiftCardComponent.PROVIDER.get(fragment, paymentMethod, giftcardConfiguration)
        }
        PaymentMethodTypes.GOOGLE_PAY -> {
            val googlePayConfiguration: GooglePayConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.GOOGLE_PAY, dropInConfiguration, amount)
            GooglePayComponent.PROVIDER.get(fragment, paymentMethod, googlePayConfiguration)
        }
        PaymentMethodTypes.GOOGLE_PAY_LEGACY -> {
            val googlePayConfiguration: GooglePayConfiguration = getConfigurationForPaymentMethod(
                PaymentMethodTypes.GOOGLE_PAY_LEGACY,
                dropInConfiguration,
                amount
            )
            GooglePayComponent.PROVIDER.get(fragment, paymentMethod, googlePayConfiguration)
        }
        PaymentMethodTypes.IDEAL -> {
            val idealConfig: IdealConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.IDEAL, dropInConfiguration, amount)
            IdealComponent.PROVIDER.get(fragment, paymentMethod, idealConfig)
        }
        PaymentMethodTypes.MB_WAY -> {
            val mbWayConfiguration: MBWayConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.MB_WAY, dropInConfiguration, amount)
            MBWayComponent.PROVIDER.get(fragment, paymentMethod, mbWayConfiguration)
        }
        PaymentMethodTypes.MOLPAY_THAILAND -> {
            val molpayConfig: MolpayConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.MOLPAY_THAILAND, dropInConfiguration, amount)
            MolpayComponent.PROVIDER.get(fragment, paymentMethod, molpayConfig)
        }
        PaymentMethodTypes.MOLPAY_MALAYSIA -> {
            val molpayConfig: MolpayConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.MOLPAY_MALAYSIA, dropInConfiguration, amount)
            MolpayComponent.PROVIDER.get(fragment, paymentMethod, molpayConfig)
        }
        PaymentMethodTypes.MOLPAY_VIETNAM -> {
            val molpayConfig: MolpayConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.MOLPAY_VIETNAM, dropInConfiguration, amount)
            MolpayComponent.PROVIDER.get(fragment, paymentMethod, molpayConfig)
        }
        PaymentMethodTypes.ONLINE_BANKING_CZ -> {
            val onlineBankingCZConfig: OnlineBankingCZConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.ONLINE_BANKING_CZ, dropInConfiguration, amount)
            OnlineBankingCZComponent.PROVIDER.get(fragment, paymentMethod, onlineBankingCZConfig)
        }
        PaymentMethodTypes.ONLINE_BANKING_PL -> {
            val onlineBankingPLConfig: OnlineBankingPLConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.ONLINE_BANKING_PL, dropInConfiguration, amount)
            OnlineBankingPLComponent.PROVIDER.get(fragment, paymentMethod, onlineBankingPLConfig)
        }
        PaymentMethodTypes.ONLINE_BANKING_SK -> {
            val onlineBankingSKConfig: OnlineBankingSKConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.ONLINE_BANKING_SK, dropInConfiguration, amount)
            OnlineBankingSKComponent.PROVIDER.get(fragment, paymentMethod, onlineBankingSKConfig)
        }
        PaymentMethodTypes.OPEN_BANKING -> {
            val openBankingConfig: OpenBankingConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.OPEN_BANKING, dropInConfiguration, amount)
            OpenBankingComponent.PROVIDER.get(fragment, paymentMethod, openBankingConfig)
        }
        PaymentMethodTypes.SCHEME -> {
            val cardConfig: CardConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.SCHEME, dropInConfiguration, amount)
            CardComponent.PROVIDER.get(fragment, paymentMethod, cardConfig)
        }
        PaymentMethodTypes.SEPA -> {
            val sepaConfiguration: SepaConfiguration =
                getConfigurationForPaymentMethod(PaymentMethodTypes.SEPA, dropInConfiguration, amount)
            SepaComponent.PROVIDER.get(fragment, paymentMethod, sepaConfiguration)
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
        is MBWayConfiguration -> MBWayConfiguration.Builder(this)
        is MolpayConfiguration -> MolpayConfiguration.Builder(this)
        is OnlineBankingCZConfiguration -> OnlineBankingCZConfiguration.Builder(this)
        is OnlineBankingPLConfiguration -> OnlineBankingPLConfiguration.Builder(this)
        is OnlineBankingSKConfiguration -> OnlineBankingSKConfiguration.Builder(this)
        is OpenBankingConfiguration -> OpenBankingConfiguration.Builder(this)
        is QRCodeConfiguration -> QRCodeConfiguration.Builder(this)
        is RedirectConfiguration -> RedirectConfiguration.Builder(this)
        is SepaConfiguration -> SepaConfiguration.Builder(this)
        is VoucherConfiguration -> VoucherConfiguration.Builder(this)
        is WeChatPayActionConfiguration -> WeChatPayActionConfiguration.Builder(this)
        else -> throw CheckoutException("Unable to find builder for class - ${this::class}")
    }
}

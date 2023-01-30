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
import com.adyen.checkout.components.PaymentComponentOld
import com.adyen.checkout.components.PaymentMethodAvailabilityCheck
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.Amount
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
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentProvider
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.ideal.IdealComponent
import com.adyen.checkout.ideal.IdealComponentProvider
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.instant.InstantPaymentComponent
import com.adyen.checkout.instant.InstantPaymentConfiguration
import com.adyen.checkout.mbway.MBWayComponent
import com.adyen.checkout.mbway.MBWayConfiguration
import com.adyen.checkout.molpay.MolpayComponent
import com.adyen.checkout.molpay.MolpayComponentProvider
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponent
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZConfiguration
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponent
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponentProvider
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLConfiguration
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKComponent
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKConfiguration
import com.adyen.checkout.openbanking.OpenBankingComponent
import com.adyen.checkout.openbanking.OpenBankingComponentProvider
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.paybybank.PayByBankComponent
import com.adyen.checkout.paybybank.PayByBankConfiguration
import com.adyen.checkout.sepa.SepaComponent
import com.adyen.checkout.sepa.SepaConfiguration
import com.adyen.checkout.wechatpay.WeChatPayProvider

private val TAG = LogUtil.getTag()

internal inline fun <reified T : Configuration> getConfigurationForPaymentMethod(
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
): T {
    val paymentMethodType = paymentMethod.type ?: throw CheckoutException("Payment method type is null")
    return dropInConfiguration.getConfigurationForPaymentMethod(paymentMethodType) ?: getDefaultConfigForPaymentMethod(
        paymentMethod,
        dropInConfiguration
    )
}

internal inline fun <reified T : Configuration> getConfigurationForPaymentMethod(
    storedPaymentMethod: StoredPaymentMethod,
    dropInConfiguration: DropInConfiguration,
): T {
    val storedPaymentMethodType = storedPaymentMethod.type ?: throw CheckoutException("Payment method type is null")
    return dropInConfiguration.getConfigurationForPaymentMethod(storedPaymentMethodType)
        ?: getDefaultConfigForPaymentMethod(
            storedPaymentMethod = storedPaymentMethod,
            dropInConfiguration = dropInConfiguration
        )
}

internal fun <T : Configuration> getDefaultConfigForPaymentMethod(
    storedPaymentMethod: StoredPaymentMethod,
    dropInConfiguration: DropInConfiguration
): T {
    val shopperLocale = dropInConfiguration.shopperLocale
    val environment = dropInConfiguration.environment
    val clientKey = dropInConfiguration.clientKey
    val builder: BaseConfigurationBuilder<*, *> = when {
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

@Suppress("LongMethod")
internal fun <T : Configuration> getDefaultConfigForPaymentMethod(
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration
): T {
    val shopperLocale = dropInConfiguration.shopperLocale
    val environment = dropInConfiguration.environment
    val clientKey = dropInConfiguration.clientKey

    // get default builder for Configuration type
    val builder: BaseConfigurationBuilder<*, *> = when {
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
        GooglePayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) ->
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

private inline fun <reified T : Configuration> getConfigurationForPaymentMethodOrNull(
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
): T? {
    return try {
        getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
    } catch (e: CheckoutException) {
        null
    }
}

internal fun checkPaymentMethodAvailability(
    application: Application,
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    amount: Amount,
    callback: ComponentAvailableCallback
) {
    try {
        Logger.v(TAG, "Checking availability for type - ${paymentMethod.type}")

        val type = paymentMethod.type ?: throw CheckoutException("PaymentMethod type is null")

        val availabilityCheck = getPaymentMethodAvailabilityCheck(dropInConfiguration, type, amount)
        val configuration =
            getConfigurationForPaymentMethodOrNull<Configuration>(paymentMethod, dropInConfiguration)

        availabilityCheck.isAvailable(application, paymentMethod, configuration, callback)
    } catch (e: CheckoutException) {
        Logger.e(TAG, "Unable to initiate ${paymentMethod.type}", e)
        callback.onAvailabilityResult(false, paymentMethod)
    }
}

/**
 * Provides the [PaymentMethodAvailabilityCheck] class for the specified [paymentMethodType], if available.
 */
internal fun getPaymentMethodAvailabilityCheck(
    dropInConfiguration: DropInConfiguration,
    paymentMethodType: String,
    amount: Amount,
): PaymentMethodAvailabilityCheck<Configuration> {
    val dropInParams = dropInConfiguration.mapToParams(amount)
    @Suppress("UNCHECKED_CAST")
    return when (paymentMethodType) {
        PaymentMethodTypes.GOOGLE_PAY,
        PaymentMethodTypes.GOOGLE_PAY_LEGACY -> GooglePayComponentProvider(dropInParams)
        PaymentMethodTypes.WECHAT_PAY_SDK -> WeChatPayProvider()
        else -> AlwaysAvailablePaymentMethod()
    } as PaymentMethodAvailabilityCheck<Configuration>
}

/**
 * Provides a [PaymentComponentOld] from a [PaymentComponentProvider] using the [StoredPaymentMethod] reference.
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
): PaymentComponentOld<*> {
    val dropInParams = dropInConfiguration.mapToParams(amount)
    @Suppress("UNREACHABLE_CODE")
    return when {
        /*CardComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) -> {
            val cardConfig: CardConfiguration =
                getConfigurationForPaymentMethod(storedPaymentMethod, dropInConfiguration)
            CardComponentProvider(dropInParams).get(
                owner = fragment,
                storedPaymentMethod = storedPaymentMethod,
                configuration = cardConfig,
                application = fragment.requireApplication(),
            )
        }*/
//        BlikComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) -> {
//            val blikConfig: BlikConfiguration =
//                getConfigurationForPaymentMethod(storedPaymentMethod, dropInConfiguration)
//            BlikComponentProvider(dropInParams).get(
//                owner = fragment,
//                storedPaymentMethod = storedPaymentMethod,
//                configuration = blikConfig,
//                application = fragment.requireApplication(),
//            )
//        }
        else -> {
            throw CheckoutException("Unable to find stored component for type - ${storedPaymentMethod.type}")
        }
    }
}

/**
 * Provides a [PaymentComponentOld] from a [PaymentComponentProvider] using the [PaymentMethod] reference.
 *
 * @param fragment The Fragment which the PaymentComponent lifecycle will be bound to.
 * @param paymentMethod The payment method to be parsed.
 * @throws CheckoutException In case a component cannot be created.
 */
@Suppress("LongMethod")
internal fun getComponentFor(
    fragment: Fragment,
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    amount: Amount
): PaymentComponentOld<*> {
    val dropInParams = dropInConfiguration.mapToParams(amount)
    return when {
//        BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
//            val bacsConfiguration: BacsDirectDebitConfiguration =
//                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
//            BacsDirectDebitComponentProvider(dropInParams).get(
//                owner = fragment,
//                paymentMethod = paymentMethod,
//                configuration = bacsConfiguration,
//                application = fragment.requireApplication(),
//            )
//        }
//        BcmcComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
//            val bcmcConfiguration: BcmcConfiguration =
//                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
//            BcmcComponentProvider(dropInParams).get(
//                owner = fragment,
//                paymentMethod = paymentMethod,
//                configuration = bcmcConfiguration,
//                application = fragment.requireApplication(),
//            )
//        }
//        BlikComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
//            val blikConfiguration: BlikConfiguration =
//                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
//            BlikComponentProvider(dropInParams).get(
//                owner = fragment,
//                paymentMethod = paymentMethod,
//                configuration = blikConfiguration,
//                application = fragment.requireApplication(),
//            )
//        }
        /*CardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val cardConfig: CardConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
            CardComponentProvider(dropInParams).get(
                owner = fragment,
                paymentMethod = paymentMethod,
                configuration = cardConfig,
                application = fragment.requireApplication(),
            )
        }*/
        DotpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val dotpayConfig: DotpayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
            DotpayComponentProvider(dropInParams).get(
                owner = fragment,
                paymentMethod = paymentMethod,
                configuration = dotpayConfig,
                application = fragment.requireApplication(),
            )
        }
        EntercashComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val entercashConfig: EntercashConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
            EntercashComponentProvider(dropInParams).get(
                owner = fragment,
                paymentMethod = paymentMethod,
                configuration = entercashConfig,
                application = fragment.requireApplication(),
            )
        }
        EPSComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val epsConfig: EPSConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
            EPSComponentProvider(dropInParams).get(
                owner = fragment,
                paymentMethod = paymentMethod,
                configuration = epsConfig,
                application = fragment.requireApplication(),
            )
        }
//        GiftCardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
//            val giftcardConfiguration: GiftCardConfiguration =
//                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
//            GiftCardComponentProvider(dropInParams).get(
//                owner = fragment,
//                paymentMethod = paymentMethod,
//                configuration = giftcardConfiguration,
//                application = fragment.requireApplication(),
//            )
//        }
//        GooglePayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
//            val googlePayConfiguration: GooglePayConfiguration =
//                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
//            GooglePayComponentProvider(dropInParams).get(
//                owner = fragment,
//                paymentMethod = paymentMethod,
//                configuration = googlePayConfiguration,
//                application = fragment.requireApplication(),
//            )
//        }
        IdealComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val idealConfig: IdealConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
            IdealComponentProvider(dropInParams).get(
                owner = fragment,
                paymentMethod = paymentMethod,
                configuration = idealConfig,
                application = fragment.requireApplication(),
            )
        }
        /*InstantPaymentComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val instantPaymentConfiguration: InstantPaymentConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
            InstantPaymentComponentProvider(dropInParams).get(
                owner = fragment,
                paymentMethod = paymentMethod,
                configuration = instantPaymentConfiguration,
                application = fragment.requireApplication(),
            )
        }*/
//        MBWayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
//            val mbWayConfiguration: MBWayConfiguration =
//                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
//            MBWayComponentProvider(dropInParams).get(
//                owner = fragment,
//                paymentMethod = paymentMethod,
//                configuration = mbWayConfiguration,
//                application = fragment.requireApplication(),
//            )
//        }
        MolpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val molpayConfig: MolpayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
            MolpayComponentProvider(dropInParams).get(
                owner = fragment,
                paymentMethod = paymentMethod,
                configuration = molpayConfig,
                application = fragment.requireApplication(),
            )
        }
//        OnlineBankingCZComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
//            val onlineBankingCZConfig: OnlineBankingCZConfiguration =
//                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
//            OnlineBankingCZComponentProvider(dropInParams).get(
//                owner = fragment,
//                paymentMethod = paymentMethod,
//                configuration = onlineBankingCZConfig,
//                application = fragment.requireApplication(),
//            )
//        }
        OnlineBankingPLComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val onlineBankingPLConfig: OnlineBankingPLConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
            OnlineBankingPLComponentProvider(dropInParams).get(
                owner = fragment,
                paymentMethod = paymentMethod,
                configuration = onlineBankingPLConfig,
                application = fragment.requireApplication(),
            )
        }
//        OnlineBankingSKComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
//            val onlineBankingSKConfig: OnlineBankingSKConfiguration =
//                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
//            OnlineBankingSKComponentProvider(dropInParams).get(
//                owner = fragment,
//                paymentMethod = paymentMethod,
//                configuration = onlineBankingSKConfig,
//                application = fragment.requireApplication(),
//            )
//        }
        OpenBankingComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
            val openBankingConfig: OpenBankingConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
            OpenBankingComponentProvider(dropInParams).get(
                owner = fragment,
                paymentMethod = paymentMethod,
                configuration = openBankingConfig,
                application = fragment.requireApplication(),
            )
        }
//        PayByBankComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
//            val payByBankConfig: PayByBankConfiguration =
//                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
//            PayByBankComponentProvider(dropInParams).get(
//                owner = fragment,
//                paymentMethod = paymentMethod,
//                configuration = payByBankConfig,
//                application = fragment.requireApplication(),
//            )
//        }
//        SepaComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) -> {
//            val sepaConfiguration: SepaConfiguration =
//                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration)
//            SepaComponentProvider(dropInParams).get(
//                owner = fragment,
//                paymentMethod = paymentMethod,
//                configuration = sepaConfiguration,
//                application = fragment.requireApplication(),
//            )
//        }
        else -> {
            throw CheckoutException("Unable to find component for type - ${paymentMethod.type}")
        }
    }
}

internal fun DropInConfiguration.mapToParams(amount: Amount): DropInComponentParams {
    return DropInComponentParamsMapper().mapToParams(this, amount)
}

private fun Fragment.requireApplication(): Application = requireContext().applicationContext as Application

/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/4/2019.
 */

@file:Suppress("TooManyFunctions")

package com.adyen.checkout.dropin.internal.provider

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import com.adyen.checkout.ach.ACHDirectDebitComponent
import com.adyen.checkout.ach.ACHDirectDebitComponentState
import com.adyen.checkout.ach.ACHDirectDebitConfiguration
import com.adyen.checkout.ach.internal.provider.ACHDirectDebitComponentProvider
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.bacs.BacsDirectDebitComponentState
import com.adyen.checkout.bacs.BacsDirectDebitConfiguration
import com.adyen.checkout.bacs.internal.provider.BacsDirectDebitComponentProvider
import com.adyen.checkout.bcmc.BcmcComponent
import com.adyen.checkout.bcmc.BcmcComponentState
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.bcmc.internal.provider.BcmcComponentProvider
import com.adyen.checkout.blik.BlikComponent
import com.adyen.checkout.blik.BlikComponentState
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.blik.internal.provider.BlikComponentProvider
import com.adyen.checkout.boleto.BoletoComponent
import com.adyen.checkout.boleto.BoletoComponentState
import com.adyen.checkout.boleto.BoletoConfiguration
import com.adyen.checkout.boleto.internal.provider.BoletoComponentProvider
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.internal.provider.CardComponentProvider
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.cashapppay.CashAppPayConfiguration
import com.adyen.checkout.cashapppay.internal.provider.CashAppPayComponentProvider
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.AlwaysAvailablePaymentMethod
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.NotAvailablePaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentMethodAvailabilityCheck
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponent
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponentState
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPConfiguration
import com.adyen.checkout.conveniencestoresjp.internal.provider.ConvenienceStoresJPComponentProvider
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.core.internal.util.runCompileOnly
import com.adyen.checkout.dotpay.DotpayComponent
import com.adyen.checkout.dotpay.DotpayComponentState
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.dotpay.internal.provider.DotpayComponentProvider
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.internal.ui.BacsDirectDebitDialogFragment
import com.adyen.checkout.dropin.internal.ui.CardComponentDialogFragment
import com.adyen.checkout.dropin.internal.ui.DropInBottomSheetDialogFragment
import com.adyen.checkout.dropin.internal.ui.GenericComponentDialogFragment
import com.adyen.checkout.dropin.internal.ui.GiftCardComponentDialogFragment
import com.adyen.checkout.dropin.internal.ui.GooglePayComponentDialogFragment
import com.adyen.checkout.dropin.internal.ui.model.DropInComponentParams
import com.adyen.checkout.dropin.internal.ui.model.DropInComponentParamsMapper
import com.adyen.checkout.entercash.EntercashComponent
import com.adyen.checkout.entercash.EntercashComponentState
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.entercash.internal.provider.EntercashComponentProvider
import com.adyen.checkout.eps.EPSComponent
import com.adyen.checkout.eps.EPSComponentState
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.eps.internal.provider.EPSComponentProvider
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardComponentCallback
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.giftcard.internal.provider.GiftCardComponentProvider
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.internal.provider.GooglePayComponentProvider
import com.adyen.checkout.ideal.IdealComponent
import com.adyen.checkout.ideal.IdealComponentState
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.ideal.internal.provider.IdealComponentProvider
import com.adyen.checkout.instant.InstantComponentState
import com.adyen.checkout.instant.InstantPaymentComponent
import com.adyen.checkout.instant.InstantPaymentConfiguration
import com.adyen.checkout.instant.internal.provider.InstantPaymentComponentProvider
import com.adyen.checkout.mbway.MBWayComponent
import com.adyen.checkout.mbway.MBWayComponentState
import com.adyen.checkout.mbway.MBWayConfiguration
import com.adyen.checkout.mbway.internal.provider.MBWayComponentProvider
import com.adyen.checkout.molpay.MolpayComponent
import com.adyen.checkout.molpay.MolpayComponentState
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.molpay.internal.provider.MolpayComponentProvider
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponent
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponentState
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZConfiguration
import com.adyen.checkout.onlinebankingcz.internal.provider.OnlineBankingCZComponentProvider
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPComponent
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPComponentState
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPConfiguration
import com.adyen.checkout.onlinebankingjp.internal.provider.OnlineBankingJPComponentProvider
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponent
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponentState
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLConfiguration
import com.adyen.checkout.onlinebankingpl.internal.provider.OnlineBankingPLComponentProvider
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKComponent
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKComponentState
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKConfiguration
import com.adyen.checkout.onlinebankingsk.internal.provider.OnlineBankingSKComponentProvider
import com.adyen.checkout.openbanking.OpenBankingComponent
import com.adyen.checkout.openbanking.OpenBankingComponentState
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.openbanking.internal.provider.OpenBankingComponentProvider
import com.adyen.checkout.paybybank.PayByBankComponent
import com.adyen.checkout.paybybank.PayByBankComponentState
import com.adyen.checkout.paybybank.PayByBankConfiguration
import com.adyen.checkout.paybybank.internal.provider.PayByBankComponentProvider
import com.adyen.checkout.payeasy.PayEasyComponent
import com.adyen.checkout.payeasy.PayEasyComponentState
import com.adyen.checkout.payeasy.PayEasyConfiguration
import com.adyen.checkout.payeasy.internal.provider.PayEasyComponentProvider
import com.adyen.checkout.sepa.SepaComponent
import com.adyen.checkout.sepa.SepaComponentState
import com.adyen.checkout.sepa.SepaConfiguration
import com.adyen.checkout.sepa.internal.provider.SepaComponentProvider
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetails
import com.adyen.checkout.sessions.core.internal.data.model.mapToParams
import com.adyen.checkout.seveneleven.SevenElevenComponent
import com.adyen.checkout.seveneleven.SevenElevenComponentState
import com.adyen.checkout.seveneleven.SevenElevenConfiguration
import com.adyen.checkout.seveneleven.internal.provider.SevenElevenComponentProvider
import com.adyen.checkout.upi.UPIComponent
import com.adyen.checkout.upi.UPIComponentState
import com.adyen.checkout.upi.UPIConfiguration
import com.adyen.checkout.upi.internal.provider.UPIComponentProvider
import com.adyen.checkout.wechatpay.internal.WeChatPayProvider

private val TAG = LogUtil.getTag()

internal inline fun <reified T : Configuration> getConfigurationForPaymentMethod(
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    context: Context,
): T {
    val paymentMethodType = paymentMethod.type ?: throw CheckoutException("Payment method type is null")
    return dropInConfiguration.getConfigurationForPaymentMethod(paymentMethodType) ?: getDefaultConfigForPaymentMethod(
        paymentMethod,
        dropInConfiguration,
        context,
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
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } ->
            ACHDirectDebitConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } ->
            BlikConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } ->
            CardConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } ->
            CashAppPayConfiguration.Builder(
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

@Suppress("LongMethod", "CyclomaticComplexMethod")
internal fun <T : Configuration> getDefaultConfigForPaymentMethod(
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    context: Context,
): T {
    val shopperLocale = dropInConfiguration.shopperLocale
    val environment = dropInConfiguration.environment
    val clientKey = dropInConfiguration.clientKey

    // get default builder for Configuration type
    val builder: BaseConfigurationBuilder<*, *> = when {
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            ACHDirectDebitConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            BacsDirectDebitConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { BcmcComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            BcmcConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            BlikConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { BoletoComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            BoletoConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            CardConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            CashAppPayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )
                .setReturnUrl(CashAppPayComponent.getReturnUrl(context))

        checkCompileOnly { ConvenienceStoresJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            ConvenienceStoresJPConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { DotpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            DotpayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { EntercashComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            EntercashConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { EPSComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            EPSConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { GiftCardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            GiftCardConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { GooglePayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            GooglePayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { IdealComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            IdealConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { InstantPaymentComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            InstantPaymentConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { MBWayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            MBWayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { MolpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            MolpayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { OnlineBankingCZComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            OnlineBankingCZConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { OnlineBankingJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            OnlineBankingJPConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { OnlineBankingPLComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            OnlineBankingPLConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { OnlineBankingSKComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            OnlineBankingSKConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { OpenBankingComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            OpenBankingConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { PayByBankComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            PayByBankConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { PayEasyComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            PayEasyConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { SepaComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            SepaConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { SevenElevenComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            SevenElevenConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey
            )

        checkCompileOnly { UPIComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            UPIConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        else -> throw CheckoutException("Unable to find component configuration for paymentMethod - $paymentMethod")
    }

    @Suppress("UNCHECKED_CAST")
    return builder.build() as T
}

private inline fun <reified T : Configuration> getConfigurationForPaymentMethodOrNull(
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    context: Context,
): T? {
    @Suppress("SwallowedException")
    return try {
        getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
    } catch (e: CheckoutException) {
        null
    }
}

@Suppress("LongParameterList")
internal fun checkPaymentMethodAvailability(
    application: Application,
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    amount: Amount,
    sessionDetails: SessionDetails?,
    callback: ComponentAvailableCallback,
) {
    try {
        Logger.v(TAG, "Checking availability for type - ${paymentMethod.type}")

        val type = paymentMethod.type ?: throw CheckoutException("PaymentMethod type is null")

        val availabilityCheck = getPaymentMethodAvailabilityCheck(dropInConfiguration, type, amount, sessionDetails)
        val configuration =
            getConfigurationForPaymentMethodOrNull<Configuration>(paymentMethod, dropInConfiguration, application)

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
    sessionDetails: SessionDetails?,
): PaymentMethodAvailabilityCheck<Configuration> {
    val dropInParams = dropInConfiguration.mapToParams(amount)
    val sessionParams = sessionDetails?.mapToParams(amount)

    @Suppress("UNCHECKED_CAST")
    val availabilityCheck = when (paymentMethodType) {
        PaymentMethodTypes.GOOGLE_PAY,
        PaymentMethodTypes.GOOGLE_PAY_LEGACY -> runCompileOnly {
            GooglePayComponentProvider(dropInParams, sessionParams)
        }

        PaymentMethodTypes.WECHAT_PAY_SDK -> runCompileOnly { WeChatPayProvider() }
        else -> AlwaysAvailablePaymentMethod()
    } as? PaymentMethodAvailabilityCheck<Configuration>

    return availabilityCheck ?: NotAvailablePaymentMethod()
}

/**
 * Provides a [PaymentComponent] from a [PaymentComponentProvider] using the [StoredPaymentMethod] reference.
 *
 * @param fragment The Fragment which the PaymentComponent lifecycle will be bound to.
 * @param storedPaymentMethod The stored payment method to be parsed.
 * @throws CheckoutException In case a component cannot be created.
 */
@Suppress("UNCHECKED_CAST", "LongParameterList")
internal fun getComponentFor(
    fragment: Fragment,
    storedPaymentMethod: StoredPaymentMethod,
    dropInConfiguration: DropInConfiguration,
    amount: Amount,
    componentCallback: ComponentCallback<*>,
    sessionDetails: SessionDetails?,
    onRedirect: () -> Unit,
): PaymentComponent {
    val dropInParams = dropInConfiguration.mapToParams(amount)
    val sessionParams = sessionDetails?.mapToParams(amount)
    return when {
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            val achConfig: ACHDirectDebitConfiguration =
                getConfigurationForPaymentMethod(storedPaymentMethod, dropInConfiguration)
            ACHDirectDebitComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                configuration = achConfig,
                callback = componentCallback as ComponentCallback<ACHDirectDebitComponentState>,
                key = storedPaymentMethod.id
            )
        }

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            val cardConfig: CardConfiguration =
                getConfigurationForPaymentMethod(storedPaymentMethod, dropInConfiguration)
            CardComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                configuration = cardConfig,
                callback = componentCallback as ComponentCallback<CardComponentState>,
                key = storedPaymentMethod.id
            )
        }

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            val cashAppPayConfig: CashAppPayConfiguration =
                getConfigurationForPaymentMethod(storedPaymentMethod, dropInConfiguration)
            CashAppPayComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                configuration = cashAppPayConfig,
                callback = componentCallback as ComponentCallback<CashAppPayComponentState>,
                key = storedPaymentMethod.id
            )
        }

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            val blikConfig: BlikConfiguration =
                getConfigurationForPaymentMethod(storedPaymentMethod, dropInConfiguration)
            BlikComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                configuration = blikConfig,
                callback = componentCallback as ComponentCallback<BlikComponentState>,
                key = storedPaymentMethod.id
            )
        }

        else -> {
            throw CheckoutException("Unable to find stored component for type - ${storedPaymentMethod.type}")
        }
    }.apply {
        setOnRedirectListener(onRedirect)
    }
}

/**
 * Provides a [PaymentComponent] from a [PaymentComponentProvider] using the [PaymentMethod] reference.
 *
 * @param fragment The Fragment which the PaymentComponent lifecycle will be bound to.
 * @param paymentMethod The payment method to be parsed.
 * @throws CheckoutException In case a component cannot be created.
 */
@Suppress("LongMethod", "UNCHECKED_CAST", "LongParameterList", "CyclomaticComplexMethod")
internal fun getComponentFor(
    fragment: Fragment,
    paymentMethod: PaymentMethod,
    dropInConfiguration: DropInConfiguration,
    amount: Amount,
    componentCallback: ComponentCallback<*>,
    sessionDetails: SessionDetails?,
    onRedirect: () -> Unit,
): PaymentComponent {
    val dropInParams = dropInConfiguration.mapToParams(amount)
    val sessionParams = sessionDetails?.mapToParams(amount)
    val context = fragment.requireContext()
    return when {
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val configuration: ACHDirectDebitConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            ACHDirectDebitComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = configuration,
                callback = componentCallback as ComponentCallback<ACHDirectDebitComponentState>,
            )
        }

        checkCompileOnly { BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val bacsConfiguration: BacsDirectDebitConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            BacsDirectDebitComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = bacsConfiguration,
                callback = componentCallback as ComponentCallback<BacsDirectDebitComponentState>,
            )
        }

        checkCompileOnly { BcmcComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val bcmcConfiguration: BcmcConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            BcmcComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = bcmcConfiguration,
                callback = componentCallback as ComponentCallback<BcmcComponentState>,
            )
        }

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val blikConfiguration: BlikConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            BlikComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = blikConfiguration,
                callback = componentCallback as ComponentCallback<BlikComponentState>,
            )
        }

        checkCompileOnly { BoletoComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val boletoConfiguration: BoletoConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            BoletoComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = boletoConfiguration,
                callback = componentCallback as ComponentCallback<BoletoComponentState>,
            )
        }

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val cardConfig: CardConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            CardComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = cardConfig,
                callback = componentCallback as ComponentCallback<CardComponentState>,
            )
        }

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val cashAppPayConfiguration: CashAppPayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            CashAppPayComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = cashAppPayConfiguration,
                callback = componentCallback as ComponentCallback<CashAppPayComponentState>,
            )
        }

        checkCompileOnly { ConvenienceStoresJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val convenienceStoresJPConfiguration: ConvenienceStoresJPConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            ConvenienceStoresJPComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = convenienceStoresJPConfiguration,
                callback = componentCallback as ComponentCallback<ConvenienceStoresJPComponentState>,
            )
        }

        checkCompileOnly { DotpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val dotpayConfig: DotpayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            DotpayComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = dotpayConfig,
                callback = componentCallback as ComponentCallback<DotpayComponentState>,
            )
        }

        checkCompileOnly { EntercashComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val entercashConfig: EntercashConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            EntercashComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = entercashConfig,
                callback = componentCallback as ComponentCallback<EntercashComponentState>,
            )
        }

        checkCompileOnly { EPSComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val epsConfig: EPSConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            EPSComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = epsConfig,
                callback = componentCallback as ComponentCallback<EPSComponentState>,
            )
        }

        checkCompileOnly { GiftCardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val giftcardConfiguration: GiftCardConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            GiftCardComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = giftcardConfiguration,
                callback = componentCallback as GiftCardComponentCallback,
            )
        }

        checkCompileOnly { GooglePayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val googlePayConfiguration: GooglePayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            GooglePayComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = googlePayConfiguration,
                callback = componentCallback as ComponentCallback<GooglePayComponentState>,
            )
        }

        checkCompileOnly { IdealComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val idealConfig: IdealConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            IdealComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = idealConfig,
                callback = componentCallback as ComponentCallback<IdealComponentState>,
            )
        }

        checkCompileOnly { InstantPaymentComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val instantPaymentConfiguration: InstantPaymentConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            InstantPaymentComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = instantPaymentConfiguration,
                callback = componentCallback as ComponentCallback<InstantComponentState>,
            )
        }

        checkCompileOnly { MBWayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val mbWayConfiguration: MBWayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            MBWayComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = mbWayConfiguration,
                callback = componentCallback as ComponentCallback<MBWayComponentState>,
            )
        }

        checkCompileOnly { MolpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val molpayConfig: MolpayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            MolpayComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = molpayConfig,
                callback = componentCallback as ComponentCallback<MolpayComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingCZComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val onlineBankingCZConfig: OnlineBankingCZConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            OnlineBankingCZComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = onlineBankingCZConfig,
                callback = componentCallback as ComponentCallback<OnlineBankingCZComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val onlineBankingJPConfig: OnlineBankingJPConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            OnlineBankingJPComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = onlineBankingJPConfig,
                callback = componentCallback as ComponentCallback<OnlineBankingJPComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingPLComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val onlineBankingPLConfig: OnlineBankingPLConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            OnlineBankingPLComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = onlineBankingPLConfig,
                callback = componentCallback as ComponentCallback<OnlineBankingPLComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingSKComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val onlineBankingSKConfig: OnlineBankingSKConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            OnlineBankingSKComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = onlineBankingSKConfig,
                callback = componentCallback as ComponentCallback<OnlineBankingSKComponentState>,
            )
        }

        checkCompileOnly { OpenBankingComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val openBankingConfig: OpenBankingConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            OpenBankingComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = openBankingConfig,
                callback = componentCallback as ComponentCallback<OpenBankingComponentState>,
            )
        }

        checkCompileOnly { PayByBankComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val payByBankConfig: PayByBankConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            PayByBankComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = payByBankConfig,
                callback = componentCallback as ComponentCallback<PayByBankComponentState>,
            )
        }

        checkCompileOnly { PayEasyComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val payEasyConfiguration: PayEasyConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            PayEasyComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = payEasyConfiguration,
                callback = componentCallback as ComponentCallback<PayEasyComponentState>,
            )
        }

        checkCompileOnly { SepaComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val sepaConfiguration: SepaConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            SepaComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = sepaConfiguration,
                callback = componentCallback as ComponentCallback<SepaComponentState>,
            )
        }

        checkCompileOnly { SevenElevenComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val sevenElevenConfiguration: SevenElevenConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            SevenElevenComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = sevenElevenConfiguration,
                callback = componentCallback as ComponentCallback<SevenElevenComponentState>,
            )
        }

        checkCompileOnly { UPIComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val upiConfiguration: UPIConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, dropInConfiguration, context)
            UPIComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = upiConfiguration,
                callback = componentCallback as ComponentCallback<UPIComponentState>,
            )
        }

        else -> {
            throw CheckoutException("Unable to find component for type - ${paymentMethod.type}")
        }
    }.apply {
        setOnRedirectListener(onRedirect)
    }
}

internal fun DropInConfiguration.mapToParams(amount: Amount): DropInComponentParams {
    return DropInComponentParamsMapper().mapToParams(this, amount)
}

internal fun getFragmentForStoredPaymentMethod(
    storedPaymentMethod: StoredPaymentMethod,
    fromPreselected: Boolean
): DropInBottomSheetDialogFragment {
    return when {
        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            CardComponentDialogFragment.newInstance(storedPaymentMethod, fromPreselected)
        }

        else -> {
            GenericComponentDialogFragment.newInstance(storedPaymentMethod, fromPreselected)
        }
    }
}

internal fun getFragmentForPaymentMethod(paymentMethod: PaymentMethod): DropInBottomSheetDialogFragment {
    return when {
        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            CardComponentDialogFragment.newInstance(paymentMethod)
        }

        checkCompileOnly { BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            BacsDirectDebitDialogFragment.newInstance(paymentMethod)
        }

        checkCompileOnly { GiftCardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            GiftCardComponentDialogFragment.newInstance(paymentMethod)
        }

        checkCompileOnly { GooglePayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            GooglePayComponentDialogFragment.newInstance(paymentMethod)
        }

        else -> {
            GenericComponentDialogFragment.newInstance(paymentMethod)
        }
    }
}

internal inline fun checkCompileOnly(block: () -> Boolean): Boolean {
    return runCompileOnly(block) ?: false
}

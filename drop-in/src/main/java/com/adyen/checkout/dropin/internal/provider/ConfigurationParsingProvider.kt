/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/12/2023.
 */

package com.adyen.checkout.dropin.internal.provider

import android.content.Context
import com.adyen.checkout.ach.ACHDirectDebitComponent
import com.adyen.checkout.ach.ACHDirectDebitConfiguration
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.bacs.BacsDirectDebitConfiguration
import com.adyen.checkout.bcmc.BcmcComponent
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.blik.BlikComponent
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.boleto.BoletoComponent
import com.adyen.checkout.boleto.BoletoConfiguration
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.cashapppay.CashAppPayConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponent
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPConfiguration
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.dotpay.DotpayComponent
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.dropin.internal.util.checkCompileOnly
import com.adyen.checkout.entercash.EntercashComponent
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.eps.EPSComponent
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.ideal.IdealComponent
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.instant.InstantPaymentComponent
import com.adyen.checkout.instant.InstantPaymentConfiguration
import com.adyen.checkout.mbway.MBWayComponent
import com.adyen.checkout.mbway.MBWayConfiguration
import com.adyen.checkout.molpay.MolpayComponent
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponent
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZConfiguration
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPComponent
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPConfiguration
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponent
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLConfiguration
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKComponent
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKConfiguration
import com.adyen.checkout.openbanking.OpenBankingComponent
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.paybybank.PayByBankComponent
import com.adyen.checkout.paybybank.PayByBankConfiguration
import com.adyen.checkout.payeasy.PayEasyComponent
import com.adyen.checkout.payeasy.PayEasyConfiguration
import com.adyen.checkout.sepa.SepaComponent
import com.adyen.checkout.sepa.SepaConfiguration
import com.adyen.checkout.seveneleven.SevenElevenComponent
import com.adyen.checkout.seveneleven.SevenElevenConfiguration
import com.adyen.checkout.upi.UPIComponent
import com.adyen.checkout.upi.UPIConfiguration

internal inline fun <reified T : Configuration> getConfigurationForPaymentMethod(
    paymentMethod: PaymentMethod,
    checkoutConfiguration: CheckoutConfiguration,
    context: Context,
): T {
    val paymentMethodType = paymentMethod.type ?: throw CheckoutException("Payment method type is null")
    return checkoutConfiguration.getConfiguration(paymentMethodType) ?: getDefaultConfigForPaymentMethod(
        paymentMethod,
        checkoutConfiguration,
        context,
    )
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
internal fun <T : Configuration> getDefaultConfigForPaymentMethod(
    paymentMethod: PaymentMethod,
    checkoutConfiguration: CheckoutConfiguration,
    context: Context,
): T {
    val shopperLocale = checkoutConfiguration.shopperLocale
    val environment = checkoutConfiguration.environment
    val clientKey = checkoutConfiguration.clientKey

    // get default builder for Configuration type
    val builder: BaseConfigurationBuilder<*, *> = when {
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            ACHDirectDebitConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            BacsDirectDebitConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { BcmcComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            BcmcConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            BlikConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { BoletoComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            BoletoConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            CardConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            CashAppPayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )
                .setReturnUrl(CashAppPayComponent.getReturnUrl(context))

        checkCompileOnly { ConvenienceStoresJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            ConvenienceStoresJPConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { DotpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            DotpayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { EntercashComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            EntercashConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { EPSComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            EPSConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { GiftCardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            GiftCardConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { GooglePayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            GooglePayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { IdealComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            IdealConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { InstantPaymentComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            InstantPaymentConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { MBWayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            MBWayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { MolpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            MolpayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { OnlineBankingCZComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            OnlineBankingCZConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { OnlineBankingJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            OnlineBankingJPConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { OnlineBankingPLComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            OnlineBankingPLConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { OnlineBankingSKComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            OnlineBankingSKConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { OpenBankingComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            OpenBankingConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { PayByBankComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            PayByBankConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { PayEasyComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            PayEasyConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { SepaComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            SepaConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { SevenElevenComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } ->
            SevenElevenConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
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

internal inline fun <reified T : Configuration> getConfigurationForPaymentMethod(
    storedPaymentMethod: StoredPaymentMethod,
    checkoutConfiguration: CheckoutConfiguration,
): T {
    val storedPaymentMethodType = storedPaymentMethod.type ?: throw CheckoutException("Payment method type is null")
    return checkoutConfiguration.getConfiguration(storedPaymentMethodType) ?: getDefaultConfigForPaymentMethod(
        storedPaymentMethod = storedPaymentMethod,
        checkoutConfiguration = checkoutConfiguration,
    )
}

internal fun <T : Configuration> getDefaultConfigForPaymentMethod(
    storedPaymentMethod: StoredPaymentMethod,
    checkoutConfiguration: CheckoutConfiguration,
): T {
    val shopperLocale = checkoutConfiguration.shopperLocale
    val environment = checkoutConfiguration.environment
    val clientKey = checkoutConfiguration.clientKey

    val builder: BaseConfigurationBuilder<*, *> = when {
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } ->
            ACHDirectDebitConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } ->
            BlikConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } ->
            CardConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } ->
            CashAppPayConfiguration.Builder(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )

        else -> throw CheckoutException(
            errorMessage = "Unable to find component configuration for storedPaymentMethod - $storedPaymentMethod",
        )
    }
    @Suppress("UNCHECKED_CAST")
    return builder.build() as T
}

internal inline fun <reified T : Configuration> getConfigurationForPaymentMethodOrNull(
    paymentMethod: PaymentMethod,
    checkoutConfiguration: CheckoutConfiguration,
): T? {
    return checkoutConfiguration.getConfiguration(paymentMethod.type ?: "")
}

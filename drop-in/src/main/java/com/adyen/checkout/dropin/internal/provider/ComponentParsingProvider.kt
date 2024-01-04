/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/4/2019.
 */

package com.adyen.checkout.dropin.internal.provider

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
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponent
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponentState
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPConfiguration
import com.adyen.checkout.conveniencestoresjp.internal.provider.ConvenienceStoresJPComponentProvider
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.dotpay.DotpayComponent
import com.adyen.checkout.dotpay.DotpayComponentState
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.dotpay.internal.provider.DotpayComponentProvider
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.getDropInConfiguration
import com.adyen.checkout.dropin.internal.ui.model.DropInComponentParams
import com.adyen.checkout.dropin.internal.ui.model.DropInComponentParamsMapper
import com.adyen.checkout.dropin.internal.util.checkCompileOnly
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
    checkoutConfiguration: CheckoutConfiguration,
    amount: Amount?,
    componentCallback: ComponentCallback<*>,
    sessionDetails: SessionDetails?,
    analyticsRepository: AnalyticsRepository,
    onRedirect: () -> Unit,
): PaymentComponent {
    val dropInParams = checkoutConfiguration.getDropInConfiguration()?.mapToParams(amount)
    val sessionParams = sessionDetails?.mapToParams(amount)
    return when {
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            val achConfig: ACHDirectDebitConfiguration =
                getConfigurationForPaymentMethod(storedPaymentMethod, checkoutConfiguration)
            ACHDirectDebitComponentProvider(true, sessionParams, analyticsRepository).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                configuration = achConfig,
                callback = componentCallback as ComponentCallback<ACHDirectDebitComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            val cardConfig: CardConfiguration =
                getConfigurationForPaymentMethod(storedPaymentMethod, checkoutConfiguration)
            CardComponentProvider(true, sessionParams, analyticsRepository).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                configuration = cardConfig,
                callback = componentCallback as ComponentCallback<CardComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            val cashAppPayConfig: CashAppPayConfiguration =
                getConfigurationForPaymentMethod(storedPaymentMethod, checkoutConfiguration)
            CashAppPayComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                configuration = cashAppPayConfig,
                callback = componentCallback as ComponentCallback<CashAppPayComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            val blikConfig: BlikConfiguration =
                getConfigurationForPaymentMethod(storedPaymentMethod, checkoutConfiguration)
            BlikComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                configuration = blikConfig,
                callback = componentCallback as ComponentCallback<BlikComponentState>,
                key = storedPaymentMethod.id,
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
    checkoutConfiguration: CheckoutConfiguration,
    amount: Amount?,
    componentCallback: ComponentCallback<*>,
    sessionDetails: SessionDetails?,
    analyticsRepository: AnalyticsRepository,
    onRedirect: () -> Unit,
): PaymentComponent {
    val dropInParams = checkoutConfiguration.getDropInConfiguration()?.mapToParams(amount)
    val sessionParams = sessionDetails?.mapToParams(amount)
    val context = fragment.requireContext()
    return when {
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val configuration: ACHDirectDebitConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            ACHDirectDebitComponentProvider(true, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = configuration,
                callback = componentCallback as ComponentCallback<ACHDirectDebitComponentState>,
            )
        }

        checkCompileOnly { BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val bacsConfiguration: BacsDirectDebitConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            BacsDirectDebitComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = bacsConfiguration,
                callback = componentCallback as ComponentCallback<BacsDirectDebitComponentState>,
            )
        }

        checkCompileOnly { BcmcComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val bcmcConfiguration: BcmcConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            BcmcComponentProvider(true, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = bcmcConfiguration,
                callback = componentCallback as ComponentCallback<BcmcComponentState>,
            )
        }

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val blikConfiguration: BlikConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            BlikComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = blikConfiguration,
                callback = componentCallback as ComponentCallback<BlikComponentState>,
            )
        }

        checkCompileOnly { BoletoComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val boletoConfiguration: BoletoConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            BoletoComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = boletoConfiguration,
                callback = componentCallback as ComponentCallback<BoletoComponentState>,
            )
        }

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val cardConfig: CardConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            CardComponentProvider(true, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = cardConfig,
                callback = componentCallback as ComponentCallback<CardComponentState>,
            )
        }

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val cashAppPayConfiguration: CashAppPayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            CashAppPayComponentProvider(dropInParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = cashAppPayConfiguration,
                callback = componentCallback as ComponentCallback<CashAppPayComponentState>,
            )
        }

        checkCompileOnly { ConvenienceStoresJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val convenienceStoresJPConfiguration: ConvenienceStoresJPConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            ConvenienceStoresJPComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = convenienceStoresJPConfiguration,
                callback = componentCallback as ComponentCallback<ConvenienceStoresJPComponentState>,
            )
        }

        checkCompileOnly { DotpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val dotpayConfig: DotpayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            DotpayComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = dotpayConfig,
                callback = componentCallback as ComponentCallback<DotpayComponentState>,
            )
        }

        checkCompileOnly { EntercashComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val entercashConfig: EntercashConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            EntercashComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = entercashConfig,
                callback = componentCallback as ComponentCallback<EntercashComponentState>,
            )
        }

        checkCompileOnly { EPSComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val epsConfig: EPSConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            EPSComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = epsConfig,
                callback = componentCallback as ComponentCallback<EPSComponentState>,
            )
        }

        checkCompileOnly { GiftCardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val giftcardConfiguration: GiftCardConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            GiftCardComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = giftcardConfiguration,
                callback = componentCallback as GiftCardComponentCallback,
            )
        }

        checkCompileOnly { GooglePayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val googlePayConfiguration: GooglePayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            GooglePayComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = googlePayConfiguration,
                callback = componentCallback as ComponentCallback<GooglePayComponentState>,
            )
        }

        checkCompileOnly { IdealComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val idealConfig: IdealConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            IdealComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = idealConfig,
                callback = componentCallback as ComponentCallback<IdealComponentState>,
            )
        }

        checkCompileOnly { InstantPaymentComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val instantPaymentConfiguration: InstantPaymentConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            InstantPaymentComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = instantPaymentConfiguration,
                callback = componentCallback as ComponentCallback<InstantComponentState>,
            )
        }

        checkCompileOnly { MBWayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val mbWayConfiguration: MBWayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            MBWayComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = mbWayConfiguration,
                callback = componentCallback as ComponentCallback<MBWayComponentState>,
            )
        }

        checkCompileOnly { MolpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val molpayConfig: MolpayConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            MolpayComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = molpayConfig,
                callback = componentCallback as ComponentCallback<MolpayComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingCZComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val onlineBankingCZConfig: OnlineBankingCZConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            OnlineBankingCZComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = onlineBankingCZConfig,
                callback = componentCallback as ComponentCallback<OnlineBankingCZComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val onlineBankingJPConfig: OnlineBankingJPConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            OnlineBankingJPComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = onlineBankingJPConfig,
                callback = componentCallback as ComponentCallback<OnlineBankingJPComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingPLComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val onlineBankingPLConfig: OnlineBankingPLConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            OnlineBankingPLComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = onlineBankingPLConfig,
                callback = componentCallback as ComponentCallback<OnlineBankingPLComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingSKComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val onlineBankingSKConfig: OnlineBankingSKConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            OnlineBankingSKComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = onlineBankingSKConfig,
                callback = componentCallback as ComponentCallback<OnlineBankingSKComponentState>,
            )
        }

        checkCompileOnly { OpenBankingComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val openBankingConfig: OpenBankingConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            OpenBankingComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = openBankingConfig,
                callback = componentCallback as ComponentCallback<OpenBankingComponentState>,
            )
        }

        checkCompileOnly { PayByBankComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val payByBankConfig: PayByBankConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            PayByBankComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = payByBankConfig,
                callback = componentCallback as ComponentCallback<PayByBankComponentState>,
            )
        }

        checkCompileOnly { PayEasyComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val payEasyConfiguration: PayEasyConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            PayEasyComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = payEasyConfiguration,
                callback = componentCallback as ComponentCallback<PayEasyComponentState>,
            )
        }

        checkCompileOnly { SepaComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val sepaConfiguration: SepaConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            SepaComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = sepaConfiguration,
                callback = componentCallback as ComponentCallback<SepaComponentState>,
            )
        }

        checkCompileOnly { SevenElevenComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val sevenElevenConfiguration: SevenElevenConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            SevenElevenComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                configuration = sevenElevenConfiguration,
                callback = componentCallback as ComponentCallback<SevenElevenComponentState>,
            )
        }

        checkCompileOnly { UPIComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            val upiConfiguration: UPIConfiguration =
                getConfigurationForPaymentMethod(paymentMethod, checkoutConfiguration, context)
            UPIComponentProvider(dropInParams, sessionParams, analyticsRepository).get(
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

internal fun DropInConfiguration.mapToParams(amount: Amount?): DropInComponentParams {
    return DropInComponentParamsMapper().mapToParams(this, amount)
}

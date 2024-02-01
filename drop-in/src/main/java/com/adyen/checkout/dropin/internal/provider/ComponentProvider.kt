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
import com.adyen.checkout.ach.internal.provider.ACHDirectDebitComponentProvider
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.bacs.BacsDirectDebitComponentState
import com.adyen.checkout.bacs.internal.provider.BacsDirectDebitComponentProvider
import com.adyen.checkout.bcmc.BcmcComponent
import com.adyen.checkout.bcmc.BcmcComponentState
import com.adyen.checkout.bcmc.internal.provider.BcmcComponentProvider
import com.adyen.checkout.blik.BlikComponent
import com.adyen.checkout.blik.BlikComponentState
import com.adyen.checkout.blik.internal.provider.BlikComponentProvider
import com.adyen.checkout.boleto.BoletoComponent
import com.adyen.checkout.boleto.BoletoComponentState
import com.adyen.checkout.boleto.internal.provider.BoletoComponentProvider
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.internal.provider.CardComponentProvider
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.cashapppay.internal.provider.CashAppPayComponentProvider
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponent
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponentState
import com.adyen.checkout.conveniencestoresjp.internal.provider.ConvenienceStoresJPComponentProvider
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.dotpay.DotpayComponent
import com.adyen.checkout.dotpay.DotpayComponentState
import com.adyen.checkout.dotpay.internal.provider.DotpayComponentProvider
import com.adyen.checkout.dropin.internal.ui.model.DropInComponentParams
import com.adyen.checkout.dropin.internal.ui.model.DropInComponentParamsMapper
import com.adyen.checkout.dropin.internal.util.checkCompileOnly
import com.adyen.checkout.entercash.EntercashComponent
import com.adyen.checkout.entercash.EntercashComponentState
import com.adyen.checkout.entercash.internal.provider.EntercashComponentProvider
import com.adyen.checkout.eps.EPSComponent
import com.adyen.checkout.eps.EPSComponentState
import com.adyen.checkout.eps.internal.provider.EPSComponentProvider
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardComponentCallback
import com.adyen.checkout.giftcard.internal.provider.GiftCardComponentProvider
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.googlepay.internal.provider.GooglePayComponentProvider
import com.adyen.checkout.ideal.IdealComponent
import com.adyen.checkout.ideal.IdealComponentState
import com.adyen.checkout.ideal.internal.provider.IdealComponentProvider
import com.adyen.checkout.instant.InstantComponentState
import com.adyen.checkout.instant.InstantPaymentComponent
import com.adyen.checkout.instant.internal.provider.InstantPaymentComponentProvider
import com.adyen.checkout.mbway.MBWayComponent
import com.adyen.checkout.mbway.MBWayComponentState
import com.adyen.checkout.mbway.internal.provider.MBWayComponentProvider
import com.adyen.checkout.molpay.MolpayComponent
import com.adyen.checkout.molpay.MolpayComponentState
import com.adyen.checkout.molpay.internal.provider.MolpayComponentProvider
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponent
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponentState
import com.adyen.checkout.onlinebankingcz.internal.provider.OnlineBankingCZComponentProvider
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPComponent
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPComponentState
import com.adyen.checkout.onlinebankingjp.internal.provider.OnlineBankingJPComponentProvider
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponent
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponentState
import com.adyen.checkout.onlinebankingpl.internal.provider.OnlineBankingPLComponentProvider
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKComponent
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKComponentState
import com.adyen.checkout.onlinebankingsk.internal.provider.OnlineBankingSKComponentProvider
import com.adyen.checkout.openbanking.OpenBankingComponent
import com.adyen.checkout.openbanking.OpenBankingComponentState
import com.adyen.checkout.openbanking.internal.provider.OpenBankingComponentProvider
import com.adyen.checkout.paybybank.PayByBankComponent
import com.adyen.checkout.paybybank.PayByBankComponentState
import com.adyen.checkout.paybybank.internal.provider.PayByBankComponentProvider
import com.adyen.checkout.payeasy.PayEasyComponent
import com.adyen.checkout.payeasy.PayEasyComponentState
import com.adyen.checkout.payeasy.internal.provider.PayEasyComponentProvider
import com.adyen.checkout.sepa.SepaComponent
import com.adyen.checkout.sepa.SepaComponentState
import com.adyen.checkout.sepa.internal.provider.SepaComponentProvider
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetails
import com.adyen.checkout.sessions.core.internal.data.model.mapToParams
import com.adyen.checkout.seveneleven.SevenElevenComponent
import com.adyen.checkout.seveneleven.SevenElevenComponentState
import com.adyen.checkout.seveneleven.internal.provider.SevenElevenComponentProvider
import com.adyen.checkout.upi.UPIComponent
import com.adyen.checkout.upi.UPIComponentState
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
    val dropInOverrideParams = DropInOverrideParams(amount)
    val sessionParams = sessionDetails?.mapToParams(amount)
    return when {
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            ACHDirectDebitComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<ACHDirectDebitComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            CardComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<CardComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            CashAppPayComponentProvider(dropInOverrideParams, sessionParams).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<CashAppPayComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            BlikComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                checkoutConfiguration = checkoutConfiguration,
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
    val dropInOverrideParams = DropInOverrideParams(amount)
    val sessionParams = sessionDetails?.mapToParams(amount)
    return when {
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            ACHDirectDebitComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<ACHDirectDebitComponentState>,
            )
        }

        checkCompileOnly { BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            BacsDirectDebitComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<BacsDirectDebitComponentState>,
            )
        }

        checkCompileOnly { BcmcComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            BcmcComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<BcmcComponentState>,
            )
        }

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            BlikComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<BlikComponentState>,
            )
        }

        checkCompileOnly { BoletoComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            BoletoComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<BoletoComponentState>,
            )
        }

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            CardComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<CardComponentState>,
            )
        }

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            CashAppPayComponentProvider(dropInOverrideParams, sessionParams).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<CashAppPayComponentState>,
            )
        }

        checkCompileOnly { ConvenienceStoresJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            ConvenienceStoresJPComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<ConvenienceStoresJPComponentState>,
            )
        }

        checkCompileOnly { DotpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            DotpayComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<DotpayComponentState>,
            )
        }

        checkCompileOnly { EntercashComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            EntercashComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<EntercashComponentState>,
            )
        }

        checkCompileOnly { EPSComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            EPSComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<EPSComponentState>,
            )
        }

        checkCompileOnly { GiftCardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            GiftCardComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as GiftCardComponentCallback,
            )
        }

        checkCompileOnly { GooglePayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            GooglePayComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<GooglePayComponentState>,
            )
        }

        checkCompileOnly { IdealComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            IdealComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<IdealComponentState>,
            )
        }

        checkCompileOnly { InstantPaymentComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            InstantPaymentComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<InstantComponentState>,
            )
        }

        checkCompileOnly { MBWayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            MBWayComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<MBWayComponentState>,
            )
        }

        checkCompileOnly { MolpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            MolpayComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<MolpayComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingCZComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            OnlineBankingCZComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<OnlineBankingCZComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            OnlineBankingJPComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<OnlineBankingJPComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingPLComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            OnlineBankingPLComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<OnlineBankingPLComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingSKComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            OnlineBankingSKComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<OnlineBankingSKComponentState>,
            )
        }

        checkCompileOnly { OpenBankingComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            OpenBankingComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<OpenBankingComponentState>,
            )
        }

        checkCompileOnly { PayByBankComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            PayByBankComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<PayByBankComponentState>,
            )
        }

        checkCompileOnly { PayEasyComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            PayEasyComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<PayEasyComponentState>,
            )
        }

        checkCompileOnly { SepaComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            SepaComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<SepaComponentState>,
            )
        }

        checkCompileOnly { SevenElevenComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            SevenElevenComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<SevenElevenComponentState>,
            )
        }

        checkCompileOnly { UPIComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            UPIComponentProvider(dropInOverrideParams, sessionParams, analyticsRepository).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
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

internal fun CheckoutConfiguration.mapToParams(amount: Amount?): DropInComponentParams {
    return DropInComponentParamsMapper().mapToParams(this, amount)
}

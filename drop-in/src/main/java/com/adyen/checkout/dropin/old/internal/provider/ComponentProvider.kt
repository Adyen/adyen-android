/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal.provider

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
import com.adyen.checkout.blik.old.BlikComponent
import com.adyen.checkout.blik.old.BlikComponentState
import com.adyen.checkout.blik.old.internal.provider.BlikComponentProvider
import com.adyen.checkout.boleto.BoletoComponent
import com.adyen.checkout.boleto.BoletoComponentState
import com.adyen.checkout.boleto.internal.provider.BoletoComponentProvider
import com.adyen.checkout.card.old.CardComponent
import com.adyen.checkout.card.old.CardComponentState
import com.adyen.checkout.card.old.internal.provider.CardComponentProvider
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.cashapppay.internal.provider.CashAppPayComponentProvider
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponent
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponentState
import com.adyen.checkout.conveniencestoresjp.internal.provider.ConvenienceStoresJPComponentProvider
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.dotpay.DotpayComponent
import com.adyen.checkout.dotpay.DotpayComponentState
import com.adyen.checkout.dotpay.internal.provider.DotpayComponentProvider
import com.adyen.checkout.dropin.old.internal.util.checkCompileOnly
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
import com.adyen.checkout.mbway.old.MBWayComponent
import com.adyen.checkout.mbway.old.MBWayComponentState
import com.adyen.checkout.mbway.old.internal.provider.MBWayComponentProvider
import com.adyen.checkout.mealvoucherfr.MealVoucherFRComponent
import com.adyen.checkout.mealvoucherfr.MealVoucherFRComponentCallback
import com.adyen.checkout.mealvoucherfr.internal.provider.MealVoucherFRComponentProvider
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
import com.adyen.checkout.paybybankus.PayByBankUSComponent
import com.adyen.checkout.paybybankus.PayByBankUSComponentState
import com.adyen.checkout.paybybankus.internal.provider.PayByBankUSComponentProvider
import com.adyen.checkout.payeasy.PayEasyComponent
import com.adyen.checkout.payeasy.PayEasyComponentState
import com.adyen.checkout.payeasy.internal.provider.PayEasyComponentProvider
import com.adyen.checkout.payto.PayToComponent
import com.adyen.checkout.payto.PayToComponentState
import com.adyen.checkout.payto.internal.provider.PayToComponentProvider
import com.adyen.checkout.sepa.SepaComponent
import com.adyen.checkout.sepa.SepaComponentState
import com.adyen.checkout.sepa.internal.provider.SepaComponentProvider
import com.adyen.checkout.seveneleven.SevenElevenComponent
import com.adyen.checkout.seveneleven.SevenElevenComponentState
import com.adyen.checkout.seveneleven.internal.provider.SevenElevenComponentProvider
import com.adyen.checkout.twint.TwintComponent
import com.adyen.checkout.twint.TwintComponentState
import com.adyen.checkout.twint.internal.provider.TwintComponentProvider
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
@Suppress("UNCHECKED_CAST", "LongParameterList", "LongMethod")
internal fun getComponentFor(
    fragment: Fragment,
    storedPaymentMethod: StoredPaymentMethod,
    checkoutConfiguration: CheckoutConfiguration,
    dropInOverrideParams: DropInOverrideParams,
    componentCallback: ComponentCallback<*>,
    analyticsManager: AnalyticsManager,
    onRedirect: () -> Unit,
): PaymentComponent {
    return when {
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            ACHDirectDebitComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<ACHDirectDebitComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            BlikComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<BlikComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            CashAppPayComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<CashAppPayComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            CardComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<CardComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { PayByBankUSComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            PayByBankUSComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<PayByBankUSComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { PayToComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            PayToComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<PayToComponentState>,
                key = storedPaymentMethod.id,
            )
        }

        checkCompileOnly { TwintComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            TwintComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                storedPaymentMethod = storedPaymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<TwintComponentState>,
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
    dropInOverrideParams: DropInOverrideParams,
    componentCallback: ComponentCallback<*>,
    analyticsManager: AnalyticsManager,
    onRedirect: () -> Unit,
): PaymentComponent {
    return when {
        checkCompileOnly { ACHDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            ACHDirectDebitComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<ACHDirectDebitComponentState>,
            )
        }

        checkCompileOnly { BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            BacsDirectDebitComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<BacsDirectDebitComponentState>,
            )
        }

        checkCompileOnly { BcmcComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            BcmcComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<BcmcComponentState>,
            )
        }

        checkCompileOnly { BlikComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            BlikComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<BlikComponentState>,
            )
        }

        checkCompileOnly { BoletoComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            BoletoComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<BoletoComponentState>,
            )
        }

        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            CardComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<CardComponentState>,
            )
        }

        checkCompileOnly { CashAppPayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            CashAppPayComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<CashAppPayComponentState>,
            )
        }

        checkCompileOnly { ConvenienceStoresJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            ConvenienceStoresJPComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<ConvenienceStoresJPComponentState>,
            )
        }

        checkCompileOnly { DotpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            DotpayComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<DotpayComponentState>,
            )
        }

        checkCompileOnly { EntercashComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            EntercashComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<EntercashComponentState>,
            )
        }

        checkCompileOnly { EPSComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            EPSComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<EPSComponentState>,
            )
        }

        checkCompileOnly { GiftCardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            GiftCardComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as GiftCardComponentCallback,
            )
        }

        checkCompileOnly { GooglePayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            GooglePayComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<GooglePayComponentState>,
            )
        }

        checkCompileOnly { IdealComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            IdealComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<IdealComponentState>,
            )
        }

        checkCompileOnly { MBWayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            MBWayComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<MBWayComponentState>,
            )
        }

        checkCompileOnly { MealVoucherFRComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            MealVoucherFRComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as MealVoucherFRComponentCallback,
            )
        }

        checkCompileOnly { MolpayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            MolpayComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<MolpayComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingCZComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            OnlineBankingCZComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<OnlineBankingCZComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingJPComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            OnlineBankingJPComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<OnlineBankingJPComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingPLComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            OnlineBankingPLComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<OnlineBankingPLComponentState>,
            )
        }

        checkCompileOnly { OnlineBankingSKComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            OnlineBankingSKComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<OnlineBankingSKComponentState>,
            )
        }

        checkCompileOnly { OpenBankingComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            OpenBankingComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<OpenBankingComponentState>,
            )
        }

        checkCompileOnly { PayByBankComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            PayByBankComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<PayByBankComponentState>,
            )
        }

        checkCompileOnly { PayByBankUSComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            PayByBankUSComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<PayByBankUSComponentState>,
            )
        }

        checkCompileOnly { PayEasyComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            PayEasyComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<PayEasyComponentState>,
            )
        }

        checkCompileOnly { PayToComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            PayToComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<PayToComponentState>,
            )
        }

        checkCompileOnly { SepaComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            SepaComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<SepaComponentState>,
            )
        }

        checkCompileOnly { SevenElevenComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            SevenElevenComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<SevenElevenComponentState>,
            )
        }

        checkCompileOnly { TwintComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            TwintComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<TwintComponentState>,
            )
        }

        checkCompileOnly { UPIComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            UPIComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<UPIComponentState>,
            )
        }

        // InstantPaymentComponent has to be checked last, since it's the only component which doesn't explicitly lists
        // which payment methods it supports. Meaning it could take over a payment method that should be handled by
        // it's dedicated component.
        checkCompileOnly { InstantPaymentComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            InstantPaymentComponentProvider(dropInOverrideParams, analyticsManager).get(
                fragment = fragment,
                paymentMethod = paymentMethod,
                checkoutConfiguration = checkoutConfiguration,
                callback = componentCallback as ComponentCallback<InstantComponentState>,
            )
        }

        else -> {
            throw CheckoutException("Unable to find component for type - ${paymentMethod.type}")
        }
    }.apply {
        setOnRedirectListener(onRedirect)
    }
}

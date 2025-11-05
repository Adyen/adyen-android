/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.dropin.old.internal.ui.model.OrderModel
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetails
import com.adyen.checkout.sessions.core.internal.data.model.mapToDetails

internal class DropInSavedStateHandleContainer(
    override val savedStateHandle: SavedStateHandle,
) : SavedStateHandleContainer {

    var checkoutConfiguration: CheckoutConfiguration? by SavedStateHandleProperty(CHECKOUT_CONFIGURATION_KEY)
    var serviceComponentName: ComponentName? by SavedStateHandleProperty(DROP_IN_SERVICE_KEY)
    var sessionDetails: SessionDetails? by SavedStateHandleProperty(SESSION_KEY)
    var isSessionsFlowTakenOver: Boolean? by SavedStateHandleProperty(IS_SESSIONS_FLOW_TAKEN_OVER_KEY)
    var paymentMethodsApiResponse: PaymentMethodsApiResponse? by SavedStateHandleProperty(PAYMENT_METHODS_RESPONSE_KEY)
    var isWaitingResult: Boolean? by SavedStateHandleProperty(IS_WAITING_FOR_RESULT_KEY)
    var cachedGiftCardComponentState: GiftCardComponentState? by SavedStateHandleProperty(CACHED_GIFT_CARD)
    var cachedPartialPaymentAmount: Amount? by SavedStateHandleProperty(PARTIAL_PAYMENT_AMOUNT)
    var currentOrder: OrderModel? by SavedStateHandleProperty(CURRENT_ORDER)
}

internal object DropInBundleHandler {

    fun putIntentExtras(
        intent: Intent,
        checkoutConfiguration: CheckoutConfiguration,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        service: ComponentName,
    ) {
        intent.apply {
            putExtra(PAYMENT_METHODS_RESPONSE_KEY, paymentMethodsApiResponse)
            putExtra(CHECKOUT_CONFIGURATION_KEY, checkoutConfiguration)
            putExtra(DROP_IN_SERVICE_KEY, service)
        }
    }

    fun putIntentExtras(
        intent: Intent,
        checkoutConfiguration: CheckoutConfiguration,
        checkoutSession: CheckoutSession,
        service: ComponentName,
    ) {
        putIntentExtras(
            intent,
            checkoutConfiguration,
            checkoutSession.sessionSetupResponse.paymentMethodsApiResponse ?: PaymentMethodsApiResponse(),
            service,
        )
        intent.apply {
            putExtra(SESSION_KEY, checkoutSession.mapToDetails())
        }
    }

    fun assertBundleExists(bundle: Bundle?): Boolean {
        return when {
            bundle == null -> {
                adyenLog(AdyenLogLevel.ERROR) { "Failed to initialize - bundle is null" }
                false
            }

            !bundle.containsKey(DROP_IN_SERVICE_KEY) || !bundle.containsKey(CHECKOUT_CONFIGURATION_KEY) -> {
                adyenLog(AdyenLogLevel.ERROR) { "Failed to initialize - bundle does not have the required keys" }
                false
            }

            else -> true
        }
    }
}

private const val PAYMENT_METHODS_RESPONSE_KEY = "PAYMENT_METHODS_RESPONSE_KEY"
private const val SESSION_KEY = "SESSION_KEY"
private const val IS_SESSIONS_FLOW_TAKEN_OVER_KEY = "IS_SESSIONS_FLOW_TAKEN_OVER_KEY"
private const val CHECKOUT_CONFIGURATION_KEY = "CHECKOUT_CONFIGURATION_KEY"
private const val DROP_IN_SERVICE_KEY = "DROP_IN_SERVICE_KEY"
private const val IS_WAITING_FOR_RESULT_KEY = "IS_WAITING_FOR_RESULT_KEY"
private const val CACHED_GIFT_CARD = "CACHED_GIFT_CARD"
private const val CURRENT_ORDER = "CURRENT_ORDER"
private const val PARTIAL_PAYMENT_AMOUNT = "PARTIAL_PAYMENT_AMOUNT"

/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/1/2023.
 */

package com.adyen.checkout.sessions.interactor

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.payments.response.BalanceResult
import com.adyen.checkout.components.model.payments.response.OrderResponse
import com.adyen.checkout.components.model.payments.response.Action as ActionResponse

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface SessionCallResult {

    sealed class Payments : SessionCallResult {
        data class Finished(val resultCode: String) : Payments()
        data class NotFullyPaidOrder(val order: OrderResponse?) : Payments()
        data class Action(val action: ActionResponse) : Payments()
        data class Error(val throwable: Throwable) : Payments()
        object TakenOver : Payments()
    }

    sealed class Details : SessionCallResult {
        data class Finished(val resultCode: String) : Details()
        data class Action(val action: ActionResponse) : Details()
        data class Error(val throwable: Throwable) : Details()
        object TakenOver : Details()
    }

    sealed class Balance : SessionCallResult {
        data class Successful(val balanceResult: BalanceResult) : Balance()
        data class Error(val throwable: Throwable) : Balance()
        object TakenOver : Balance()
    }

    sealed class CreateOrder : SessionCallResult {
        data class Successful(val order: OrderResponse) : CreateOrder()
        data class Error(val throwable: Throwable) : CreateOrder()
        object TakenOver : CreateOrder()
    }

    sealed class CancelOrder : SessionCallResult {
        object Successful : CancelOrder()
        data class Error(val throwable: Throwable) : CancelOrder()
        object TakenOver : CancelOrder()
    }

    sealed class UpdatePaymentMethods : SessionCallResult {
        data class Successful(
            val paymentMethods: PaymentMethodsApiResponse,
            val order: OrderResponse?,
        ) : UpdatePaymentMethods()

        data class Error(val throwable: Throwable) : UpdatePaymentMethods()
    }
}

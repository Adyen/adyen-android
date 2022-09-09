/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 8/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import kotlinx.coroutines.flow.Flow

interface OnlineBankingDelegate<IssuerListPaymentMethodT : IssuerListPaymentMethod> :
    PaymentMethodDelegate<
        OnlineBankingConfiguration,
        OnlineBankingInputData,
        OnlineBankingOutputData,
        PaymentComponentState<IssuerListPaymentMethodT>> {

    val outputDataFlow: Flow<OnlineBankingOutputData?>
    val componentStateFlow: Flow<PaymentComponentState<IssuerListPaymentMethodT>?>

    fun getIssuers(): List<OnlineBankingModel>
    fun getTermsAndConditionsUrl(): String
}

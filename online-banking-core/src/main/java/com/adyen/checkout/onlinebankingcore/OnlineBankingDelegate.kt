/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 8/9/2022.
 */

package com.adyen.checkout.onlinebankingcore

import android.content.Context
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.UiStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.flow.Flow

interface OnlineBankingDelegate<IssuerListPaymentMethodT : IssuerListPaymentMethod> :
    PaymentComponentDelegate<PaymentComponentState<IssuerListPaymentMethodT>>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UiStateDelegate {

    val outputData: OnlineBankingOutputData

    val outputDataFlow: Flow<OnlineBankingOutputData>

    val componentStateFlow: Flow<PaymentComponentState<IssuerListPaymentMethodT>>

    val exceptionFlow: Flow<CheckoutException>

    fun getIssuers(): List<OnlineBankingModel>

    fun openTermsAndConditions(context: Context)

    fun updateInputData(update: OnlineBankingInputData.() -> Unit)
}

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 8/9/2022.
 */

package com.adyen.checkout.onlinebankingcore.internal.ui

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.UIStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.onlinebankingcore.internal.ui.model.OnlineBankingInputData
import com.adyen.checkout.onlinebankingcore.internal.ui.model.OnlineBankingModel
import com.adyen.checkout.onlinebankingcore.internal.ui.model.OnlineBankingOutputData
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface OnlineBankingDelegate<IssuerListPaymentMethodT : IssuerListPaymentMethod> :
    PaymentComponentDelegate<PaymentComponentState<IssuerListPaymentMethodT>>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    val outputData: OnlineBankingOutputData

    val outputDataFlow: Flow<OnlineBankingOutputData>

    val componentStateFlow: Flow<PaymentComponentState<IssuerListPaymentMethodT>>

    val exceptionFlow: Flow<CheckoutException>

    fun getIssuers(): List<OnlineBankingModel>

    fun openTermsAndConditions(context: Context)

    fun updateInputData(update: OnlineBankingInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}

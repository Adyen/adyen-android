/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/8/2022.
 */

package com.adyen.checkout.onlinebankingcz

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.IssuerListDelegate

class OnlineBankingCZComponent(
    savedStateHandle: SavedStateHandle,
    issuerListDelegate: IssuerListDelegate<OnlineBankingCZPaymentMethod>,
    configuration: OnlineBankingCZConfiguration
) : IssuerListComponent<OnlineBankingCZPaymentMethod>(
    savedStateHandle,
    issuerListDelegate,
    configuration
) {

    private var termsAndConditionsUrl: String? = null

    init {
        termsAndConditionsUrl = (issuerListDelegate as OnlineBankingCZDelegate).getTermsAndConditionsUrl()
    }

    fun getTermsAndConditionsUrl(): String? = termsAndConditionsUrl

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<OnlineBankingCZComponent, OnlineBankingCZConfiguration> =
            OnlineBankingCZComponentProvider()
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.ONLINE_BANKING_CZ)
    }
}

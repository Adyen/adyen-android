/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.BacsDirectDebitPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.util.PaymentMethodTypes

private val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.BACS)

class BacsDirectDebitComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: BacsDirectDebitConfiguration
) :
    BasePaymentComponent<BacsDirectDebitConfiguration, BacsDirectDebitInputData, BacsDirectDebitOutputData,
        GenericComponentState<BacsDirectDebitPaymentMethod>>(savedStateHandle, paymentMethodDelegate, configuration) {

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

    override fun onInputDataChanged(inputData: BacsDirectDebitInputData): BacsDirectDebitOutputData {
        // TODO
        return BacsDirectDebitOutputData(
            FieldState("", Validation.Valid),
            FieldState("", Validation.Valid),
            FieldState("", Validation.Valid),
            FieldState("", Validation.Valid)
        )
    }

    override fun createComponentState(): GenericComponentState<BacsDirectDebitPaymentMethod> {
        // TODO
        return GenericComponentState(
            PaymentComponentData(),
            true,
            true
        )
    }

    companion object {
        @JvmStatic
        val PROVIDER: PaymentComponentProvider<BacsDirectDebitComponent, BacsDirectDebitConfiguration> =
            GenericPaymentComponentProvider(BacsDirectDebitComponent::class.java)
    }
}

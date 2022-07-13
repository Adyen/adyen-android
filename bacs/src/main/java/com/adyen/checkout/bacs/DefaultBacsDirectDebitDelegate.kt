/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/7/2022.
 */

package com.adyen.checkout.bacs

import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.BacsDirectDebitPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultBacsDirectDebitDelegate(
    val paymentMethod: PaymentMethod,
) : BacsDirectDebitDelegate {

    private val _outputDataFlow = MutableStateFlow<BacsDirectDebitOutputData?>(null)
    override val outputDataFlow: Flow<BacsDirectDebitOutputData?> = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow<BacsDirectDebitComponentState?>(null)
    override val componentStateFlow: Flow<BacsDirectDebitComponentState?> = _componentStateFlow

    override fun getPaymentMethodType(): String = paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    override fun onInputDataChanged(inputData: BacsDirectDebitInputData) {
        val outputData = BacsDirectDebitOutputData(
            holderNameState = BacsDirectDebitValidationUtils.validateHolderName(inputData.holderName),
            bankAccountNumberState = BacsDirectDebitValidationUtils
                .validateBankAccountNumber(inputData.bankAccountNumber),
            sortCodeState = BacsDirectDebitValidationUtils.validateSortCode(inputData.sortCode),
            shopperEmailState = BacsDirectDebitValidationUtils.validateShopperEmail(inputData.shopperEmail),
            isAmountConsentChecked = inputData.isAmountConsentChecked,
            isAccountConsentChecked = inputData.isAccountConsentChecked,
            mode = inputData.mode,
        )

        _outputDataFlow.tryEmit(outputData)

        createComponentState(outputData)
    }

    override fun createComponentState(outputData: BacsDirectDebitOutputData) {
        val bacsDirectDebitPaymentMethod = BacsDirectDebitPaymentMethod(
            type = BacsDirectDebitPaymentMethod.PAYMENT_METHOD_TYPE,
            holderName = outputData.holderNameState.value,
            bankAccountNumber = outputData.bankAccountNumberState.value,
            bankLocationId = outputData.sortCodeState.value,
        )

        val paymentComponentData = PaymentComponentData(
            shopperEmail = outputData.shopperEmailState.value,
            paymentMethod = bacsDirectDebitPaymentMethod,
        )

        val componentState = BacsDirectDebitComponentState(
            paymentComponentData = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true,
            mode = outputData.mode
        )

        _componentStateFlow.tryEmit(componentState)
    }
}

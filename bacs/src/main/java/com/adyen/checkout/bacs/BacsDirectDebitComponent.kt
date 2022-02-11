/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.BacsDirectDebitPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes

class BacsDirectDebitComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: BacsDirectDebitConfiguration
) :
    BasePaymentComponent<BacsDirectDebitConfiguration, BacsDirectDebitInputData, BacsDirectDebitOutputData,
        BacsDirectDebitComponentState>(savedStateHandle, paymentMethodDelegate, configuration) {

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

    override fun onInputDataChanged(inputData: BacsDirectDebitInputData): BacsDirectDebitOutputData {
        return BacsDirectDebitOutputData(
            holderNameState = BacsDirectDebitValidationUtils.validateHolderName(inputData.holderName),
            bankAccountNumberState = BacsDirectDebitValidationUtils.validateBankAccountNumber(inputData.bankAccountNumber),
            sortCodeState = BacsDirectDebitValidationUtils.validateSortCode(inputData.sortCode),
            shopperEmailState = BacsDirectDebitValidationUtils.validateShopperEmail(inputData.shopperEmail),
            isAmountConsentChecked = inputData.isAmountConsentChecked,
            isAccountConsentChecked = inputData.isAccountConsentChecked
        )
    }

    override fun createComponentState(): BacsDirectDebitComponentState {
        val paymentComponentData = PaymentComponentData<BacsDirectDebitPaymentMethod>()
        val bacsDirectDebitPaymentMethod = BacsDirectDebitPaymentMethod().apply {
            type = BacsDirectDebitPaymentMethod.PAYMENT_METHOD_TYPE
            holderName = outputData?.holderNameState?.value
            bankAccountNumber = outputData?.bankAccountNumberState?.value
            bankLocationId = outputData?.sortCodeState?.value
        }

        paymentComponentData.apply {
            shopperEmail = outputData?.shopperEmailState?.value
            paymentMethod = bacsDirectDebitPaymentMethod
        }

        return BacsDirectDebitComponentState(
            paymentComponentData = paymentComponentData,
            isInputValid = outputData?.isValid ?: false,
            isReady = true,
            mode = mLatestInputData?.mode ?: BacsDirectDebitMode.INPUT
        )
    }

    fun setInputMode() {
        mLatestInputData?.mode = BacsDirectDebitMode.INPUT
        notifyStateChanged()
    }

    fun setConfirmationMode() {
        mLatestInputData?.mode = BacsDirectDebitMode.CONFIRMATION
        notifyStateChanged()
    }

    companion object {
        @JvmStatic
        val PROVIDER: PaymentComponentProvider<BacsDirectDebitComponent, BacsDirectDebitConfiguration> =
            GenericPaymentComponentProvider(BacsDirectDebitComponent::class.java)
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.BACS)
    }
}

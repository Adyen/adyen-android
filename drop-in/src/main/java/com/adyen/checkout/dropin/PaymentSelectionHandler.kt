package com.adyen.checkout.dropin

import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.model.payments.request.GenericPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.ui.DropInViewModel
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.adyen.checkout.googlepay.GooglePayComponent

private val TAG = LogUtil.getTag()

class PaymentSelectionHandler(
    private val dropInViewModel: DropInViewModel,
    private val protocol: DropInBottomSheetDialogFragment.Protocol
) {

    fun handlePaymentSelection(paymentMethodType: String) {
        when {
            GooglePayComponent.PAYMENT_METHOD_TYPES.contains(paymentMethodType) -> {
                Logger.d(TAG, "onPaymentMethodSelected: starting Google Pay")
                protocol.startGooglePay(
                    dropInViewModel.getPaymentMethod(paymentMethodType),
                    dropInViewModel.dropInConfiguration.getConfigurationForPaymentMethod(paymentMethodType)
                )
            }
            PaymentMethodTypes.SUPPORTED_ACTION_ONLY_PAYMENT_METHODS.contains(paymentMethodType) -> {
                Logger.d(TAG, "onPaymentMethodSelected: payment method does not need a component, sending payment")
                sendPayment(paymentMethodType)
            }
            PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(paymentMethodType) -> {
                Logger.d(TAG, "onPaymentMethodSelected: payment method is supported")
                protocol.showComponentDialog(dropInViewModel.getPaymentMethod(paymentMethodType))
            }
            else -> {
                Logger.d(TAG, "onPaymentMethodSelected: unidentified payment method, sending payment in case of redirect")
                sendPayment(paymentMethodType)
            }
        }
    }

    private fun sendPayment(type: String) {
        val paymentComponentData = PaymentComponentData<PaymentMethodDetails>()
        paymentComponentData.paymentMethod = GenericPaymentMethod(type)
        val paymentComponentState = GenericComponentState(paymentComponentData, true, true)
        protocol.requestPaymentsCall(paymentComponentState)
    }
}
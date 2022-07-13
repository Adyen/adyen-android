/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */
package com.adyen.checkout.googlepay

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.base.ActivityResultHandlingComponent
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.GooglePayPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger.d
import com.adyen.checkout.googlepay.model.GooglePayParams
import com.adyen.checkout.googlepay.util.GooglePayUtils
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.Wallet

class GooglePayComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: GooglePayConfiguration
) : BasePaymentComponent<GooglePayConfiguration, GooglePayInputData, GooglePayOutputData, GooglePayComponentState>(
        savedStateHandle,
        paymentMethodDelegate,
        configuration
    ),
    ActivityResultHandlingComponent {

    override var inputData: GooglePayInputData = GooglePayInputData(null)

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

    override fun onInputDataChanged(inputData: GooglePayInputData) {
        notifyOutputDataChanged(
            GooglePayOutputData(
                inputData.paymentData ?: throw CheckoutException("paymentData is null")
            )
        )
        createComponentState()
    }

    private fun createComponentState() {
        val outputData = this.outputData ?: throw CheckoutException("outputData is null")
        val paymentMethodType = paymentMethod.type
        val paymentComponentData = PaymentComponentData<GooglePayPaymentMethod>()
        val paymentMethod = GooglePayUtils.createGooglePayPaymentMethod(outputData.paymentData, paymentMethodType)
        paymentComponentData.paymentMethod = paymentMethod
        notifyStateChanged(
            GooglePayComponentState(
                paymentComponentData,
                outputData.isValid,
                true,
                outputData.paymentData
            )
        )
    }

    /**
     * Start the GooglePay screen which will return the result to the provided Activity.
     *
     * @param activity    The activity to start the screen and later receive the result.
     * @param requestCode The code that will be returned on the [Activity.onActivityResult]
     */
    fun startGooglePayScreen(activity: Activity, requestCode: Int) {
        d(TAG, "startGooglePayScreen")
        val googlePayParams = googlePayParams
        val paymentsClient = Wallet.getPaymentsClient(activity, GooglePayUtils.createWalletOptions(googlePayParams))
        val paymentDataRequest = GooglePayUtils.createPaymentDataRequest(googlePayParams)
        // TODO this forces us to use the deprecated onActivityResult. Look into alternatives when/if Google provides any later.
        AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(paymentDataRequest), activity, requestCode)
    }

    private val googlePayParams: GooglePayParams
        get() {
            val configuration = paymentMethod.configuration
            val serverGatewayMerchantId = configuration?.gatewayMerchantId
            return GooglePayParams(this.configuration, serverGatewayMerchantId, paymentMethod.brands)
        }
    private val paymentMethod: PaymentMethod
        get() = (paymentMethodDelegate as GenericPaymentMethodDelegate).paymentMethod

    /**
     * Handle the result from the GooglePay screen that was started by [.startGooglePayScreen].
     *
     * @param resultCode The result code from the [Activity.onActivityResult]
     * @param data       The data intent from the [Activity.onActivityResult]
     */
    override fun handleActivityResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                if (data == null) {
                    notifyException(ComponentException("Result data is null"))
                    return
                }
                val paymentData = PaymentData.getFromIntent(data)
                inputData = GooglePayInputData(paymentData)
                notifyInputDataChanged()
            }
            Activity.RESULT_CANCELED -> notifyException(ComponentException("Payment canceled."))
            AutoResolveHelper.RESULT_ERROR -> {
                val status = AutoResolveHelper.getStatusFromIntent(data)
                var errorMessage = "GooglePay returned an error"
                if (status != null) {
                    errorMessage = errorMessage + ": " + status.statusMessage
                }
                notifyException(ComponentException(errorMessage))
            }
            else -> {}
        }
    }

    companion object {
        private val TAG = getTag()

        @JvmField
        val PROVIDER = GooglePayComponentProvider()

        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.GOOGLE_PAY, PaymentMethodTypes.GOOGLE_PAY_LEGACY)
    }
}

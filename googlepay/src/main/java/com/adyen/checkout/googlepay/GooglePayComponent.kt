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
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.ActivityResultHandlingComponent
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger.d
import com.adyen.checkout.googlepay.GooglePayComponent.Companion.PROVIDER
import com.adyen.checkout.googlepay.util.GooglePayUtils
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.Wallet
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class GooglePayComponent(
    savedStateHandle: SavedStateHandle,
    private val googlePayDelegate: GooglePayDelegate,
    configuration: GooglePayConfiguration
) : BasePaymentComponent<GooglePayConfiguration, GooglePayInputData, GooglePayOutputData, GooglePayComponentState>(
        savedStateHandle,
        googlePayDelegate,
        configuration
    ),
    ActivityResultHandlingComponent {

    override val inputData: GooglePayInputData = GooglePayInputData()

    init {
        googlePayDelegate.outputDataFlow
            .filterNotNull()
            .onEach { notifyOutputDataChanged(it) }
            .launchIn(viewModelScope)

        googlePayDelegate.componentStateFlow
            .filterNotNull()
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)
    }

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

    override fun onInputDataChanged(inputData: GooglePayInputData) {
        googlePayDelegate.onInputDataChanged(inputData)
    }

    /**
     * Start the GooglePay screen which will return the result to the provided Activity.
     *
     * @param activity    The activity to start the screen and later receive the result.
     * @param requestCode The code that will be returned on the [Activity.onActivityResult]
     */
    fun startGooglePayScreen(activity: Activity, requestCode: Int) {
        d(TAG, "startGooglePayScreen")
        val googlePayParams = googlePayDelegate.getGooglePayParams()
        val paymentsClient = Wallet.getPaymentsClient(activity, GooglePayUtils.createWalletOptions(googlePayParams))
        val paymentDataRequest = GooglePayUtils.createPaymentDataRequest(googlePayParams)
        // TODO this forces us to use the deprecated onActivityResult. Look into alternatives when/if Google provides any later.
        AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(paymentDataRequest), activity, requestCode)
    }

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
                inputData.paymentData = paymentData
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
            else -> Unit
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: PaymentComponentProvider<GooglePayComponent, GooglePayConfiguration> =
            GooglePayComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.GOOGLE_PAY, PaymentMethodTypes.GOOGLE_PAY_LEGACY)
    }
}

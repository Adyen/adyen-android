/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/8/2019.
 */

package com.adyen.checkout.dropin.ui.base

import androidx.lifecycle.Observer
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.base.ComponentError
import com.adyen.checkout.base.PaymentComponent
import com.adyen.checkout.base.PaymentComponentState
import com.adyen.checkout.base.component.Configuration
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.getComponentFor

open abstract class BaseComponentDialogFragment : DropInBottomSheetDialogFragment(), Observer<PaymentComponentState<in PaymentMethodDetails>> {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    lateinit var paymentMethod: PaymentMethod
    lateinit var component: PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration>
    lateinit var dropInConfiguration: DropInConfiguration

    open class BaseCompanion<T : BaseComponentDialogFragment>(private var classes: Class<T>) {

        companion object {
            const val PAYMENT_METHOD = "PAYMENT_METHOD"
            const val WAS_IN_EXPAND_STATUS = "WAS_IN_EXPAND_STATUS"
            const val DROP_IN_CONFIGURATION = "DROP_IN_CONFIGURATION"
        }

        fun newInstance(
            paymentMethod: PaymentMethod,
            dropInConfiguration: DropInConfiguration,
            wasInExpandStatus: Boolean
        ): T {
            val args = Bundle()
            args.putParcelable(PAYMENT_METHOD, paymentMethod)
            args.putBoolean(WAS_IN_EXPAND_STATUS, wasInExpandStatus)
            args.putParcelable(DROP_IN_CONFIGURATION, dropInConfiguration)

            var dialogFragment = classes.newInstance()
            dialogFragment.arguments = args
            return dialogFragment
        }
    }

    abstract override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?

    abstract override fun onViewCreated(view: View, savedInstanceState: Bundle?)

    abstract override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentMethod = arguments?.getParcelable(BaseCompanion.PAYMENT_METHOD) ?: throw IllegalArgumentException("Payment method is null")
        dropInConfiguration = arguments?.getParcelable(BaseCompanion.DROP_IN_CONFIGURATION)
            ?: throw IllegalArgumentException("DropIn Configuration is null")

        try {
            component = getComponentFor(this, paymentMethod, dropInConfiguration)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
            return
        }
    }

    override fun onBackPressed(): Boolean {
        Logger.d(TAG, "onBackPressed")
        protocol.showPaymentMethodsDialog(arguments?.getBoolean(BaseCompanion.WAS_IN_EXPAND_STATUS, false)!!)
        return true
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    fun startPayment() {
        val componentState = component.state
        try {
            if (componentState != null) {
                if (componentState.isValid) {
                    protocol.requestPaymentsCall(componentState.data)
                } else {
                    throw CheckoutException("PaymentComponentState are not valid.")
                }
            } else {
                throw CheckoutException("PaymentComponentState are null.")
            }
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    protected fun createErrorHandlerObserver(): Observer<ComponentError> {
        return Observer {
            if (it != null) {
                handleError(it)
            }
        }
    }

    fun handleError(componentError: ComponentError) {
        Logger.e(TAG, componentError.errorMessage)
        protocol.showError(getString(R.string.component_error), true)
    }
}

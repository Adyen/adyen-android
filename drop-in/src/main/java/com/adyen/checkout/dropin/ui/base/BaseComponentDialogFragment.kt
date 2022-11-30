/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/8/2019.
 */

package com.adyen.checkout.dropin.ui.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.getComponentFor
import com.adyen.checkout.dropin.ui.viewmodel.ComponentDialogViewModel

private const val STORED_PAYMENT_METHOD = "STORED_PAYMENT_METHOD"
private const val NAVIGATED_FROM_PRESELECTED = "NAVIGATED_FROM_PRESELECTED"
private const val PAYMENT_METHOD = "PAYMENT_METHOD"

@Suppress("TooManyFunctions")
internal abstract class BaseComponentDialogFragment : DropInBottomSheetDialogFragment() {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    protected val componentDialogViewModel: ComponentDialogViewModel by viewModels()

    var paymentMethod: PaymentMethod = PaymentMethod()
    var storedPaymentMethod: StoredPaymentMethod = StoredPaymentMethod()
    lateinit var component: PaymentComponent<*>
    private var isStoredPayment = false
    private var navigatedFromPreselected = false

    open class BaseCompanion<T : BaseComponentDialogFragment>(private var classes: Class<T>) {

        fun newInstance(
            paymentMethod: PaymentMethod
        ): T {
            val args = Bundle()
            args.putParcelable(PAYMENT_METHOD, paymentMethod)

            val dialogFragment = classes.newInstance()
            dialogFragment.arguments = args
            return dialogFragment
        }

        fun newInstance(
            storedPaymentMethod: StoredPaymentMethod,
            navigatedFromPreselected: Boolean
        ): T {
            val args = Bundle()
            args.putParcelable(STORED_PAYMENT_METHOD, storedPaymentMethod)
            args.putBoolean(NAVIGATED_FROM_PRESELECTED, navigatedFromPreselected)

            val dialogFragment = classes.newInstance()
            dialogFragment.arguments = args
            return dialogFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            storedPaymentMethod = it.getParcelable(STORED_PAYMENT_METHOD) ?: storedPaymentMethod
            paymentMethod = it.getParcelable(PAYMENT_METHOD) ?: paymentMethod
            isStoredPayment = !storedPaymentMethod.type.isNullOrEmpty()
            navigatedFromPreselected = it.getBoolean(NAVIGATED_FROM_PRESELECTED, false)
        }

        try {
            component = if (isStoredPayment) {
                getComponentFor(this, storedPaymentMethod, dropInViewModel.dropInConfiguration, dropInViewModel.amount)
            } else {
                getComponentFor(this, paymentMethod, dropInViewModel.dropInConfiguration, dropInViewModel.amount)
            }
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
            return
        }
    }

    abstract override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?

    override fun onBackPressed(): Boolean {
        Logger.d(TAG, "onBackPressed - $navigatedFromPreselected")

        when {
            navigatedFromPreselected -> protocol.showPreselectedDialog()
            dropInViewModel.shouldSkipToSinglePaymentMethod() -> protocol.terminateDropIn()
            else -> protocol.showPaymentMethodsDialog()
        }
        return true
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    protected fun startPayment(componentState: PaymentComponentState<out PaymentMethodDetails>) {
        try {
            if (componentState.isValid) {
                requestProtocolCall(componentState)
            } else {
                throw CheckoutException("PaymentComponentState are not valid.")
            }
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    open fun requestProtocolCall(componentState: PaymentComponentState<*>) {
        protocol.requestPaymentsCall(componentState)
    }

    protected fun onComponentError(componentError: ComponentError) {
        Logger.e(TAG, "ComponentError", componentError.exception)
        handleError(componentError)
    }

    fun handleError(componentError: ComponentError) {
        Logger.e(TAG, componentError.errorMessage)
        protocol.showError(getString(R.string.component_error), componentError.errorMessage, true)
    }
}

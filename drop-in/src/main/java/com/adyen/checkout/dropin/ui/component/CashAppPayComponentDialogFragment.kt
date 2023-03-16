/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2023.
 */

package com.adyen.checkout.dropin.ui.component

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.CashAppPayPaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.getComponentFor
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment

@Suppress("TooManyFunctions")
class CashAppPayComponentDialogFragment : DropInBottomSheetDialogFragment(), Observer<GenericComponentState<CashAppPayPaymentMethod>> {

    companion object {
        private val TAG = LogUtil.getTag()

        private const val PAYMENT_METHOD = "PAYMENT_METHOD"

        fun newInstance(
            paymentMethod: PaymentMethod
        ): CashAppPayComponentDialogFragment {
            val args = Bundle()
            args.putParcelable(PAYMENT_METHOD, paymentMethod)

            return CashAppPayComponentDialogFragment().apply {
                arguments = args
            }
        }
    }

    private lateinit var paymentMethod: PaymentMethod
    private lateinit var component: CashAppPayComponent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_cash_app_pay_component, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")

        arguments?.let {
            paymentMethod = it.getParcelable(PAYMENT_METHOD) ?: throw IllegalArgumentException("Payment method is null")
        }

        try {
            component = getComponentFor(this, paymentMethod, dropInViewModel.dropInConfiguration, dropInViewModel.amount) as CashAppPayComponent
            component.observe(viewLifecycleOwner, this)
            component.observeErrors(viewLifecycleOwner, createErrorHandlerObserver())
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
            return
        } catch (e: ClassCastException) {
            throw CheckoutException("Component is not CashAppPayComponent")
        }
    }

    override fun onBackPressed(): Boolean {
        Logger.d(TAG, "onBackPressed")
        return performBackAction()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    private fun createErrorHandlerObserver(): Observer<ComponentError> {
        return Observer {
            if (it != null) {
                Logger.e(TAG, "ComponentError", it.exception)
                handleError(it)
            }
        }
    }

    private fun handleError(componentError: ComponentError) {
        Logger.e(TAG, componentError.errorMessage)
        protocol.showError(getString(R.string.component_error), componentError.errorMessage, true)
    }

    private fun performBackAction(): Boolean {
        when {
            dropInViewModel.shouldSkipToSinglePaymentMethod() -> protocol.terminateDropIn()
            else -> protocol.showPaymentMethodsDialog()
        }
        return true
    }

    override fun onChanged(state: GenericComponentState<CashAppPayPaymentMethod>?) {
        if (state?.isValid == true) {
            protocol.requestPaymentsCall(state)
        }
    }
}

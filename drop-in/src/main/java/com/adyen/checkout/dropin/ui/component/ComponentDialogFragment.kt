/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.component

import android.arch.lifecycle.Observer
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.adyen.checkout.base.ComponentError
import com.adyen.checkout.base.ComponentView
import com.adyen.checkout.base.PaymentComponent
import com.adyen.checkout.base.PaymentComponentState
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exeption.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.getComponentFor
import com.adyen.checkout.dropin.getViewFor
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragmentdialog_component.componentContainer
import kotlinx.android.synthetic.main.fragmentdialog_component.payButton
import kotlinx.android.synthetic.main.fragmentdialog_header.view.header

class ComponentDialogFragment : DropInBottomSheetDialogFragment(), Observer<PaymentComponentState<in PaymentMethodDetails>> {

    companion object {
        private val TAG = LogUtil.getTag()

        private const val PAYMENT_METHOD = "PAYMENT_METHOD"
        private const val WAS_IN_EXPAND_STATUS = "WAS_IN_EXPAND_STATUS"
        private const val DROP_IN_CONFIGURATION = "DROP_IN_CONFIGURATION"

        fun newInstance(paymentMethod: PaymentMethod, dropInConfiguration: DropInConfiguration, wasInExpandStatus: Boolean): ComponentDialogFragment {
            val args = Bundle()
            args.putParcelable(PAYMENT_METHOD, paymentMethod)
            args.putBoolean(WAS_IN_EXPAND_STATUS, wasInExpandStatus)
            args.putParcelable(DROP_IN_CONFIGURATION, dropInConfiguration)

            val componentDialogFragment = ComponentDialogFragment()
            componentDialogFragment.arguments = args

            return componentDialogFragment
        }
    }

    private lateinit var paymentMethod: PaymentMethod
    private lateinit var dropInConfiguration: DropInConfiguration
    private lateinit var component: PaymentComponent
    private lateinit var componentView: ComponentView<PaymentComponent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentMethod = arguments?.getParcelable(PAYMENT_METHOD) ?: throw IllegalArgumentException("Payment method is null")
        dropInConfiguration = arguments?.getParcelable(DROP_IN_CONFIGURATION) ?: throw IllegalArgumentException("DropIn Configuration is null")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragmentdialog_component, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        attachComponent(paymentMethod)
        view.header.setText(R.string.credit_card)
    }

    override fun onBackPressed(): Boolean {
        Logger.d(TAG, "onBackPressed")
        protocol.showPaymentMethodsDialog(arguments?.getBoolean(WAS_IN_EXPAND_STATUS, false)!!)
        return true
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?) {
        if (componentView.isConfirmationRequired) {
            @Suppress("UsePropertyAccessSyntax")
            payButton.isEnabled = paymentComponentState != null && paymentComponentState.isValid()
        } else {
            startPayment()
        }
    }

    private fun attachComponent(paymentMethod: PaymentMethod) {
        try {
            component = getComponentFor(this, paymentMethod, dropInConfiguration)
            componentView = getViewFor(requireContext(), paymentMethod)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
            return
        }

        component.observe(this, this)
        component.observeErrors(this, createErrorHandlerObserver())
        componentContainer.addView(componentView as View)
        componentView.attach(component, this)

        if (componentView.isConfirmationRequired) {
            payButton.setOnClickListener {
                startPayment()
            }

            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            (componentView as View).requestFocus()
        } else {
            payButton.visibility = View.GONE
        }
    }

    private fun startPayment() {
        val componentState = component.state
        try {
            if (componentState != null) {
                if (componentState.isValid) {
                    protocol.sendPaymentRequest(componentState.data)
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

    private fun createErrorHandlerObserver(): Observer<ComponentError> {
        return Observer {
            if (it != null) {
                handleError(it)
            }
        }
    }

    private fun handleError(componentError: ComponentError) {
        Logger.e(TAG, componentError.errorMessage)
        Toast.makeText(context, R.string.component_error, Toast.LENGTH_LONG).show()

        protocol.terminateDropIn()
    }
}

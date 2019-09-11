/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/8/2019.
 */

package com.adyen.checkout.dropin.ui.component

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.base.ComponentError
import com.adyen.checkout.base.ComponentView
import com.adyen.checkout.base.PaymentComponent
import com.adyen.checkout.base.PaymentComponentState
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exeption.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.getViewFor
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import kotlinx.android.synthetic.main.fragmentdialog_component.componentContainer
import kotlinx.android.synthetic.main.fragmentdialog_component.payButton
import kotlinx.android.synthetic.main.fragmentdialog_header.view.header

class GenericComponentDialogFragment : BaseComponentDialogFragment() {

    private lateinit var componentView: ComponentView<PaymentComponent>

    companion object : BaseCompanion<GenericComponentDialogFragment>(GenericComponentDialogFragment::class.java) {
        private val TAG = LogUtil.getTag()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragmentdialog_component, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")
        view.header.text = paymentMethod.name

        payButton.setOnClickListener {
            startPayment()
        }

        try {
            componentView = getViewFor(requireContext(), paymentMethod)
            attachComponent(component, componentView)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?) {
        if (componentView.isConfirmationRequired) {
            @Suppress("UsePropertyAccessSyntax")
            payButton.isEnabled = paymentComponentState != null && paymentComponentState.isValid()
        } else {
            startPayment()
        }
    }

    private fun attachComponent(
        component: PaymentComponent,
        componentView: ComponentView<PaymentComponent>
    ) {
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
}

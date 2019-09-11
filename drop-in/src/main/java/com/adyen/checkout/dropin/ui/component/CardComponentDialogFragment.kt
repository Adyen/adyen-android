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
import com.adyen.checkout.base.PaymentComponentState
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.core.exeption.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import kotlinx.android.synthetic.main.view_card_component_dropin.view.payButton
import kotlinx.android.synthetic.main.fragmentdialog_card_component.dropInCardView
import kotlinx.android.synthetic.main.fragmentdialog_header.view.header

class CardComponentDialogFragment : BaseComponentDialogFragment() {

    companion object : BaseCompanion<CardComponentDialogFragment>(CardComponentDialogFragment::class.java) {
        private val TAG = LogUtil.getTag()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragmentdialog_card_component, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (component !is CardComponent) {
            throw CheckoutException("Component is not card")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")

        component.observe(this, this)
        component.observeErrors(this, createErrorHandlerObserver())

        dropInCardView.attach(component as CardComponent, this)

        dropInCardView.header.setText(R.string.credit_card)

        if (dropInCardView.isConfirmationRequired) {
            dropInCardView.payButton.setOnClickListener {
                startPayment()
            }

            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            dropInCardView.requestFocus()
        } else {
            dropInCardView.payButton.visibility = View.GONE
        }
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?) {
        dropInCardView.payButton.isEnabled = paymentComponentState != null && paymentComponentState.isValid()
    }
}

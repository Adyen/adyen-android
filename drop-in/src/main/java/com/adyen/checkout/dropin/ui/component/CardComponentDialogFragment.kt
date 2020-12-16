/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/8/2019.
 */

package com.adyen.checkout.dropin.ui.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.base.PaymentComponentState
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.base.util.CurrencyUtils
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_card_component.dropInCardView
import kotlinx.android.synthetic.main.view_card_component_dropin.view.header
import kotlinx.android.synthetic.main.view_card_component_dropin.view.payButton

class CardComponentDialogFragment : BaseComponentDialogFragment() {

    companion object : BaseCompanion<CardComponentDialogFragment>(CardComponentDialogFragment::class.java) {
        private val TAG = LogUtil.getTag()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_card_component, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            component as CardComponent
        } catch (e: ClassCastException) {
            throw CheckoutException("Component is not CardComponent")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")

        val cardComponent = component as CardComponent

        if (!dropInConfiguration.amount.isEmpty) {
            val value = CurrencyUtils.formatAmount(dropInConfiguration.amount, dropInConfiguration.shopperLocale)
            dropInCardView.payButton.text = String.format(resources.getString(R.string.pay_button_with_value), value)
        }

        // Keeping generic component to use the observer from the BaseComponentDialogFragment
        component.observe(this, this)
        cardComponent.observeErrors(this, createErrorHandlerObserver())

        // try to get the name from the payment methods response
        dropInCardView.header.text = dropInViewModel.paymentMethodsApiResponse.paymentMethods?.find { it.type == PaymentMethodTypes.SCHEME }?.name

        dropInCardView.attach(cardComponent, this)

        if (dropInCardView.isConfirmationRequired) {
            dropInCardView.payButton.setOnClickListener {
                if (cardComponent.state?.isValid == true) {
                    startPayment()
                } else {
                    dropInCardView.highlightValidationErrors()
                }
            }

            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            dropInCardView.requestFocus()
        } else {
            dropInCardView.payButton.visibility = View.GONE
        }
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?) {
        // nothing, validation is already checked on focus change and button click
    }
}

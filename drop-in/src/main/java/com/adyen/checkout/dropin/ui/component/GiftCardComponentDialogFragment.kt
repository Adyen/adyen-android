/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/9/2021.
 */

package com.adyen.checkout.dropin.ui.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.databinding.FragmentGiftcardComponentBinding
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.google.android.material.bottomsheet.BottomSheetBehavior

class GiftCardComponentDialogFragment : BaseComponentDialogFragment() {

    private var _binding: FragmentGiftcardComponentBinding? = null
    private val binding: FragmentGiftcardComponentBinding get() = requireNotNull(_binding)

    companion object : BaseCompanion<GiftCardComponentDialogFragment>(GiftCardComponentDialogFragment::class.java) {
        private val TAG = LogUtil.getTag()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGiftcardComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setPaymentPendingInitialization(pending: Boolean) {
        if (!binding.giftCardView.isConfirmationRequired) return
        binding.redeemButton.isVisible = !pending
        if (pending) binding.progressBar.show()
        else binding.progressBar.hide()
    }

    override fun highlightValidationErrors() {
        binding.giftCardView.highlightValidationErrors()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")
        binding.header.text = paymentMethod.name

        try {
            attachComponent(component)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?) {
        componentDialogViewModel.componentStateChanged(component.state, binding.giftCardView.isConfirmationRequired)
    }

    private fun attachComponent(
        component: PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration>,
    ) {
        component.observe(viewLifecycleOwner, this)
        component.observeErrors(viewLifecycleOwner, createErrorHandlerObserver())

        binding.giftCardView.attach(component as ViewableComponent<*, *>, viewLifecycleOwner)

        if (binding.giftCardView.isConfirmationRequired) {
            binding.redeemButton.setOnClickListener { componentDialogViewModel.payButtonClicked() }
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            binding.giftCardView.requestFocus()
        } else {
            binding.redeemButton.isVisible = false
        }
    }

    override fun requestProtocolCall(componentState: PaymentComponentState<out PaymentMethodDetails>) {
        if (componentState !is GiftCardComponentState) {
            throw CheckoutException("Unsupported payment method, not a gift card: $componentState")
        }
        protocol.requestBalanceCall(componentState)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

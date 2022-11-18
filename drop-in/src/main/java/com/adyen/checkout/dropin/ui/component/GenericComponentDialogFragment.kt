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
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.databinding.FragmentGenericComponentBinding
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

internal class GenericComponentDialogFragment : BaseComponentDialogFragment() {

    private var _binding: FragmentGenericComponentBinding? = null
    private val binding: FragmentGenericComponentBinding get() = requireNotNull(_binding)

    companion object : BaseCompanion<GenericComponentDialogFragment>(GenericComponentDialogFragment::class.java) {
        private val TAG = LogUtil.getTag()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGenericComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setPaymentPendingInitialization(pending: Boolean) {
        if (!binding.componentView.isConfirmationRequired) return
        binding.componentView.setPaymentPendingInitialization(pending)
        if (pending) binding.progressBar.show() else binding.progressBar.hide()
    }

    override fun highlightValidationErrors() {
        binding.componentView.highlightValidationErrors()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")
        binding.header.text = paymentMethod.name

        if (!dropInViewModel.amount.isEmpty) {
            val value =
                CurrencyUtils.formatAmount(dropInViewModel.amount, dropInViewModel.dropInConfiguration.shopperLocale)
//            binding.payButton.text = String.format(resources.getString(R.string.pay_button_with_value), value)
            // TODO button labels
        }

        try {
            attachComponent(component)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    private fun attachComponent(component: PaymentComponent<*>) {
        if (component is ViewableComponent) {
            binding.componentView.attach(component, viewLifecycleOwner)

            if (binding.componentView.isConfirmationRequired) {
                setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
                binding.componentView.requestFocus()
            }
        }
        component.observe(viewLifecycleOwner, ::onPaymentComponentEvent)
    }

    private fun onPaymentComponentEvent(event: PaymentComponentEvent<*>) {
        when (event) {
            is PaymentComponentEvent.StateChanged -> componentDialogViewModel.componentStateChanged(
                event.state,
                binding.componentView.isConfirmationRequired
            )
            is PaymentComponentEvent.Error -> onComponentError(event.error)
            is PaymentComponentEvent.ActionDetails -> {
                throw IllegalStateException("This event should not be used in drop-in")
            }
            is PaymentComponentEvent.Submit -> {
                componentDialogViewModel.payButtonClicked()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

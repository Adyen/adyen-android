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
import androidx.core.view.isVisible
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.ComponentView
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentGenericComponentBinding
import com.adyen.checkout.dropin.getViewFor
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

class GenericComponentDialogFragment : BaseComponentDialogFragment() {

    private lateinit var componentView: ComponentView<in OutputData, ViewableComponent<*, *, *>>
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
        if (!componentView.isConfirmationRequired) return
        binding.payButton.isVisible = !pending
        if (pending) binding.progressBar.show()
        else binding.progressBar.hide()
    }

    override fun highlightValidationErrors() {
        componentView.highlightValidationErrors()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")
        binding.header.text = paymentMethod.name

        if (!dropInViewModel.amount.isEmpty) {
            val value =
                CurrencyUtils.formatAmount(dropInViewModel.amount, dropInViewModel.dropInConfiguration.shopperLocale)
            binding.payButton.text = String.format(resources.getString(R.string.pay_button_with_value), value)
        }

        try {
            componentView = getViewFor(requireContext(), paymentMethod.type!!)
            attachComponent(component, componentView)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?) {
        componentDialogViewModel.componentStateChanged(component.state, componentView.isConfirmationRequired)
    }

    private fun attachComponent(
        component: PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration>,
        componentView: ComponentView<in OutputData, ViewableComponent<*, *, *>>
    ) {
        component.observe(viewLifecycleOwner, this)
        component.observeErrors(viewLifecycleOwner, createErrorHandlerObserver())
        binding.componentContainer.addView(componentView as View)
        componentView.attach(component as ViewableComponent<*, *, *>, viewLifecycleOwner)

        if (componentView.isConfirmationRequired) {
            binding.payButton.setOnClickListener { componentDialogViewModel.payButtonClicked() }
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            (componentView as View).requestFocus()
        } else {
            binding.payButton.isVisible = false
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/11/2021.
 */

package com.adyen.checkout.dropin.ui.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.bacs.BacsDirectDebitComponentState
import com.adyen.checkout.bacs.BacsDirectDebitConfirmationView
import com.adyen.checkout.bacs.BacsDirectDebitMode
import com.adyen.checkout.bacs.BacsDirectDebitInputView
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.databinding.FragmentBacsDirectDebitComponentBinding
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BacsDirectDebitDialogFragment : BaseComponentDialogFragment() {

    private lateinit var binding: FragmentBacsDirectDebitComponentBinding

    companion object : BaseCompanion<BacsDirectDebitDialogFragment>(BacsDirectDebitDialogFragment::class.java) {
        private val TAG = LogUtil.getTag()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBacsDirectDebitComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")
        binding.header.text = paymentMethod.name

        val bacsDirectDebitComponent = component as BacsDirectDebitComponent

        component.observe(viewLifecycleOwner, this)
        bacsDirectDebitComponent.observeErrors(viewLifecycleOwner, createErrorHandlerObserver())

        val bacsDirectDebitInputView = BacsDirectDebitInputView(requireContext())
        binding.viewContainer.addView(bacsDirectDebitInputView)
        bacsDirectDebitInputView.attach(bacsDirectDebitComponent, viewLifecycleOwner)

        if (bacsDirectDebitInputView.isConfirmationRequired) {
            binding.payButton.setOnClickListener {
                handlePayClick()
            }
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            bacsDirectDebitInputView.requestFocus()
        } else {
            binding.payButton.isVisible = false
        }
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?) {
        val bacsDirectDebitComponent = component as BacsDirectDebitComponent
        val bacsDirectDebitComponentState = paymentComponentState as? BacsDirectDebitComponentState

        if (bacsDirectDebitComponentState != null) {
            when (bacsDirectDebitComponentState.mode) {
                BacsDirectDebitMode.INPUT -> attachInputView()
                BacsDirectDebitMode.CONFIRMATION -> attachConfirmationView()
            }
        }

        componentDialogViewModel.componentStateChanged(bacsDirectDebitComponent.state)
    }

    override fun setPaymentPendingInitialization(pending: Boolean) {
        binding.payButton.isVisible = !pending
        if (pending) binding.progressBar.show()
        else binding.progressBar.hide()
    }

    override fun highlightValidationErrors() {
        binding.viewContainer.children.firstOrNull { it is BacsDirectDebitInputView }?.let {
            (it as BacsDirectDebitInputView).highlightValidationErrors()
        }
    }

    override fun onBackPressed(): Boolean {
        val bacsDirectDebitComponent = component as BacsDirectDebitComponent
        val mode = (bacsDirectDebitComponent.state as? BacsDirectDebitComponentState)?.mode
        val isConfirmationMode = mode == BacsDirectDebitMode.CONFIRMATION
        return if (isConfirmationMode) {
            bacsDirectDebitComponent.handleBackPress()
            true
        } else {
            super.onBackPressed()
        }
    }

    private fun handlePayClick() {
        componentDialogViewModel.payButtonClicked()
        val bacsDirectDebitComponent = component as BacsDirectDebitComponent
        val mode = (bacsDirectDebitComponent.state as? BacsDirectDebitComponentState)?.mode
        val isInputMode = mode == BacsDirectDebitMode.INPUT
        if (isInputMode && bacsDirectDebitComponent.state?.isInputValid == true) {
            bacsDirectDebitComponent.handleContinueClick()
        }
    }

    private fun attachInputView() {
        val bacsDirectDebitComponent = component as BacsDirectDebitComponent
        val isInputViewAttached = binding.viewContainer.children.any { it is BacsDirectDebitInputView }
        if (!isInputViewAttached) {
            val bacsDirectDebitInputView = BacsDirectDebitInputView(requireContext())
            binding.viewContainer.apply {
                removeAllViews()
                addView(bacsDirectDebitInputView)
                bacsDirectDebitInputView.attach(bacsDirectDebitComponent, viewLifecycleOwner)
            }
        }
    }

    private fun attachConfirmationView() {
        val bacsDirectDebitComponent = component as BacsDirectDebitComponent
        val isConfirmationViewAttached = binding.viewContainer.children.any { it is BacsDirectDebitConfirmationView }
        if (!isConfirmationViewAttached) {
            val bacsDirectDebitConfirmationView = BacsDirectDebitConfirmationView(requireContext())
            binding.viewContainer.apply {
                removeAllViews()
                addView(bacsDirectDebitConfirmationView)
                bacsDirectDebitConfirmationView.attach(bacsDirectDebitComponent, viewLifecycleOwner)
            }
        }
    }
}

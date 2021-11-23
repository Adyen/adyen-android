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
import com.adyen.checkout.bacs.*
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.databinding.FragmentBacsDirectDebitComponentBinding
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BacsDirectDebitDialogFragment: BaseComponentDialogFragment() {

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

        val bacsDirectDebitView = BacsDirectDebitView(requireContext())
        binding.viewContainer.addView(bacsDirectDebitView)
        bacsDirectDebitView.attach(bacsDirectDebitComponent, viewLifecycleOwner)

        if (bacsDirectDebitView.isConfirmationRequired) {
            binding.payButton.setOnClickListener {
                handlePayClick()
            }
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            bacsDirectDebitView.requestFocus()
        } else {
            binding.payButton.isVisible = false
        }
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?) {
        val bacsDirectDebitComponent = component as BacsDirectDebitComponent
        val bacsDirectDebitComponentState = paymentComponentState as? BacsDirectDebitComponentState

        if (bacsDirectDebitComponentState != null) {
            when (bacsDirectDebitComponentState.mode) {
                BacsDirectDebitMode.INPUT -> {
                    val isInputViewAttached = binding.viewContainer.children.any { it is BacsDirectDebitView }
                    if (!isInputViewAttached) {
                        val bacsDirectDebitView = BacsDirectDebitView(requireContext())
                        binding.viewContainer.apply {
                            removeAllViews()
                            addView(bacsDirectDebitView)
                            bacsDirectDebitView.attach(bacsDirectDebitComponent, viewLifecycleOwner)
                        }
                    }
                }
                BacsDirectDebitMode.CONFIRMATION -> {
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
        }

        componentDialogViewModel.componentStateChanged(bacsDirectDebitComponent.state)
    }

    override fun setPaymentPendingInitialization(pending: Boolean) {
        binding.payButton.isVisible = !pending
        if (pending) binding.progressBar.show()
        else binding.progressBar.hide()
    }

    override fun highlightValidationErrors() {
        binding.viewContainer.children.firstOrNull { it is BacsDirectDebitView }?.let {
            (it as BacsDirectDebitView).highlightValidationErrors()
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
}
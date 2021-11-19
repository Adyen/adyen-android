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
import androidx.core.view.isVisible
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.bacs.BacsDirectDebitView
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
            binding.payButton.setOnClickListener { componentDialogViewModel.payButtonClicked() }
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            bacsDirectDebitView.requestFocus()
        } else {
            binding.payButton.isVisible = false
        }
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?) {
        TODO("Not yet implemented")
    }

    override fun setPaymentPendingInitialization(pending: Boolean) {
        TODO("Not yet implemented")
    }

    override fun highlightValidationErrors() {
        TODO("Not yet implemented")
    }
}
/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/11/2021.
 */

package com.adyen.checkout.dropin.ui.component

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.bacs.BacsDirectDebitComponentState
import com.adyen.checkout.bacs.BacsDirectDebitMode
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentBacsDirectDebitComponentBinding
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

@Suppress("TooManyFunctions")
class BacsDirectDebitDialogFragment : BaseComponentDialogFragment() {

    private var _binding: FragmentBacsDirectDebitComponentBinding? = null
    private val binding: FragmentBacsDirectDebitComponentBinding get() = requireNotNull(_binding)

    private val bacsDirectDebitComponent: BacsDirectDebitComponent by lazy { component as BacsDirectDebitComponent }

    companion object : BaseCompanion<BacsDirectDebitDialogFragment>(BacsDirectDebitDialogFragment::class.java) {
        private val TAG = LogUtil.getTag()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBacsDirectDebitComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")
        binding.header.text = paymentMethod.name

        bacsDirectDebitComponent.observe(viewLifecycleOwner, ::onPaymentComponentEvent)

        binding.bacsView.attach(bacsDirectDebitComponent, viewLifecycleOwner)

        if (binding.bacsView.isConfirmationRequired) {
            binding.payButton.setOnClickListener {
                handleContinueClick()
            }
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            binding.bacsView.requestFocus()
        } else {
            binding.payButton.isVisible = false
        }
    }

    private fun onPaymentComponentEvent(event: PaymentComponentEvent<BacsDirectDebitComponentState>) {
        when (event) {
            is PaymentComponentEvent.StateChanged -> componentStateChanged(event.state)
            is PaymentComponentEvent.Error -> onComponentError(event.error)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Logger.d(TAG, "onCreateDialog")
        val dialog = super.onCreateDialog(savedInstanceState)
        setDialogToFullScreen(dialog)
        return dialog
    }

    private fun componentStateChanged(bacsDirectDebitComponentState: BacsDirectDebitComponentState) {
        val payButtonText = when (bacsDirectDebitComponentState.mode) {
            BacsDirectDebitMode.INPUT -> R.string.bacs_continue
            BacsDirectDebitMode.CONFIRMATION -> R.string.bacs_confirm_and_pay
        }
        binding.payButton.setText(payButtonText)

        componentDialogViewModel.componentStateChanged(
            bacsDirectDebitComponentState,
            binding.bacsView.isConfirmationRequired
        )
    }

    override fun setPaymentPendingInitialization(pending: Boolean) {
        binding.payButton.isVisible = !pending
        if (pending) binding.progressBar.show()
        else binding.progressBar.hide()
    }

    override fun highlightValidationErrors() {
        binding.bacsView.highlightValidationErrors()
    }

    override fun onBackPressed(): Boolean {
        val componentState = getComponentState()
        val mode = componentState?.mode
        val isConfirmationMode = mode == BacsDirectDebitMode.CONFIRMATION
        return if (isConfirmationMode) {
            bacsDirectDebitComponent.setInputMode()
            true
        } else {
            super.onBackPressed()
        }
    }

    private fun handleContinueClick() {
        componentDialogViewModel.payButtonClicked()
        val componentState = getComponentState()
        val mode = componentState?.mode
        val isInputMode = mode == BacsDirectDebitMode.INPUT
        if (isInputMode && componentState?.isInputValid == true) {
            bacsDirectDebitComponent.setConfirmationMode()
        }
    }

    private fun getComponentState(): BacsDirectDebitComponentState? {
        return componentDialogViewModel.componentState as? BacsDirectDebitComponentState
    }

    // TODO improve
    private fun setDialogToFullScreen(dialog: Dialog) {
        dialog.setOnShowListener {
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val layoutParams = bottomSheet?.layoutParams
            val behavior = bottomSheet?.let { BottomSheetBehavior.from(it) }
            behavior?.isDraggable = false
            layoutParams?.height = WindowManager.LayoutParams.MATCH_PARENT
            bottomSheet?.layoutParams = layoutParams
            behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

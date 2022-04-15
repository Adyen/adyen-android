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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.bacs.BacsDirectDebitComponentState
import com.adyen.checkout.bacs.BacsDirectDebitConfirmationView
import com.adyen.checkout.bacs.BacsDirectDebitInputView
import com.adyen.checkout.bacs.BacsDirectDebitMode
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
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

        val bacsDirectDebitComponent = component as BacsDirectDebitComponent

        component.observe(viewLifecycleOwner, this)
        bacsDirectDebitComponent.observeErrors(viewLifecycleOwner, createErrorHandlerObserver())

        val bacsView = when (bacsDirectDebitComponent.state?.mode) {
            BacsDirectDebitMode.CONFIRMATION -> BacsDirectDebitConfirmationView(requireContext())
            else -> BacsDirectDebitInputView(requireContext())
        }

        binding.viewContainer.addView(bacsView)
        bacsView.attach(bacsDirectDebitComponent, viewLifecycleOwner)

        if (bacsView.isConfirmationRequired) {
            binding.payButton.setOnClickListener {
                handleContinueClick()
            }
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            bacsView.requestFocus()
        } else {
            binding.payButton.isVisible = false
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Logger.d(TAG, "onCreateDialog")
        val dialog = super.onCreateDialog(savedInstanceState)
        setDialogToFullScreen(dialog)
        return dialog
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?) {
        val bacsDirectDebitComponent = component as BacsDirectDebitComponent
        val bacsDirectDebitComponentState = paymentComponentState as? BacsDirectDebitComponentState

        if (bacsDirectDebitComponentState != null) {
            val payButtonText = when (bacsDirectDebitComponentState.mode) {
                BacsDirectDebitMode.INPUT -> R.string.bacs_continue
                BacsDirectDebitMode.CONFIRMATION -> R.string.bacs_confirm_and_pay
            }
            binding.payButton.setText(payButtonText)
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
        val mode = bacsDirectDebitComponent.state?.mode
        val isConfirmationMode = mode == BacsDirectDebitMode.CONFIRMATION
        return if (isConfirmationMode) {
            attachInputView()
            true
        } else {
            super.onBackPressed()
        }
    }

    private fun handleContinueClick() {
        componentDialogViewModel.payButtonClicked()
        val bacsDirectDebitComponent = component as BacsDirectDebitComponent
        val mode = bacsDirectDebitComponent.state?.mode
        val isInputMode = mode == BacsDirectDebitMode.INPUT
        if (isInputMode && bacsDirectDebitComponent.state?.isInputValid == true) {
            attachConfirmationView()
        }
    }

    private fun attachInputView() {
        val bacsDirectDebitComponent = component as BacsDirectDebitComponent
        val isInputViewAttached = binding.viewContainer.children.any { it is BacsDirectDebitInputView }
        if (!isInputViewAttached) {
            val inputView = BacsDirectDebitInputView(requireContext())
            val confirmationRemoveAnimation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left_to_right)
            val inputAddAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_left_to_right)
            binding.viewContainer.apply {
                val confirmationView = children.firstOrNull { it is BacsDirectDebitConfirmationView }
                confirmationRemoveAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        // no ops
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        removeView(confirmationView)
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                        // no ops
                    }
                })
                addView(inputView)
                confirmationView?.startAnimation(confirmationRemoveAnimation)
                inputView.startAnimation(inputAddAnimation)
                inputView.attach(bacsDirectDebitComponent, viewLifecycleOwner)
            }
        }
    }

    private fun attachConfirmationView() {
        val bacsDirectDebitComponent = component as BacsDirectDebitComponent
        val isConfirmationViewAttached = binding.viewContainer.children.any { it is BacsDirectDebitConfirmationView }
        if (!isConfirmationViewAttached) {
            val confirmationView = BacsDirectDebitConfirmationView(requireContext())
            val confirmationAddAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right_to_left)
            val inputRemoveAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_right_to_left)
            binding.viewContainer.apply {
                val inputView = children.firstOrNull { it is BacsDirectDebitInputView }
                inputRemoveAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        // no ops
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        removeView(inputView)
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                        // no ops
                    }
                })
                addView(confirmationView)
                inputView?.startAnimation(inputRemoveAnimation)
                confirmationView.startAnimation(confirmationAddAnimation)
                confirmationView.attach(bacsDirectDebitComponent, viewLifecycleOwner)
            }
        }
    }

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

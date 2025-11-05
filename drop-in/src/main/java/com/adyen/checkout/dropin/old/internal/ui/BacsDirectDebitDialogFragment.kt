/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/11/2021.
 */

package com.adyen.checkout.dropin.old.internal.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.dropin.databinding.FragmentBacsDirectDebitComponentBinding
import com.adyen.checkout.ui.core.old.internal.util.requestFocusOnNextLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.R as MaterialR

internal class BacsDirectDebitDialogFragment : BaseComponentDialogFragment() {

    private var _binding: FragmentBacsDirectDebitComponentBinding? = null
    private val binding: FragmentBacsDirectDebitComponentBinding get() = requireNotNull(_binding)

    private val bacsDirectDebitComponent: BacsDirectDebitComponent by lazy { component as BacsDirectDebitComponent }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBacsDirectDebitComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adyenLog(AdyenLogLevel.DEBUG) { "onViewCreated" }

        initToolbar()

        binding.bacsView.attach(bacsDirectDebitComponent, viewLifecycleOwner)

        if (bacsDirectDebitComponent.isConfirmationRequired()) {
            binding.bacsView.requestFocusOnNextLayout()
        }
    }

    private fun initToolbar() = with(binding.bottomSheetToolbar) {
        setTitle(paymentMethod.name)
        setOnButtonClickListener {
            onBackPressed()
        }
        setMode(toolbarMode)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        adyenLog(AdyenLogLevel.DEBUG) { "onCreateDialog" }
        val dialog = super.onCreateDialog(savedInstanceState)
        setDialogToFullScreen(dialog)
        return dialog
    }

    override fun onBackPressed() = if (bacsDirectDebitComponent.handleBackPress()) {
        true
    } else {
        super.onBackPressed()
    }

    private fun setDialogToFullScreen(dialog: Dialog) {
        dialog.setOnShowListener {
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<FrameLayout>(MaterialR.id.design_bottom_sheet)

            bottomSheet?.let {
                val layoutParams = it.layoutParams
                layoutParams?.height = WindowManager.LayoutParams.MATCH_PARENT
                it.layoutParams = layoutParams

                BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    isHideable = false
                    isDraggable = false
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object : BaseCompanion<BacsDirectDebitDialogFragment>(BacsDirectDebitDialogFragment::class.java)
}

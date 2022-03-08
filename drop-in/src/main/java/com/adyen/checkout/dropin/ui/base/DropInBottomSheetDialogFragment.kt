/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 4/7/2019.
 */

package com.adyen.checkout.dropin.ui.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.viewmodel.DropInViewModel
import com.adyen.checkout.dropin.ui.viewmodel.DropInViewModelFactory
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

private val TAG = LogUtil.getTag()

abstract class DropInBottomSheetDialogFragment : BottomSheetDialogFragment() {

    lateinit var protocol: Protocol

    private var dialogInitViewState: Int = BottomSheetBehavior.STATE_COLLAPSED
    protected val dropInViewModel: DropInViewModel by activityViewModels { DropInViewModelFactory(requireActivity()) }

    fun setInitViewState(firstViewState: Int) {
        this.dialogInitViewState = firstViewState
    }

    override fun getTheme(): Int = R.style.AdyenCheckout_BottomSheetDialogTheme

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (activity is Protocol) {
            protocol = activity as Protocol
        } else {
            throw IllegalArgumentException("Host activity needs to implement DropInBottomSheetDialogFragment.Protocol")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                onBackPressed()
            } else {
                false
            }
        }

        dialog.setOnShowListener {
            val bottomSheet = (dialog as BottomSheetDialog).findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)

                if (this.dialogInitViewState == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.skipCollapsed = true
                }
                behavior.state = this.dialogInitViewState
            } else {
                Logger.e(TAG, "Failed to set BottomSheetBehavior.")
            }
        }

        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    open fun onBackPressed(): Boolean {
        return false
    }

    /**
     * Interface for Drop-in fragments to interact with the main Activity
     */
    @Suppress("TooManyFunctions")
    interface Protocol {
        fun showPreselectedDialog()
        fun showPaymentMethodsDialog()
        fun showStoredComponentDialog(storedPaymentMethod: StoredPaymentMethod, fromPreselected: Boolean)
        fun showComponentDialog(paymentMethod: PaymentMethod)
        fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>)
        fun requestDetailsCall(actionComponentData: ActionComponentData)
        fun showError(errorMessage: String, reason: String, terminate: Boolean)
        fun terminateDropIn()
        fun requestBalanceCall(giftCardComponentState: GiftCardComponentState)
        fun requestPartialPayment()
        fun requestOrderCancellation()
        fun finishWithAction()
        fun removeStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod)
    }
}

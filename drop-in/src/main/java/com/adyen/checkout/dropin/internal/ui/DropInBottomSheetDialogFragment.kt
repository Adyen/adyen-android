/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 4/7/2019.
 */

package com.adyen.checkout.dropin.internal.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

internal abstract class DropInBottomSheetDialogFragment : BottomSheetDialogFragment() {

    lateinit var protocol: Protocol

    private var dialogInitViewState: Int = BottomSheetBehavior.STATE_COLLAPSED
    protected val dropInViewModel: DropInViewModel by activityViewModels { DropInViewModelFactory(requireActivity()) }

    fun setInitViewState(firstViewState: Int) {
        this.dialogInitViewState = firstViewState
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        require(activity is Protocol)
        protocol = activity as Protocol
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
            val bottomSheet = (dialog as BottomSheetDialog).findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet,
            )

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

    override fun onCancel(dialog: DialogInterface) {
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    companion object {
        private val TAG = LogUtil.getTag()
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
        fun showError(dialogTitle: String?, errorMessage: String, reason: String, terminate: Boolean)
        fun terminateDropIn()
        fun requestBalanceCall(giftCardComponentState: GiftCardComponentState)
        fun requestPartialPayment()
        fun requestOrderCancellation()
        fun finishWithAction()
        fun removeStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod)
        fun onRedirect()
        fun onBinValue(binValue: String)
        fun onBinLookup(data: List<BinLookupData>)
        fun onAddressLookupQuery(query: String)
        fun onAddressLookupCompletion(lookupAddress: LookupAddress): Boolean
    }
}

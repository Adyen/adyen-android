/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2023.
 */

package com.adyen.checkout.dropin.ui.component

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentCashAppPayComponentBinding
import com.adyen.checkout.dropin.getComponentFor
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

@Suppress("TooManyFunctions")
class CashAppPayComponentDialogFragment : DropInBottomSheetDialogFragment() {

    private lateinit var binding: FragmentCashAppPayComponentBinding

    private lateinit var paymentMethod: PaymentMethod
    private lateinit var cashAppPayComponent: CashAppPayComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            paymentMethod = it.getParcelable(PAYMENT_METHOD) ?: throw CheckoutException("Cannot launch fragment without payment method")
        }

        try {
            cashAppPayComponent = getComponentFor(
                this,
                paymentMethod,
                dropInViewModel.dropInConfiguration,
                dropInViewModel.amount
            ) as CashAppPayComponent
        } catch (e: ClassCastException) {
            throw CheckoutException("Cannot load CashAppPayComponent")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCashAppPayComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")

        cashAppPayComponent.observe(viewLifecycleOwner) { state ->
            if (state.isValid) {
                protocol.requestPaymentsCall(state)
            }
        }
        cashAppPayComponent.observeErrors(viewLifecycleOwner) { error ->
            if (error == null) return@observeErrors
            Logger.e(TAG, "ComponentError", error.exception)
            handleError(error)
        }

        binding.header.text = paymentMethod.name

        binding.cashAppPayView.attach(cashAppPayComponent, viewLifecycleOwner)

        if (binding.cashAppPayView.isConfirmationRequired) {
            binding.payButton.setOnClickListener {
                // we need to trigger the Cash App Pay here to start the transaction
                // this is different from other components as the component state only gets created after this method is called and Cash App Pay
                // redirects back to the app
                cashAppPayComponent.submit()
                setPaymentInProgress(true)
            }
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            binding.cashAppPayView.requestFocus()
            setPaymentInProgress(false)
        } else {
            setPaymentInProgress(true)
        }
    }

    private fun setPaymentInProgress(isPaymentInProgress: Boolean) {
        binding.containerComponent.isVisible = !isPaymentInProgress
        binding.containerPaymentInProgress.isVisible = isPaymentInProgress
    }

    private fun handleError(componentError: ComponentError) {
        Logger.e(TAG, componentError.errorMessage)
        protocol.showError(getString(R.string.component_error), componentError.errorMessage, true)
    }

    override fun onBackPressed(): Boolean {
        Logger.d(TAG, "onBackPressed")

        when {
            dropInViewModel.shouldSkipToSinglePaymentMethod() -> protocol.terminateDropIn()
            else -> protocol.showPaymentMethodsDialog()
        }
        return true
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val PAYMENT_METHOD = "PAYMENT_METHOD"

        fun newInstance(
            paymentMethod: PaymentMethod
        ): CashAppPayComponentDialogFragment {
            val args = Bundle().apply {
                putParcelable(PAYMENT_METHOD, paymentMethod)
            }

            return CashAppPayComponentDialogFragment().apply {
                arguments = args
            }
        }
    }
}

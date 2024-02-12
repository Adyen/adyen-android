/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/9/2021.
 */

package com.adyen.checkout.dropin.internal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentGiftcardComponentBinding
import com.adyen.checkout.dropin.internal.provider.getComponentFor
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardComponentCallback
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import com.google.android.material.bottomsheet.BottomSheetBehavior

@Suppress("TooManyFunctions")
internal class GiftCardComponentDialogFragment : DropInBottomSheetDialogFragment(), GiftCardComponentCallback {

    private var _binding: FragmentGiftcardComponentBinding? = null
    private val binding: FragmentGiftcardComponentBinding get() = requireNotNull(_binding)

    private lateinit var paymentMethod: PaymentMethod
    private lateinit var giftCardComponent: GiftCardComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        adyenLog(AdyenLogLevel.DEBUG) { "onCreate" }
        super.onCreate(savedInstanceState)
        arguments?.let {
            paymentMethod = it.getParcelable(PAYMENT_METHOD)
                ?: throw IllegalArgumentException("Payment method is null")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGiftcardComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adyenLog(AdyenLogLevel.DEBUG) { "onViewCreated" }
        binding.header.text = paymentMethod.name

        try {
            loadComponent()
            attachComponent(giftCardComponent)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    @Suppress("SwallowedException")
    private fun loadComponent() {
        try {
            giftCardComponent = getComponentFor(
                fragment = this,
                paymentMethod = paymentMethod,
                checkoutConfiguration = dropInViewModel.checkoutConfiguration,
                amount = dropInViewModel.amount,
                componentCallback = this,
                sessionDetails = dropInViewModel.sessionDetails,
                analyticsRepository = dropInViewModel.analyticsRepository,
                onRedirect = protocol::onRedirect,
            ) as GiftCardComponent
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
            return
        } catch (e: ClassCastException) {
            throw CheckoutException("Component is not GiftCardComponent")
        }
    }

    private fun attachComponent(component: PaymentComponent) {
        if (component !is ViewableComponent) throw CheckoutException("Attached component is not viewable")

        binding.giftCardView.attach(component, viewLifecycleOwner)

        if (giftCardComponent.isConfirmationRequired()) {
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            binding.giftCardView.requestFocus()
        }
    }

    override fun onRequestOrder() {
        adyenLog(AdyenLogLevel.DEBUG) { "onRequestOrder" }
        // no ops
    }

    override fun onBalanceCheck(paymentComponentState: PaymentComponentState<*>) {
        if (paymentComponentState !is GiftCardComponentState) {
            throw CheckoutException("paymentComponentState is not an instance of GiftCardComponentState.")
        }
        adyenLog(AdyenLogLevel.DEBUG) { "onBalanceCheck" }
        protocol.requestBalanceCall(paymentComponentState)
    }

    override fun onSubmit(state: GiftCardComponentState) {
        adyenLog(AdyenLogLevel.DEBUG) { "onSubmit" }
        // no ops
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        adyenLog(AdyenLogLevel.DEBUG) { "onAdditionalDetails" }
        // no ops
    }

    override fun onError(componentError: ComponentError) {
        adyenLog(AdyenLogLevel.DEBUG) { "onError" }
        handleError(componentError)
    }

    private fun handleError(componentError: ComponentError) {
        adyenLog(AdyenLogLevel.ERROR) { componentError.errorMessage }
        protocol.showError(null, getString(R.string.component_error), componentError.errorMessage, true)
    }

    override fun onBackPressed(): Boolean {
        return performBackAction()
    }

    private fun performBackAction(): Boolean {
        when {
            dropInViewModel.shouldSkipToSinglePaymentMethod() -> protocol.terminateDropIn()
            else -> protocol.showPaymentMethodsDialog()
        }
        return true
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val PAYMENT_METHOD = "PAYMENT_METHOD"

        fun newInstance(
            paymentMethod: PaymentMethod
        ): GiftCardComponentDialogFragment {
            val args = Bundle()
            args.putParcelable(PAYMENT_METHOD, paymentMethod)

            return GiftCardComponentDialogFragment().apply {
                arguments = args
            }
        }
    }
}

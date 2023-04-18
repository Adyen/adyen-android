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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.dropin.databinding.FragmentGiftcardComponentBinding
import com.adyen.checkout.dropin.internal.provider.getComponentFor
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardComponentCallback
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class GiftCardComponentDialogFragment : DropInBottomSheetDialogFragment(), GiftCardComponentCallback {

    private val giftCardViewModel: GiftCardViewModel by viewModels()

    private var _binding: FragmentGiftcardComponentBinding? = null
    private val binding: FragmentGiftcardComponentBinding get() = requireNotNull(_binding)

    private lateinit var paymentMethod: PaymentMethod
    private lateinit var giftCardComponent: GiftCardComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
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
        Logger.d(TAG, "onViewCreated")
        binding.header.text = paymentMethod.name

        try {
            loadComponent()
            attachComponent(giftCardComponent)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                giftCardViewModel.eventsFlow.collect { handleEvent(it) }
            }
        }
    }

    private fun handleEvent(giftCardFragmentEvent: GiftCardFragmentEvent) {
        when (giftCardFragmentEvent) {
            is GiftCardFragmentEvent.CheckBalance ->
                protocol.requestBalanceCall(giftCardFragmentEvent.paymentComponentState)
        }
    }

    @Suppress("SwallowedException")
    private fun loadComponent() {
        try {
            giftCardComponent = getComponentFor(
                fragment = this,
                paymentMethod = paymentMethod,
                dropInConfiguration = dropInViewModel.dropInConfiguration,
                amount = dropInViewModel.amount,
                componentCallback = this,
                sessionDetails = dropInViewModel.sessionDetails,
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

    override fun onStateChanged(state: GiftCardComponentState) {
        Logger.d(TAG, "onStateChanged")
        giftCardViewModel.onState(state)
    }

    override fun onRequestOrder() {
        Logger.d(TAG, "onRequestOrder")
        // no ops
    }

    override fun onBalanceCheck(paymentComponentData: PaymentComponentData<GiftCardPaymentMethod>) {
        Logger.d(TAG, "onBalanceCheck")
        giftCardViewModel.onBalanceCheck(paymentComponentData)
    }

    override fun onSubmit(state: GiftCardComponentState) {
        Logger.d(TAG, "onSubmit")
        // no ops
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        Logger.d(TAG, "onAdditionalDetails")
        // no ops
    }

    override fun onError(componentError: ComponentError) {
        Logger.d(TAG, "onError")
        handleError(componentError)
    }

    private fun handleError(componentError: ComponentError) {
        Logger.e(TAG, componentError.errorMessage)
        // TODO find a way to show an error dialog unless the payment is cancelled by the user
        //  then move back to the payment methods screen afterwards
        performBackAction()
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

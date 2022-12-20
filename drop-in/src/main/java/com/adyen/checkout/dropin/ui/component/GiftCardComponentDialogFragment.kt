/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/9/2021.
 */

package com.adyen.checkout.dropin.ui.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.databinding.FragmentGiftcardComponentBinding
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.google.android.material.bottomsheet.BottomSheetBehavior

internal class GiftCardComponentDialogFragment : BaseComponentDialogFragment() {

    private var _binding: FragmentGiftcardComponentBinding? = null
    private val binding: FragmentGiftcardComponentBinding get() = requireNotNull(_binding)

    private val giftCardComponent: GiftCardComponent by lazy { component as GiftCardComponent }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGiftcardComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")
        binding.header.text = paymentMethod.name

        try {
            attachComponent(component)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    private fun attachComponent(component: PaymentComponent<*>) {
        if (component !is ViewableComponent) throw CheckoutException("Attached component is not viewable")
        component.observe(viewLifecycleOwner, ::onPaymentComponentEvent)

        binding.giftCardView.attach(component, viewLifecycleOwner)

        if (giftCardComponent.isConfirmationRequired()) {
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            binding.giftCardView.requestFocus()
        }
    }

    private fun onPaymentComponentEvent(event: PaymentComponentEvent<*>) {
        when (event) {
            is PaymentComponentEvent.StateChanged -> {
                // no ops
            }
            is PaymentComponentEvent.Error -> onComponentError(event.error)
            is PaymentComponentEvent.ActionDetails -> {
                throw IllegalStateException("This event should not be used in drop-in")
            }
            is PaymentComponentEvent.Submit -> startPayment(event.state)
        }
    }

    override fun requestProtocolCall(componentState: PaymentComponentState<*>) {
        if (componentState !is GiftCardComponentState) {
            throw CheckoutException("Unsupported payment method, not a gift card: $componentState")
        }
        protocol.requestBalanceCall(componentState)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object : BaseCompanion<GiftCardComponentDialogFragment>(GiftCardComponentDialogFragment::class.java) {
        private val TAG = LogUtil.getTag()
    }
}

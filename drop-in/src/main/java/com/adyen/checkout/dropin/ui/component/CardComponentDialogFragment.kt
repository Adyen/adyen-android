/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/8/2019.
 */

package com.adyen.checkout.dropin.ui.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.databinding.FragmentCardComponentBinding
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

internal class CardComponentDialogFragment : BaseComponentDialogFragment() {

    private var _binding: FragmentCardComponentBinding? = null
    private val binding: FragmentCardComponentBinding get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCardComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")

        // Keeping generic component to use the observer from the BaseComponentDialogFragment
        component.observe(viewLifecycleOwner, ::onPaymentComponentEvent)

        // try to get the name from the payment methods response
        binding.header.text = dropInViewModel.getPaymentMethods()
            .find { it.type == PaymentMethodTypes.SCHEME }?.name

        binding.cardView.attach(component as CardComponent, viewLifecycleOwner)

        if (binding.cardView.isConfirmationRequired) {
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            binding.cardView.requestFocus()
        }
    }

    private fun onPaymentComponentEvent(event: PaymentComponentEvent<*>) {
        when (event) {
            is PaymentComponentEvent.StateChanged -> componentDialogViewModel.componentStateChanged(
                event.state,
                binding.cardView.isConfirmationRequired
            )
            is PaymentComponentEvent.Error -> onComponentError(event.error)
            is PaymentComponentEvent.ActionDetails -> {
                throw IllegalStateException("This event should not be used in drop-in")
            }
            is PaymentComponentEvent.Submit -> startPayment()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object : BaseCompanion<CardComponentDialogFragment>(CardComponentDialogFragment::class.java) {
        private val TAG = LogUtil.getTag()
    }
}

/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/8/2019.
 */

package com.adyen.checkout.dropin.internal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.dropin.databinding.FragmentCardComponentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

internal class CardComponentDialogFragment : BaseComponentDialogFragment() {

    private var _binding: FragmentCardComponentBinding? = null
    private val binding: FragmentCardComponentBinding get() = requireNotNull(_binding)

    private val cardComponent: CardComponent by lazy { component as CardComponent }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCardComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")

        // try to get the name from the payment methods response
        binding.header.text = dropInViewModel.getPaymentMethods()
            .find { it.type == PaymentMethodTypes.SCHEME }?.name

        cardComponent.setOnBinValueListener(protocol::onBinValue)
        cardComponent.setOnBinLookupListener(protocol::onBinLookup)

        binding.cardView.attach(cardComponent, viewLifecycleOwner)

        if (cardComponent.isConfirmationRequired()) {
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            binding.cardView.requestFocus()
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

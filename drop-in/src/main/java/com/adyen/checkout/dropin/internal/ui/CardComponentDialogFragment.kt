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
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.components.core.AddressLookupCallback
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.dropin.databinding.FragmentCardComponentBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class CardComponentDialogFragment : BaseComponentDialogFragment(), AddressLookupCallback {

    private var _binding: FragmentCardComponentBinding? = null
    private val binding: FragmentCardComponentBinding get() = requireNotNull(_binding)

    private val cardComponent: CardComponent by lazy { component as CardComponent }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCardComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adyenLog(AdyenLogLevel.DEBUG) { "onViewCreated" }

        binding.header.text = if (isStoredPayment) {
            storedPaymentMethod.name
        } else {
            paymentMethod.name
        }

        cardComponent.setOnBinValueListener(protocol::onBinValue)
        cardComponent.setOnBinLookupListener(protocol::onBinLookup)
        cardComponent.setAddressLookupCallback(this)

        binding.cardView.attach(cardComponent, viewLifecycleOwner)

        if (cardComponent.isConfirmationRequired()) {
            binding.cardView.requestFocus()
        }

        dropInViewModel.addressLookupOptionsFlow
            .onEach { cardComponent.updateAddressLookupOptions(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        dropInViewModel.addressLookupCompleteFlow
            .onEach { cardComponent.setAddressLookupResult(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onQueryChanged(query: String) {
        protocol.onAddressLookupQuery(query)
    }

    override fun onLookupCompletion(lookupAddress: LookupAddress): Boolean {
        return protocol.onAddressLookupCompletion(lookupAddress)
    }

    override fun onBackPressed(): Boolean {
        if (cardComponent.handleBackPress()) {
            return true
        }
        return super.onBackPressed()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object : BaseCompanion<CardComponentDialogFragment>(CardComponentDialogFragment::class.java)
}

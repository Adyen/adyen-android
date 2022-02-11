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
import androidx.core.view.isVisible
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardListAdapter
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentCardComponentBinding
import com.adyen.checkout.dropin.ui.base.BaseComponentDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CardComponentDialogFragment : BaseComponentDialogFragment() {

    companion object : BaseCompanion<CardComponentDialogFragment>(CardComponentDialogFragment::class.java) {
        private val TAG = LogUtil.getTag()
    }

    private lateinit var binding: FragmentCardComponentBinding
    private lateinit var cardListAdapter: CardListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCardComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setPaymentPendingInitialization(pending: Boolean) {
        binding.payButton.isVisible = !pending
        if (pending) binding.progressBar.show()
        else binding.progressBar.hide()
    }

    override fun highlightValidationErrors() {
        binding.cardView.highlightValidationErrors()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            component as CardComponent
        } catch (e: ClassCastException) {
            throw CheckoutException("Component is not CardComponent")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")

        val cardComponent = component as CardComponent

        if (!dropInViewModel.amount.isEmpty) {
            val value = CurrencyUtils.formatAmount(dropInViewModel.amount, dropInViewModel.dropInConfiguration.shopperLocale)
            binding.payButton.text = String.format(resources.getString(R.string.pay_button_with_value), value)
        }

        // Keeping generic component to use the observer from the BaseComponentDialogFragment
        component.observe(viewLifecycleOwner, this)
        cardComponent.observeErrors(viewLifecycleOwner, createErrorHandlerObserver())

        // try to get the name from the payment methods response
        binding.header.text =
            dropInViewModel.paymentMethodsApiResponse.paymentMethods?.find { it.type == PaymentMethodTypes.SCHEME }?.name

        binding.cardView.attach(cardComponent, viewLifecycleOwner)

        if (binding.cardView.isConfirmationRequired) {
            binding.payButton.setOnClickListener { componentDialogViewModel.payButtonClicked() }
            setInitViewState(BottomSheetBehavior.STATE_EXPANDED)
            binding.cardView.requestFocus()
        } else {
            binding.payButton.visibility = View.GONE
        }

        val supportedCards = if (cardComponent.isStoredPaymentMethod()) {
            emptyList<CardType>()
        } else {
            cardComponent.configuration.supportedCardTypes
        }
        cardListAdapter = CardListAdapter(
            ImageLoader.getInstance(requireContext(), component.configuration.environment),
            supportedCards
        )
        binding.recyclerViewCardList.adapter = cardListAdapter
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentMethodDetails>?) {
        val cardComponent = component as CardComponent
        val cardComponentState = paymentComponentState as? CardComponentState
        if (cardComponentState?.cardType != null && !cardComponent.isStoredPaymentMethod()) {
            // TODO: 11/01/2021 pass list of cards from Bin Lookup
            cardListAdapter.setFilteredCard(listOf(cardComponentState.cardType))
        } else {
            cardListAdapter.setFilteredCard(emptyList())
        }

        componentDialogViewModel.componentStateChanged(component.state)
    }
}

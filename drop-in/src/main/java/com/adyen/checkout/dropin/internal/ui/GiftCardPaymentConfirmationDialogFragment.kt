/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/10/2021.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.components.core.internal.util.CurrencyUtils
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentGiftCardPaymentConfirmationBinding
import com.adyen.checkout.dropin.internal.ui.model.GiftCardPaymentConfirmationData
import com.adyen.checkout.dropin.internal.ui.model.GiftCardPaymentMethodModel
import com.adyen.checkout.ui.core.R as UICoreR

internal class GiftCardPaymentConfirmationDialogFragment : DropInBottomSheetDialogFragment() {

    private var _binding: FragmentGiftCardPaymentConfirmationBinding? = null
    private val binding: FragmentGiftCardPaymentConfirmationBinding get() = requireNotNull(_binding)

    private val navigationSource: NavigationSource
        get() = when {
            dropInViewModel.shouldSkipToSinglePaymentMethod() -> NavigationSource.NO_SOURCE
            else -> NavigationSource.PAYMENT_METHOD_LIST
        }

    private lateinit var giftCardPaymentConfirmationData: GiftCardPaymentConfirmationData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        giftCardPaymentConfirmationData = arguments?.getParcelable(GIFT_CARD_DATA)
            ?: throw IllegalArgumentException("Gift card data not found")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        adyenLog(AdyenLogLevel.DEBUG) { "onAttach" }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        adyenLog(AdyenLogLevel.DEBUG) { "onCreateView" }
        _binding = FragmentGiftCardPaymentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val amountToPay = CurrencyUtils.formatAmount(
            giftCardPaymentConfirmationData.amountPaid,
            giftCardPaymentConfirmationData.shopperLocale,
        )
        binding.payButton.text = String.format(resources.getString(UICoreR.string.pay_button_with_value), amountToPay)

        val remainingBalance = CurrencyUtils.formatAmount(
            giftCardPaymentConfirmationData.remainingBalance,
            giftCardPaymentConfirmationData.shopperLocale,
        )
        binding.textViewRemainingBalance.text =
            String.format(resources.getString(R.string.checkout_giftcard_remaining_balance_text), remainingBalance)

        binding.changePaymentMethodButton.setOnClickListener {
            performBackAction()
        }

        initToolbar()
        initRecyclerView()

        binding.payButton.setOnClickListener {
            protocol.requestPartialPayment()
        }
    }

    private fun initToolbar() = with(binding.bottomSheetToolbar) {
        setTitle(giftCardPaymentConfirmationData.paymentMethodName)
        setOnButtonClickListener {
            performBackAction()
        }

        val toolbarMode = when (navigationSource) {
            NavigationSource.PAYMENT_METHOD_LIST -> DropInBottomSheetToolbarMode.BACK_BUTTON
            NavigationSource.NO_SOURCE -> DropInBottomSheetToolbarMode.CLOSE_BUTTON
        }
        setMode(toolbarMode)
    }

    private fun initRecyclerView() {
        val alreadyPaidMethods = dropInViewModel.currentOrder?.paymentMethods.orEmpty().map {
            GiftCardPaymentMethodModel(
                imageId = it.type,
                lastFour = it.lastFour,
                amount = it.amount,
                transactionLimit = it.transactionLimit,
                shopperLocale = giftCardPaymentConfirmationData.shopperLocale,
                environment = dropInViewModel.dropInParams.environment,
            )
        }
        val currentPaymentMethod = GiftCardPaymentMethodModel(
            imageId = giftCardPaymentConfirmationData.brand,
            lastFour = giftCardPaymentConfirmationData.lastFourDigits,
            amount = null,
            transactionLimit = null,
            shopperLocale = null,
            environment = dropInViewModel.dropInParams.environment,
        )

        val paymentMethods = alreadyPaidMethods + currentPaymentMethod

        binding.recyclerViewGiftCards.adapter = PaymentMethodAdapter().apply {
            submitList(paymentMethods)
        }
    }

    override fun onBackPressed() = performBackAction()

    private fun performBackAction(): Boolean {
        when (navigationSource) {
            NavigationSource.PAYMENT_METHOD_LIST -> protocol.showPaymentMethodsDialog()
            NavigationSource.NO_SOURCE -> protocol.terminateDropIn()
        }
        return true
    }

    override fun onDestroyView() {
        binding.recyclerViewGiftCards.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val GIFT_CARD_DATA = "GIFT_CARD_DATA"

        @JvmStatic
        fun newInstance(data: GiftCardPaymentConfirmationData) =
            GiftCardPaymentConfirmationDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(GIFT_CARD_DATA, data)
                }
            }
    }

    internal enum class NavigationSource {
        PAYMENT_METHOD_LIST,
        NO_SOURCE,
    }
}

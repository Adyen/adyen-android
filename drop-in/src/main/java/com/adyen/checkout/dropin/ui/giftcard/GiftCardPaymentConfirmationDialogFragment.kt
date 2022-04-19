/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/10/2021.
 */

package com.adyen.checkout.dropin.ui.giftcard

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentGiftCardPaymentConfirmationBinding
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.adyen.checkout.dropin.ui.paymentmethods.GiftCardPaymentMethodModel
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodAdapter

class GiftCardPaymentConfirmationDialogFragment : DropInBottomSheetDialogFragment() {

    private var _binding: FragmentGiftCardPaymentConfirmationBinding? = null
    private val binding: FragmentGiftCardPaymentConfirmationBinding get() = requireNotNull(_binding)

    private lateinit var giftCardPaymentConfirmationData: GiftCardPaymentConfirmationData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        giftCardPaymentConfirmationData = arguments?.getParcelable(GIFT_CARD_DATA)
            ?: throw IllegalArgumentException("Gift card data not found")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Logger.d(TAG, "onAttach")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView")
        _binding = FragmentGiftCardPaymentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val amountToPay = CurrencyUtils.formatAmount(
            giftCardPaymentConfirmationData.amountPaid,
            giftCardPaymentConfirmationData.shopperLocale
        )
        binding.payButton.text = String.format(resources.getString(R.string.pay_button_with_value), amountToPay)

        val remainingBalance = CurrencyUtils.formatAmount(
            giftCardPaymentConfirmationData.remainingBalance,
            giftCardPaymentConfirmationData.shopperLocale
        )
        binding.textViewRemainingBalance.text =
            String.format(resources.getString(R.string.checkout_giftcard_remaining_balance_text), remainingBalance)

        binding.changePaymentMethodButton.setOnClickListener {
            performBackAction()
        }

        initRecyclerView()

        binding.payButton.setOnClickListener {
            protocol.requestPartialPayment()
        }
    }

    private fun initRecyclerView() {
        val alreadyPaidMethods = dropInViewModel.currentOrder?.paymentMethods.orEmpty().map {
            GiftCardPaymentMethodModel(
                imageId = it.type,
                lastFour = it.lastFour,
                amount = it.amount,
                transactionLimit = it.transactionLimit,
                shopperLocale = giftCardPaymentConfirmationData.shopperLocale
            )
        }
        val currentPaymentMethod = GiftCardPaymentMethodModel(
            imageId = giftCardPaymentConfirmationData.brand,
            lastFour = giftCardPaymentConfirmationData.lastFourDigits,
            amount = null,
            transactionLimit = null,
            shopperLocale = null
        )

        val paymentMethods = alreadyPaidMethods + currentPaymentMethod

        val imageLoader = ImageLoader.getInstance(
            requireContext(),
            dropInViewModel.dropInConfiguration.environment
        )

        binding.recyclerViewGiftCards.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewGiftCards.adapter = PaymentMethodAdapter(paymentMethods, imageLoader)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    override fun onBackPressed(): Boolean {
        return performBackAction()
    }

    private fun performBackAction(): Boolean {
        if (dropInViewModel.shouldSkipToSinglePaymentMethod()) {
            protocol.terminateDropIn()
        } else {
            protocol.showPaymentMethodsDialog()
        }
        return true
    }

    override fun onDestroyView() {
        binding.recyclerViewGiftCards.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val GIFT_CARD_DATA = "GIFT_CARD_DATA"

        @JvmStatic
        fun newInstance(data: GiftCardPaymentConfirmationData) =
            GiftCardPaymentConfirmationDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(GIFT_CARD_DATA, data)
                }
            }
    }
}

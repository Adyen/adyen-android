/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.GenericPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.ui.view.AdyenSwipeToRevealLayout
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentPaymentMethodsListBinding
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.adyen.checkout.dropin.ui.getViewModel
import com.adyen.checkout.dropin.ui.viewmodel.PaymentMethodsListViewModel

private val TAG = LogUtil.getTag()

@Suppress("TooManyFunctions")
class PaymentMethodListDialogFragment :
    DropInBottomSheetDialogFragment(),
    PaymentMethodAdapter.OnPaymentMethodSelectedCallback,
    PaymentMethodAdapter.OnStoredPaymentRemovedCallback {

    private var _binding: FragmentPaymentMethodsListBinding? = null
    private val binding: FragmentPaymentMethodsListBinding get() = requireNotNull(_binding)

    private lateinit var paymentMethodsListViewModel: PaymentMethodsListViewModel
    private lateinit var paymentMethodAdapter: PaymentMethodAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Logger.d(TAG, "onAttach")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView")

        paymentMethodsListViewModel = getViewModel {
            PaymentMethodsListViewModel(
                requireActivity().application,
                dropInViewModel.getPaymentMethods(),
                dropInViewModel.getStoredPaymentMethods(),
                dropInViewModel.currentOrder,
                dropInViewModel.dropInConfiguration,
                dropInViewModel.amount
            )
        }

        _binding = FragmentPaymentMethodsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")

        initPaymentMethodsRecyclerView()
        initObservers()
    }

    private fun initPaymentMethodsRecyclerView() {
        val imageLoader = ImageLoader.getInstance(
            requireContext(),
            dropInViewModel.dropInConfiguration.environment
        )

        paymentMethodAdapter = PaymentMethodAdapter(emptyList(), imageLoader) {
            collapseNotUsedUnderlayButtons(binding.recyclerViewPaymentMethods, it)
        }
        paymentMethodAdapter.setPaymentMethodSelectedCallback(this)
        paymentMethodAdapter.setStoredPaymentRemovedCallback(this)

        binding.recyclerViewPaymentMethods.adapter = paymentMethodAdapter
    }

    private fun initObservers() {
        paymentMethodsListViewModel.paymentMethodsLiveData.observe(
            viewLifecycleOwner
        ) { paymentMethods ->
            Logger.d(TAG, "paymentMethods changed")
            if (paymentMethods == null) {
                throw CheckoutException("List of PaymentMethodModel is null.")
            }
            paymentMethodAdapter.updatePaymentMethods(paymentMethods)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewPaymentMethods.adapter = null
        _binding = null
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    override fun onBackPressed(): Boolean {
        if (dropInViewModel.shouldShowPreselectedStored()) {
            protocol.showPreselectedDialog()
        } else {
            protocol.terminateDropIn()
        }
        return true
    }

    override fun onStoredPaymentMethodSelected(storedPaymentMethodModel: StoredPaymentMethodModel) {
        Logger.d(TAG, "onStoredPaymentMethodSelected")
        val storedPaymentMethod = dropInViewModel.getStoredPaymentMethod(storedPaymentMethodModel.id)
        // TODO: 10/12/2020 remove this after we have UI for stored Blik component
        if (storedPaymentMethod.type == PaymentMethodTypes.BLIK) {
            Logger.e(TAG, "Stored Blik is not yet supported in this flow.")
            throw ComponentException("Stored Blik is not yet supported in this flow.")
        }
        protocol.showStoredComponentDialog(storedPaymentMethod, false)
    }

    override fun onPaymentMethodSelected(paymentMethod: PaymentMethodModel) {
        Logger.d(TAG, "onPaymentMethodSelected - ${paymentMethod.type}")

        // Check some specific payment methods that don't need to show a view
        when {
            PaymentMethodTypes.SUPPORTED_ACTION_ONLY_PAYMENT_METHODS.contains(paymentMethod.type) -> {
                Logger.d(TAG, "onPaymentMethodSelected: payment method does not need a component, sending payment")
                sendPayment(paymentMethod.type)
            }
            PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(paymentMethod.type) -> {
                Logger.d(TAG, "onPaymentMethodSelected: payment method is supported")
                protocol.showComponentDialog(paymentMethodsListViewModel.getPaymentMethod(paymentMethod))
            }
            else -> {
                Logger.d(
                    TAG,
                    "onPaymentMethodSelected: unidentified payment method, sending payment in case of redirect"
                )
                sendPayment(paymentMethod.type)
            }
        }
    }

    override fun onHeaderActionSelected(header: PaymentMethodHeader) {
        when (header.type) {
            PaymentMethodHeader.TYPE_GIFT_CARD_HEADER -> showCancelOrderAlert()
        }
    }

    override fun onStoredPaymentMethodRemoved(storedPaymentMethodModel: StoredPaymentMethodModel) {
        val storedPaymentMethod = StoredPaymentMethod().apply {
            id = storedPaymentMethodModel.id
        }
        protocol.removeStoredPaymentMethod(storedPaymentMethod)
    }

    fun removeStoredPaymentMethod(id: String) {
        paymentMethodsListViewModel.removePaymentMethodWithId(id)
    }

    private fun showCancelOrderAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.checkout_giftcard_remove_gift_cards_title)
            .setMessage(R.string.checkout_giftcard_remove_gift_cards_body)
            .setNegativeButton(R.string.checkout_giftcard_remove_gift_cards_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.checkout_giftcard_remove_gift_cards_positive_button) { dialog, _ ->
                dialog.dismiss()
                protocol.requestOrderCancellation()
            }
            .show()
    }

    private fun sendPayment(type: String) {
        val paymentComponentData = PaymentComponentData<PaymentMethodDetails>()
        paymentComponentData.paymentMethod = GenericPaymentMethod(type)
        val paymentComponentState = PaymentComponentState(paymentComponentData, true, true)
        protocol.requestPaymentsCall(paymentComponentState)
    }

    private fun collapseNotUsedUnderlayButtons(recyclerView: RecyclerView, draggedItem: AdyenSwipeToRevealLayout) {
        recyclerView.children.filterIsInstance(AdyenSwipeToRevealLayout::class.java).forEach {
            if (it != draggedItem) {
                it.collapseUnderlay()
            }
        }
    }
}

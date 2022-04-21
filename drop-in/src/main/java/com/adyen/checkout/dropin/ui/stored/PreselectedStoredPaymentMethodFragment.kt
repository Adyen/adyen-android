/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 1/12/2020.
 */

package com.adyen.checkout.dropin.ui.stored

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.components.util.DateUtils
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentStoredPaymentMethodBinding
import com.adyen.checkout.dropin.getComponentFor
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.adyen.checkout.dropin.ui.paymentmethods.GenericStoredModel
import com.adyen.checkout.dropin.ui.paymentmethods.StoredCardModel
import com.adyen.checkout.dropin.ui.viewModelsFactory
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredPaymentViewModel
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredState.AwaitingComponentInitialization
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredState.PaymentError
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredState.RequestPayment
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredState.ShowStoredPaymentDialog

private val TAG = LogUtil.getTag()
private const val STORED_PAYMENT_KEY = "STORED_PAYMENT"

class PreselectedStoredPaymentMethodFragment : DropInBottomSheetDialogFragment() {

    private val storedPaymentViewModel: PreselectedStoredPaymentViewModel by viewModelsFactory {
        PreselectedStoredPaymentViewModel(
            storedPaymentMethod,
            component.requiresInput(),
            dropInViewModel.dropInConfiguration.isRemovingStoredPaymentMethodsEnabled
        )
    }
    private var _binding: FragmentStoredPaymentMethodBinding? = null
    private val binding: FragmentStoredPaymentMethodBinding get() = requireNotNull(_binding)
    private lateinit var storedPaymentMethod: StoredPaymentMethod
    private lateinit var imageLoader: ImageLoader
    private lateinit var component: PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration>

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        observeState()
        super.onActivityCreated(savedInstanceState)
    }

    private fun observeState() {
        storedPaymentViewModel.componentFragmentState.observe(viewLifecycleOwner) {
            Logger.v(TAG, "state: $it")
            setPaymentPendingInitialization(it is AwaitingComponentInitialization)
            when (it) {
                is ShowStoredPaymentDialog -> protocol.showStoredComponentDialog(storedPaymentMethod, true)
                is RequestPayment -> protocol.requestPaymentsCall(it.componentState)
                is PaymentError -> handleError(it.componentError)
                else -> { // do nothing
                }
            }
        }
    }

    private fun setPaymentPendingInitialization(pending: Boolean) {
        binding.payButton.isVisible = !pending
        if (pending) binding.progressBar.show()
        else binding.progressBar.hide()
    }

    private fun handleError(componentError: ComponentError) {
        Logger.e(TAG, componentError.errorMessage)
        protocol.showError(getString(R.string.component_error), componentError.errorMessage, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        storedPaymentMethod = arguments?.getParcelable(STORED_PAYMENT_KEY) ?: StoredPaymentMethod()

        if (storedPaymentMethod.type.isNullOrEmpty()) {
            throw ComponentException("Stored payment method is empty or not found.")
        }

        imageLoader = ImageLoader.getInstance(
            requireContext(),
            dropInViewModel.dropInConfiguration.environment
        )

        component =
            getComponentFor(this, storedPaymentMethod, dropInViewModel.dropInConfiguration, dropInViewModel.amount)
        component.observe(viewLifecycleOwner, storedPaymentViewModel::componentStateChanged)
        component.observeErrors(viewLifecycleOwner, storedPaymentViewModel::componentErrorOccurred)

        _binding = FragmentStoredPaymentMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")
        binding.paymentMethodsListHeader.paymentMethodHeaderTitle.setText(R.string.store_payment_methods_header)
        binding.storedPaymentMethodItem.root.setBackgroundColor(android.R.color.transparent)
        observe()

        if (component.requiresInput()) {
            binding.payButton.setText(R.string.continue_button)
        } else {
            val value = CurrencyUtils.formatAmount(
                dropInViewModel.amount,
                dropInViewModel.dropInConfiguration.shopperLocale
            )
            binding.payButton.text = getString(R.string.pay_button_with_value, value)
        }

        if (dropInViewModel.dropInConfiguration.isRemovingStoredPaymentMethodsEnabled) {
            binding.storedPaymentMethodItem.paymentMethodItemUnderlayButton.setOnClickListener {
                showRemoveStoredPaymentDialog()
            }
        }

        binding.payButton.setOnClickListener {
            storedPaymentViewModel.payButtonClicked()
        }

        binding.changePaymentMethodButton.setOnClickListener {
            protocol.showPaymentMethodsDialog()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    private fun observe() {
        storedPaymentViewModel.storedPaymentLiveData.observe(
            viewLifecycleOwner
        ) {
            binding.storedPaymentMethodItem.swipeToRevealLayout.setDragLocked(!it.isRemovable)
            when (it) {
                is StoredCardModel -> {
                    binding.storedPaymentMethodItem.textViewText.text =
                        requireActivity().getString(R.string.card_number_4digit, it.lastFour)
                    imageLoader.load(it.imageId, binding.storedPaymentMethodItem.imageViewLogo)
                    binding.storedPaymentMethodItem.textViewDetail.text =
                        DateUtils.parseDateToView(it.expiryMonth, it.expiryYear)
                    binding.storedPaymentMethodItem.textViewDetail.isVisible = true
                }
                is GenericStoredModel -> {
                    binding.storedPaymentMethodItem.textViewText.text = it.name
                    binding.storedPaymentMethodItem.textViewDetail.isVisible = false
                    imageLoader.load(it.imageId, binding.storedPaymentMethodItem.imageViewLogo)
                }
            }
        }
    }

    private fun showRemoveStoredPaymentDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.checkout_giftcard_remove_gift_cards_title)
            .setMessage(R.string.checkout_remove_stored_payment_method_body)
            .setPositiveButton(R.string.checkout_giftcard_remove_gift_cards_positive_button) { dialog, _ ->
                val storedPaymentMethod = StoredPaymentMethod().apply {
                    id = storedPaymentMethod.id
                }
                protocol.removeStoredPaymentMethod(storedPaymentMethod)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.checkout_giftcard_remove_gift_cards_negative_button) { dialog, _ ->
                binding.storedPaymentMethodItem.root.collapseUnderlay()
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance(storedPaymentMethod: StoredPaymentMethod) =
            PreselectedStoredPaymentMethodFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(STORED_PAYMENT_KEY, storedPaymentMethod)
                }
            }
    }
}

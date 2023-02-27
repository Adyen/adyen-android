/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 1/12/2020.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.ButtonComponent
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.util.DateUtils
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentStoredPaymentMethodBinding
import com.adyen.checkout.dropin.internal.provider.getComponentFor
import com.adyen.checkout.dropin.internal.ui.model.GenericStoredModel
import com.adyen.checkout.dropin.internal.ui.model.StoredCardModel
import com.adyen.checkout.dropin.internal.ui.model.StoredPaymentMethodModel
import com.adyen.checkout.dropin.internal.util.arguments
import com.adyen.checkout.dropin.internal.util.viewModelsFactory
import com.adyen.checkout.ui.core.internal.ui.loadLogo
import com.adyen.checkout.ui.core.internal.util.PayButtonFormatter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
internal class PreselectedStoredPaymentMethodFragment : DropInBottomSheetDialogFragment() {

    private val storedPaymentViewModel: PreselectedStoredPaymentViewModel by viewModelsFactory {
        PreselectedStoredPaymentViewModel(
            storedPaymentMethod,
            dropInViewModel.amount,
            dropInViewModel.dropInConfiguration,
        )
    }

    private var _binding: FragmentStoredPaymentMethodBinding? = null
    private val binding: FragmentStoredPaymentMethodBinding get() = requireNotNull(_binding)
    private val storedPaymentMethod: StoredPaymentMethod by arguments(STORED_PAYMENT_KEY)
    private lateinit var component: PaymentComponent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (storedPaymentMethod.type.isNullOrEmpty()) {
            throw ComponentException("Stored payment method is empty or not found.")
        }

        _binding = FragmentStoredPaymentMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")

        initView()
        observeState()
        observeEvents()
        loadComponent()
    }

    private fun loadComponent() {
        try {
            component = getComponentFor(
                fragment = this,
                storedPaymentMethod = storedPaymentMethod,
                dropInConfiguration = dropInViewModel.dropInConfiguration,
                amount = dropInViewModel.amount,
                componentCallback = storedPaymentViewModel,
                sessionSetupConfiguration = dropInViewModel.sessionDetails?.sessionSetupConfiguration
            )
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    private fun initView() {
        binding.paymentMethodsListHeader.paymentMethodHeaderTitle.setText(R.string.store_payment_methods_header)

        val isRemovingStoredPaymentMethodsEnabled =
            dropInViewModel.dropInConfiguration.isRemovingStoredPaymentMethodsEnabled
        binding.storedPaymentMethodItem.swipeToRevealLayout.setDragLocked(!isRemovingStoredPaymentMethodsEnabled)
        if (isRemovingStoredPaymentMethodsEnabled) {
            binding.storedPaymentMethodItem.paymentMethodItemUnderlayButton.setOnClickListener {
                showRemoveStoredPaymentDialog()
            }
        }

        binding.payButton.setOnClickListener {
            storedPaymentViewModel.onButtonClicked()
        }

        binding.changePaymentMethodButton.setOnClickListener {
            protocol.showPaymentMethodsDialog()
        }
    }

    private fun observeState() {
        storedPaymentViewModel.uiStateFlow.onEach { state ->
            Logger.v(TAG, "state: $state")
            updateStoredPaymentMethodItem(state.storedPaymentMethodModel)
            updateButtonState(state.buttonState)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun updateStoredPaymentMethodItem(storedPaymentMethodModel: StoredPaymentMethodModel) {
        when (storedPaymentMethodModel) {
            is StoredCardModel -> {
                binding.storedPaymentMethodItem.textViewTitle.text =
                    requireActivity().getString(R.string.card_number_4digit, storedPaymentMethodModel.lastFour)
                binding.storedPaymentMethodItem.imageViewLogo.loadLogo(
                    environment = dropInViewModel.dropInConfiguration.environment,
                    txVariant = storedPaymentMethodModel.imageId,
                )
                binding.storedPaymentMethodItem.textViewDetail.text =
                    DateUtils.parseDateToView(storedPaymentMethodModel.expiryMonth, storedPaymentMethodModel.expiryYear)
                binding.storedPaymentMethodItem.textViewDetail.isVisible = true
            }
            is GenericStoredModel -> {
                binding.storedPaymentMethodItem.textViewTitle.text = storedPaymentMethodModel.name
                binding.storedPaymentMethodItem.textViewDetail.isVisible = false
                binding.storedPaymentMethodItem.imageViewLogo.loadLogo(
                    environment = dropInViewModel.dropInConfiguration.environment,
                    txVariant = storedPaymentMethodModel.imageId,
                )
            }
        }
    }

    private fun updateButtonState(buttonState: ButtonState) {
        setPaymentPendingInitialization(buttonState is ButtonState.Loading)
        when (buttonState) {
            is ButtonState.ContinueButton -> {
                binding.payButton.setText(buttonState.labelResId)
            }
            is ButtonState.PayButton -> {
                binding.payButton.text = PayButtonFormatter.getPayButtonText(
                    amount = buttonState.amount,
                    locale = buttonState.shopperLocale,
                    localizedContext = requireContext(),
                )
            }
            is ButtonState.Loading -> {
                // already handled
            }
        }
    }

    private fun setPaymentPendingInitialization(pending: Boolean) {
        binding.payButton.isVisible = !pending
        if (pending) binding.progressBar.show() else binding.progressBar.hide()
    }

    private fun observeEvents() {
        storedPaymentViewModel.eventsFlow.onEach { event ->
            when (event) {
                is PreselectedStoredEvent.ShowStoredPaymentScreen -> {
                    protocol.showStoredComponentDialog(storedPaymentMethod, true)
                }
                is PreselectedStoredEvent.SubmitComponent -> {
                    (component as? ButtonComponent)?.submit()
                        ?: throw CheckoutException("Component must be of type ButtonComponent.")
                }
                is PreselectedStoredEvent.RequestPaymentsCall -> {
                    protocol.requestPaymentsCall(event.state)
                }
                is PreselectedStoredEvent.ShowError -> {
                    handleError(event.componentError)
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleError(componentError: ComponentError) {
        Logger.e(TAG, componentError.errorMessage)
        protocol.showError(getString(R.string.component_error), componentError.errorMessage, true)
    }

    private fun showRemoveStoredPaymentDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.checkout_giftcard_remove_gift_cards_title)
            .setMessage(R.string.checkout_remove_stored_payment_method_body)
            .setPositiveButton(R.string.checkout_giftcard_remove_gift_cards_positive_button) { dialog, _ ->
                val storedPaymentMethod = StoredPaymentMethod(
                    id = storedPaymentMethod.id
                )
                protocol.removeStoredPaymentMethod(storedPaymentMethod)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.checkout_giftcard_remove_gift_cards_negative_button) { dialog, _ ->
                binding.storedPaymentMethodItem.root.collapseUnderlay()
                dialog.dismiss()
            }
            .show()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val STORED_PAYMENT_KEY = "STORED_PAYMENT"

        @JvmStatic
        fun newInstance(storedPaymentMethod: StoredPaymentMethod) =
            PreselectedStoredPaymentMethodFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(STORED_PAYMENT_KEY, storedPaymentMethod)
                }
            }
    }
}

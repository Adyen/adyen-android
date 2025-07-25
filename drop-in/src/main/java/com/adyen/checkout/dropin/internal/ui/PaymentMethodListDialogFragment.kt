/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentPaymentMethodsListBinding
import com.adyen.checkout.dropin.internal.provider.getComponentFor
import com.adyen.checkout.dropin.internal.ui.model.PaymentMethodHeader
import com.adyen.checkout.dropin.internal.ui.model.PaymentMethodModel
import com.adyen.checkout.dropin.internal.ui.model.StoredPaymentMethodModel
import com.adyen.checkout.dropin.internal.ui.model.mapToStoredPaymentMethodItem
import com.adyen.checkout.ui.core.old.internal.ui.view.AdyenSwipeToRevealLayout
import com.adyen.checkout.ui.core.old.internal.util.PayButtonFormatter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.adyen.checkout.ui.core.R as UICoreR

@Suppress("TooManyFunctions")
internal class PaymentMethodListDialogFragment :
    DropInBottomSheetDialogFragment(),
    PaymentMethodAdapter.OnPaymentMethodSelectedCallback,
    PaymentMethodAdapter.OnStoredPaymentRemovedCallback {

    private var _binding: FragmentPaymentMethodsListBinding? = null
    private val binding: FragmentPaymentMethodsListBinding get() = requireNotNull(_binding)

    private val paymentMethodsListViewModel: PaymentMethodsListViewModel by viewModels {
        viewModelFactory {
            PaymentMethodsListViewModel(
                application = requireActivity().application,
                paymentMethods = dropInViewModel.getPaymentMethods(),
                storedPaymentMethods = dropInViewModel.getStoredPaymentMethods(),
                order = dropInViewModel.currentOrder,
                checkoutConfiguration = dropInViewModel.checkoutConfiguration,
                dropInParams = dropInViewModel.dropInParams,
                dropInOverrideParams = dropInViewModel.getDropInOverrideParams(),
            )
        }
    }

    private var paymentMethodAdapter: PaymentMethodAdapter? = null
    private var component: PaymentComponent? = null

    private val navigationSource: NavigationSource
        get() = when {
            dropInViewModel.shouldShowPreselectedStored() -> NavigationSource.PRESELECTED_PAYMENT_METHOD
            else -> NavigationSource.NO_SOURCE
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        adyenLog(AdyenLogLevel.DEBUG) { "onAttach" }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        adyenLog(AdyenLogLevel.DEBUG) { "onCreateView" }
        _binding = FragmentPaymentMethodsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adyenLog(AdyenLogLevel.DEBUG) { "onViewCreated" }

        initToolbar()
        initPaymentMethodsRecyclerView()
        initObservers()
    }

    private fun initToolbar() {
        binding.bottomSheetToolbar.setOnButtonClickListener {
            performBackAction()
        }
        updateToolbarMode()
        binding.bottomSheetToolbar.setTitle(getString(R.string.payment_methods_header))
    }

    private fun updateToolbarMode() {
        val toolbarMode = when (navigationSource) {
            NavigationSource.PRESELECTED_PAYMENT_METHOD -> DropInBottomSheetToolbarMode.BACK_BUTTON
            NavigationSource.NO_SOURCE -> DropInBottomSheetToolbarMode.CLOSE_BUTTON
        }
        binding.bottomSheetToolbar.setMode(toolbarMode)
    }

    private fun initPaymentMethodsRecyclerView() {
        paymentMethodAdapter = PaymentMethodAdapter(this, this) {
            collapseNotUsedUnderlayButtons(binding.recyclerViewPaymentMethods, it)
        }

        binding.recyclerViewPaymentMethods.adapter = paymentMethodAdapter
    }

    private fun initObservers() {
        paymentMethodsListViewModel
            .paymentMethodsFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { paymentMethods ->
                adyenLog(AdyenLogLevel.DEBUG) { "paymentMethods changed" }
                paymentMethodAdapter?.submitList(paymentMethods)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        paymentMethodsListViewModel
            .eventsFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { event ->
                when (event) {
                    is PaymentMethodListStoredEvent.ShowConfirmationPopup -> {
                        showConfirmationPopup(event.paymentMethodName, event.storedPaymentMethodModel)
                    }

                    is PaymentMethodListStoredEvent.ShowStoredComponentDialog -> {
                        protocol.showStoredComponentDialog(event.storedPaymentMethod, false)
                    }

                    is PaymentMethodListStoredEvent.RequestPaymentsCall -> {
                        protocol.requestPaymentsCall(event.state)
                    }

                    is PaymentMethodListStoredEvent.ShowError -> {
                        adyenLog(AdyenLogLevel.ERROR) { event.componentError.errorMessage }
                        protocol.showError(
                            dialogTitle = null,
                            errorMessage = getString(UICoreR.string.component_error),
                            reason = event.componentError.errorMessage,
                            terminate = true,
                        )
                    }

                    is PaymentMethodListStoredEvent.AdditionalDetails -> {
                        protocol.requestDetailsCall(event.data)
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        paymentMethodAdapter = null
        binding.recyclerViewPaymentMethods.adapter = null
        _binding = null
    }

    override fun onBackPressed() = performBackAction()

    private fun performBackAction(): Boolean {
        when (navigationSource) {
            NavigationSource.PRESELECTED_PAYMENT_METHOD -> protocol.showPreselectedDialog()
            NavigationSource.NO_SOURCE -> protocol.terminateDropIn()
        }
        return true
    }

    override fun onStoredPaymentMethodSelected(storedPaymentMethodModel: StoredPaymentMethodModel) {
        adyenLog(AdyenLogLevel.DEBUG) { "onStoredPaymentMethodSelected" }
        val storedPaymentMethod = dropInViewModel.getStoredPaymentMethod(storedPaymentMethodModel.id)
        component = getComponentFor(
            fragment = this,
            storedPaymentMethod = storedPaymentMethod,
            checkoutConfiguration = dropInViewModel.checkoutConfiguration,
            dropInOverrideParams = dropInViewModel.getDropInOverrideParams(),
            componentCallback = paymentMethodsListViewModel,
            analyticsManager = dropInViewModel.analyticsManager,
            onRedirect = protocol::onRedirect,
        )
        paymentMethodsListViewModel.onClickStoredItem(storedPaymentMethod, storedPaymentMethodModel)
    }

    private fun showConfirmationPopup(paymentMethodName: String, storedPaymentMethodModel: StoredPaymentMethodModel) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(
                String.format(
                    resources.getString(R.string.checkout_stored_payment_confirmation_message),
                    paymentMethodName,
                ),
            )
            .setNegativeButton(R.string.checkout_stored_payment_confirmation_cancel_button) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(
                PayButtonFormatter.getPayButtonText(
                    amount = dropInViewModel.dropInParams.amount,
                    locale = dropInViewModel.dropInParams.shopperLocale,
                    localizedContext = requireContext(),
                ),
            ) { dialog, _ ->
                dialog.dismiss()
                paymentMethodsListViewModel.onClickConfirmationButton()
            }

        val message = storedPaymentMethodModel.mapToStoredPaymentMethodItem(requireContext()).popUpMessage
        dialog.setMessage(message)
        dialog.show()
    }

    override fun onPaymentMethodSelected(paymentMethod: PaymentMethodModel) {
        adyenLog(AdyenLogLevel.DEBUG) { "onPaymentMethodSelected - ${paymentMethod.type}" }

        protocol.showComponentDialog(paymentMethodsListViewModel.getPaymentMethod(paymentMethod))
    }

    override fun onHeaderActionSelected(header: PaymentMethodHeader) {
        when (header.type) {
            PaymentMethodHeader.TYPE_GIFT_CARD_HEADER -> showCancelOrderAlert()
        }
    }

    override fun onStoredPaymentMethodRemoved(storedPaymentMethodModel: StoredPaymentMethodModel) {
        val storedPaymentMethod = StoredPaymentMethod(
            id = storedPaymentMethodModel.id,
        )
        protocol.removeStoredPaymentMethod(storedPaymentMethod)
    }

    fun removeStoredPaymentMethod(id: String) {
        paymentMethodsListViewModel.removePaymentMethodWithId(id)
        updateToolbarMode()
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

    private fun collapseNotUsedUnderlayButtons(recyclerView: RecyclerView, draggedItem: AdyenSwipeToRevealLayout) {
        recyclerView.children.filterIsInstance(AdyenSwipeToRevealLayout::class.java).forEach {
            if (it != draggedItem) {
                it.collapseUnderlay()
            }
        }
    }

    internal enum class NavigationSource {
        PRESELECTED_PAYMENT_METHOD,
        NO_SOURCE,
    }
}

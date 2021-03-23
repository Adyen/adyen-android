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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
import com.adyen.checkout.dropin.ui.stored.PreselectedStoredState.AwaitingComponentInitialization
import com.adyen.checkout.dropin.ui.stored.PreselectedStoredState.RequestPayment
import com.adyen.checkout.dropin.ui.stored.PreselectedStoredState.ShowStoredPaymentDialog
import com.adyen.checkout.dropin.ui.viewModelsFactory

private val TAG = LogUtil.getTag()
private const val STORED_PAYMENT_KEY = "STORED_PAYMENT"

class PreselectedStoredPaymentMethodFragment : DropInBottomSheetDialogFragment() {

    private val storedPaymentViewModel: PreselectedStoredPaymentViewModel by viewModelsFactory {
        PreselectedStoredPaymentViewModel(storedPaymentMethod, component.requiresInput())
    }
    private lateinit var binding: FragmentStoredPaymentMethodBinding
    private lateinit var storedPaymentMethod: StoredPaymentMethod
    private lateinit var imageLoader: ImageLoader
    private lateinit var component: PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration>

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        observeState()
        super.onActivityCreated(savedInstanceState)
    }

    private fun observeState() {
        storedPaymentViewModel.componentFragmentState.observe(viewLifecycleOwner) {
            Log.d(TAG, "state: $it")
            setPaymentPendingInitialization(it is AwaitingComponentInitialization)
            when (it) {
                is ShowStoredPaymentDialog -> protocol.showStoredComponentDialog(storedPaymentMethod, true)
                is RequestPayment -> protocol.requestPaymentsCall(it.componentState)
                else -> { //do nothing
                }
            }
        }
    }

    private fun setPaymentPendingInitialization(pending: Boolean) {
        binding.payButton.isVisible = !pending
        if (pending) binding.progressBar.show()
        else binding.progressBar.hide()
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

        component = getComponentFor(this, storedPaymentMethod, dropInViewModel.dropInConfiguration)
        component.observe(viewLifecycleOwner, storedPaymentViewModel::componentStateChanged)

        binding = FragmentStoredPaymentMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")
        binding.paymentMethodsListHeader.paymentMethodHeader.setText(R.string.store_payment_methods_header)
        binding.storedPaymentMethodItem.root.setBackgroundColor(android.R.color.transparent)
        observe()

        if (component.requiresInput()) {
            binding.payButton.setText(R.string.continue_button)
        } else {
            val value = CurrencyUtils.formatAmount(
                dropInViewModel.dropInConfiguration.amount,
                dropInViewModel.dropInConfiguration.shopperLocale
            )
            binding.payButton.text = getString(R.string.pay_button_with_value, value)
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
            viewLifecycleOwner,
            {
                when (it) {
                    is StoredCardModel -> {
                        binding.storedPaymentMethodItem.textViewText.text = requireActivity().getString(R.string.card_number_4digit, it.lastFour)
                        imageLoader.load(it.imageId, binding.storedPaymentMethodItem.imageViewLogo)
                        binding.storedPaymentMethodItem.textViewDetail.text = DateUtils.parseDateToView(it.expiryMonth, it.expiryYear)
                        binding.storedPaymentMethodItem.textViewDetail.visibility = View.VISIBLE
                    }
                    is GenericStoredModel -> {
                        binding.storedPaymentMethodItem.textViewText.text = it.name
                        binding.storedPaymentMethodItem.textViewDetail.visibility = View.GONE
                        imageLoader.load(it.imageId, binding.storedPaymentMethodItem.imageViewLogo)
                    }
                }
            }
        )
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

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

private val TAG = LogUtil.getTag()
private const val STORED_PAYMENT_KEY = "STORED_PAYMENT"

class PreselectedStoredPaymentMethodFragment : DropInBottomSheetDialogFragment() {

    private val storedPaymentViewModel: PreselectedStoredPaymentViewModel by viewModelsFactory {
        PreselectedStoredPaymentViewModel(storedPaymentMethod)
    }
    private lateinit var binding: FragmentStoredPaymentMethodBinding
    private lateinit var storedPaymentMethod: StoredPaymentMethod
    private lateinit var imageLoader: ImageLoader
    private lateinit var component: PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration>
    private lateinit var componentState: PaymentComponentState<PaymentMethodDetails>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        arguments?.let {
            storedPaymentMethod = it.getParcelable(STORED_PAYMENT_KEY) ?: StoredPaymentMethod()
        }

        if (storedPaymentMethod.type.isNullOrEmpty()) {
            throw ComponentException("Stored payment method is empty or not found.")
        }

        imageLoader = ImageLoader.getInstance(
            requireContext(),
            dropInViewModel.dropInConfiguration.environment
        )

        component = getComponentFor(this, storedPaymentMethod, dropInViewModel.dropInConfiguration)
        if (!component.requiresInput()) {
            component.observe(this) {
                if (it.isValid) {
                    componentState = it
                } else {
                    Logger.e(TAG, "Component state is not valid")
                }
            }
        }

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
            if (component.requiresInput()) {
                protocol.showStoredComponentDialog(storedPaymentMethod, true)
            } else {
                if (this::componentState.isInitialized) {
                    protocol.requestPaymentsCall(componentState)
                } else {
                    Logger.e(TAG, "Component data is not initialized.")
                }
            }
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
            this,
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

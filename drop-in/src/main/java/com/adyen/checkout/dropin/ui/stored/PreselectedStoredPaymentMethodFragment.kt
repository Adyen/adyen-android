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
import androidx.fragment.app.activityViewModels
import com.adyen.checkout.base.PaymentComponent
import com.adyen.checkout.base.PaymentComponentState
import com.adyen.checkout.base.api.ImageLoader
import com.adyen.checkout.base.component.Configuration
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.base.util.DateUtils
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.getComponentFor
import com.adyen.checkout.dropin.ui.DropInViewModel
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.adyen.checkout.dropin.ui.paymentmethods.GenericStoredModel
import com.adyen.checkout.dropin.ui.paymentmethods.StoredCardModel
import com.adyen.checkout.dropin.ui.viewModelsFactory
import kotlinx.android.synthetic.main.fragment_stored_payment_method.change_payment_method_button
import kotlinx.android.synthetic.main.fragment_stored_payment_method.payButton
import kotlinx.android.synthetic.main.fragment_stored_payment_method.stored_payment_method_container
import kotlinx.android.synthetic.main.payment_methods_list_header.payment_method_header
import kotlinx.android.synthetic.main.payment_methods_list_item.imageView_logo
import kotlinx.android.synthetic.main.payment_methods_list_item.textView_detail
import kotlinx.android.synthetic.main.payment_methods_list_item.textView_text

private val TAG = LogUtil.getTag()
private const val STORED_PAYMENT_KEY = "STORED_PAYMENT"

class PreselectedStoredPaymentMethodFragment : DropInBottomSheetDialogFragment() {

    private val dropInViewModel: DropInViewModel by activityViewModels()
    private val storedPaymentViewModel: PreselectedStoredPaymentViewModel by viewModelsFactory {
        PreselectedStoredPaymentViewModel(storedPaymentMethod)
    }
    private lateinit var storedPaymentMethod: StoredPaymentMethod
    private lateinit var imageLoader: ImageLoader
    private lateinit var component: PaymentComponent<PaymentComponentState<in PaymentMethodDetails>, Configuration>
    private lateinit var componentData: PaymentComponentData<PaymentMethodDetails>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    componentData = it.data
                } else {
                    Logger.e(TAG, "Component state is not valid")
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_stored_payment_method, container, false)
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")
        payment_method_header.setText(R.string.store_payment_methods_header)
        stored_payment_method_container.setBackgroundColor(android.R.color.transparent)
        observe()

        payButton.setOnClickListener {
            if (component.requiresInput()) {
                protocol.showStoredComponentDialog(storedPaymentMethod, true)
            } else {
                if (this::componentData.isInitialized) {
                    protocol.requestPaymentsCall(componentData)
                } else {
                    Logger.e(TAG, "Component data is not initialized.")
                }
            }
        }

        change_payment_method_button.setOnClickListener {
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
                        textView_text.text = requireActivity().getString(R.string.card_number_4digit, it.lastFour)
                        imageLoader.load(it.imageId, imageView_logo)
                        textView_detail.text = DateUtils.parseDateToView(it.expiryMonth, it.expiryYear)
                        textView_detail.visibility = View.VISIBLE
                    }
                    is GenericStoredModel -> {
                        textView_text.text = it.name
                        textView_detail.visibility = View.GONE
                        imageLoader.load(it.imageId, imageView_logo)
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

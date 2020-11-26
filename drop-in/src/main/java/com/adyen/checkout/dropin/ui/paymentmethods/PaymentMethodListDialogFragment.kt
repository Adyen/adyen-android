/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.base.api.ImageLoader
import com.adyen.checkout.base.model.payments.request.GenericPaymentMethod
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.DropInViewModel
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment

class PaymentMethodListDialogFragment : DropInBottomSheetDialogFragment(), PaymentMethodAdapter.OnPaymentMethodSelectedCallback {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    private lateinit var mDropInViewModel: DropInViewModel
    private lateinit var paymentMethodAdapter: PaymentMethodAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d(TAG, "onCreateView")
        mDropInViewModel = ViewModelProvider(requireActivity()).get(DropInViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_payment_methods_list, container, false)
        addObserver(view.findViewById(R.id.recyclerView_paymentMethods))
        return view
    }

    private fun addObserver(recyclerView: RecyclerView) {
        mDropInViewModel.paymentMethodsLiveData.observe(
            this,
            {
                Logger.d(TAG, "paymentMethods changed")
                if (it == null) {
                    throw CheckoutException("List of PaymentMethodModel is null.")
                }

                // We expect the list of payment methods to be updated only once, so we just set the adapter
                paymentMethodAdapter = PaymentMethodAdapter(
                    it,
                    ImageLoader.getInstance(
                        requireContext(),
                        mDropInViewModel.dropInConfiguration.environment
                    ),
                    this
                )
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = paymentMethodAdapter
            }
        )
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    override fun onStoredPaymentMethodSelected(storedPaymentMethodModel: StoredPaymentMethodModel) {
        Logger.e(TAG, "Not yet implemented, start stored payment method fragment")
    }

    override fun onPaymentMethodSelected(paymentMethod: PaymentMethodModel) {
        Logger.d(TAG, "onPaymentMethodSelected - ${paymentMethod.type}")

        // Check some specific payment methods that don't need to show a view
        when (paymentMethod.type) {
            PaymentMethodTypes.GOOGLE_PAY -> {
                protocol.startGooglePay(
                    mDropInViewModel.getPaymentMethod(paymentMethod.type),
                    mDropInViewModel.dropInConfiguration.getConfigurationFor(PaymentMethodTypes.GOOGLE_PAY, requireContext())
                )
            }
            PaymentMethodTypes.WECHAT_PAY_SDK -> {
                sendPayment(paymentMethod.type)
            }
            else -> {
                if (PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(paymentMethod.type)) {
                    protocol.showComponentDialog(mDropInViewModel.getPaymentMethod(paymentMethod.type))
                } else {
                    sendPayment(paymentMethod.type)
                }
            }
        }
    }

    private fun sendPayment(type: String) {
        val paymentComponentData = PaymentComponentData<PaymentMethodDetails>()
        paymentComponentData.paymentMethod = GenericPaymentMethod(type)
        protocol.requestPaymentsCall(paymentComponentData)
    }
}

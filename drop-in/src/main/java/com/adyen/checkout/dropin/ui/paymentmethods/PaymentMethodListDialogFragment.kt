/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.base.api.ImageLoader
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
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
        private const val SHOW_IN_EXPAND_STATUS = "SHOW_IN_EXPAND_STATUS"

        fun newInstance(showInExpandStatus: Boolean): PaymentMethodListDialogFragment {
            val args = Bundle()
            args.putBoolean(SHOW_IN_EXPAND_STATUS, showInExpandStatus)

            val componentDialogFragment = PaymentMethodListDialogFragment()
            componentDialogFragment.arguments = args

            return componentDialogFragment
        }
    }

    private lateinit var mPaymentMethodModelList: PaymentMethodsModel
    private lateinit var mDropInViewModel: DropInViewModel
    private lateinit var paymentMethodAdapter: PaymentMethodAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d(TAG, "onCreateView")
        mDropInViewModel = ViewModelProviders.of(requireActivity()).get(DropInViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_payment_methods_list, container, false)
        addObserver(view.findViewById(R.id.recyclerView_paymentMethods))
        return view
    }

    private fun addObserver(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        mDropInViewModel.paymentMethodsModelLiveData.observe(this, Observer<PaymentMethodsModel> {
            Logger.d(TAG, "paymentMethods changed")
            if (it == null) {
                throw CheckoutException("List of PaymentMethodModel is null.")
            }

            // we only expect payment methods to be updated inside the same list, without adding or removing elements
            if (!::mPaymentMethodModelList.isInitialized) {
                mPaymentMethodModelList = it
                paymentMethodAdapter = PaymentMethodAdapter(mPaymentMethodModelList,
                        ImageLoader.getInstance(requireContext(), mDropInViewModel.dropInConfiguration.environment),
                        arguments?.getBoolean(SHOW_IN_EXPAND_STATUS)!!,
                        this)
                recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
                recyclerView.adapter = paymentMethodAdapter
            } else {
                paymentMethodAdapter.updatePaymentMethodsList(it)
                paymentMethodAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    override fun onPaymentMethodSelected(paymentMethod: PaymentMethod, isInExpandMode: Boolean) {
        Logger.d(TAG, "onPaymentMethodSelected - ${paymentMethod.type}")
        paymentMethod.type?.let { paymentMethodType ->
            // Check some specific payment methods that don't need to show a view
            when (paymentMethodType) {
                PaymentMethodTypes.GOOGLE_PAY -> {
                    protocol.startGooglePay(
                            paymentMethod, mDropInViewModel.dropInConfiguration.getConfigurationFor(PaymentMethodTypes.GOOGLE_PAY, requireContext()))
                }
                PaymentMethodTypes.WECHAT_PAY_SDK -> {
                    sendPayment(paymentMethodType)
                }
                else -> {
                    if (PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(paymentMethodType)) {
                        protocol.showComponentDialog(paymentMethod, isInExpandMode)
                    } else {
                        sendPayment(paymentMethodType)
                    }
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

/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.base.api.ImageLoader
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.request.GenericPaymentMethod
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.core.exeption.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.DropInViewModel
import com.adyen.checkout.dropin.ui.LoadingActivity
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragmentdialog_header.view.*

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
        mDropInViewModel = ViewModelProviders.of(activity!!).get(DropInViewModel::class.java)
        val view = inflater.inflate(R.layout.fragmentdialog_paymentmethod_list_dialog, container, false)
        addObserver(view.findViewById(R.id.recyclerView_paymentMethods))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.header.visibility = View.GONE
    }

    private fun addObserver(recyclerView: RecyclerView) {
        mDropInViewModel.paymentMethodsModelLiveData.observe(this, Observer<PaymentMethodsModel> {
            if (it == null) {
                throw CheckoutException("List of PaymentMethodModel is null.")
            }

            // we only expect payment methods to be updated inside the same list, without adding or removing elements
            if (!::mPaymentMethodModelList.isInitialized) {
                mPaymentMethodModelList = it
                paymentMethodAdapter = PaymentMethodAdapter(mPaymentMethodModelList,
                        ImageLoader.getInstance(context!!, DropIn.INSTANCE.configuration.environment),
                        arguments?.getBoolean(SHOW_IN_EXPAND_STATUS)!!,
                        this)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = paymentMethodAdapter
            } else {
                paymentMethodAdapter.updatePaymentMethodsList(it)
            }
        })
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    override fun onPaymentMethodSelected(paymentMethod: PaymentMethod, isInExpandMode: Boolean) {
        Logger.d(TAG, "onPaymentMethodSelected")
        if (PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(paymentMethod.type)) {
            protocol.showComponentDialog(paymentMethod, isInExpandMode)
        } else {
            val paymentComponentData = PaymentComponentData<PaymentMethodDetails>()
            paymentComponentData.paymentMethod = GenericPaymentMethod(paymentMethod.type)
            startActivity(LoadingActivity.getIntentForPayments(context!!, paymentComponentData))
        }
    }
}

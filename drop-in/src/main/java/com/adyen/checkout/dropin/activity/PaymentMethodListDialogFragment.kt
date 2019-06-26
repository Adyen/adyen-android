package com.adyen.checkout.dropin.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.base.model.payments.request.GenericPaymentMethod
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.core.exeption.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R

class PaymentMethodListDialogFragment : BottomSheetDialogFragment(), PaymentMethodAdapter.OnPaymentMethodSelectedCallback {

    companion object {
        private val TAG = LogUtil.getTag()

        fun newInstance(): PaymentMethodListDialogFragment {
            return PaymentMethodListDialogFragment()
        }
    }

    private lateinit var paymentMethodModelList: List<PaymentMethodModel>
    private lateinit var paymentMethodPickerViewModel: PaymentMethodPickerViewModel
    private lateinit var paymentMethodAdapter: PaymentMethodAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d(TAG, "onCreateView")
        paymentMethodPickerViewModel = ViewModelProviders.of(activity!!).get(PaymentMethodPickerViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_paymentmethod_list_dialog, container, false)
        addObserver(view.findViewById(R.id.recyclerView_paymentMethods))
        return view
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        Logger.d(TAG, "onDismiss")
        if (activity is PaymentMethodPickerActivity) {
            (activity as PaymentMethodPickerActivity).onDialogDismissed()
        }
    }

    private fun addObserver(recyclerView: RecyclerView) {
        paymentMethodPickerViewModel.paymentMethodsModelLiveData.observe(this, Observer<List<PaymentMethodModel>> {
            if (it == null) {
                throw CheckoutException("List of PaymentMethodModel is null.")
            }

            // we only expect payment methods to be updated inside the same list, without adding or removing elements
            if (!::paymentMethodModelList.isInitialized) {
                paymentMethodModelList = it
                paymentMethodAdapter = PaymentMethodAdapter(paymentMethodModelList, this)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = paymentMethodAdapter
            } else {
                paymentMethodAdapter.updatePaymentMethodsList(it)
            }
        })
    }

    override fun onPaymentMethodSelected(paymentMethodModel: PaymentMethodModel) {
        Logger.d(TAG, "onPaymentMethodSelected")
        if (PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(paymentMethodModel.paymentMethod.type)) {
            startActivity(ComponentActivity.createIntent(context!!, paymentMethodModel.paymentMethod))
        } else {
            startActivity(LoadingActivity.getIntentForPayments(context!!, GenericPaymentMethod(paymentMethodModel.paymentMethod.type)))
        }
    }
}

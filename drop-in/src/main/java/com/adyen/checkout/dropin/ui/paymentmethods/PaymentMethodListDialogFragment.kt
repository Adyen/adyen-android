/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.model.payments.request.GenericPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.adyen.checkout.dropin.ui.getViewModel
import com.adyen.checkout.googlepay.GooglePayComponent

private val TAG = LogUtil.getTag()

class PaymentMethodListDialogFragment : DropInBottomSheetDialogFragment(), PaymentMethodAdapter.OnPaymentMethodSelectedCallback {

    private lateinit var paymentMethodsListViewModel: PaymentMethodsListViewModel
    private lateinit var paymentMethodAdapter: PaymentMethodAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Logger.d(TAG, "onAttach")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d(TAG, "onCreateView")
        paymentMethodsListViewModel = getViewModel {
            PaymentMethodsListViewModel(
                requireActivity().application,
                dropInViewModel.paymentMethodsApiResponse.paymentMethods.orEmpty(),
                dropInViewModel.paymentMethodsApiResponse.storedPaymentMethods.orEmpty(),
                dropInViewModel.dropInConfiguration
            )
        }
        val view = inflater.inflate(R.layout.fragment_payment_methods_list, container, false)
        addObserver(view.findViewById(R.id.recyclerView_paymentMethods))
        return view
    }

    private fun addObserver(recyclerView: RecyclerView) {
        paymentMethodsListViewModel.paymentMethodsLiveData.observe(
            viewLifecycleOwner,
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
                        dropInViewModel.dropInConfiguration.environment
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

    override fun onBackPressed(): Boolean {
        if (dropInViewModel.showPreselectedStored) {
            protocol.showPreselectedDialog()
        } else {
            protocol.terminateDropIn()
        }
        return true
    }

    override fun onStoredPaymentMethodSelected(storedPaymentMethodModel: StoredPaymentMethodModel) {
        Logger.d(TAG, "onStoredPaymentMethodSelected")
        val storedPaymentMethod = dropInViewModel.getStoredPaymentMethod(storedPaymentMethodModel.id)
        // TODO: 10/12/2020 remove this after we have UI for stored Blik component
        if (storedPaymentMethod.type == PaymentMethodTypes.BLIK) {
            Logger.e(TAG, "Stored Blik is not yet supported in this flow.")
            throw ComponentException("Stored Blik is not yet supported in this flow.")
        }
        protocol.showStoredComponentDialog(storedPaymentMethod, false)
    }

    override fun onPaymentMethodSelected(paymentMethod: PaymentMethodModel) {
        Logger.d(TAG, "onPaymentMethodSelected - ${paymentMethod.type}")

        // Check some specific payment methods that don't need to show a view
        when {
            GooglePayComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type) -> {
                Logger.d(TAG, "onPaymentMethodSelected: starting Google Pay")
                protocol.startGooglePay(
                    paymentMethodsListViewModel.getPaymentMethod(paymentMethod),
                    dropInViewModel.dropInConfiguration.getConfigurationForPaymentMethod(paymentMethod.type)
                )
            }
            PaymentMethodTypes.SUPPORTED_ACTION_ONLY_PAYMENT_METHODS.contains(paymentMethod.type) -> {
                Logger.d(TAG, "onPaymentMethodSelected: payment method does not need a component, sending payment")
                sendPayment(paymentMethod.type)
            }
            PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(paymentMethod.type) -> {
                Logger.d(TAG, "onPaymentMethodSelected: payment method is supported")
                protocol.showComponentDialog(paymentMethodsListViewModel.getPaymentMethod(paymentMethod))
            }
            else -> {
                Logger.d(TAG, "onPaymentMethodSelected: unidentified payment method, sending payment in case of redirect")
                sendPayment(paymentMethod.type)
            }
        }
    }

    private fun sendPayment(type: String) {
        val paymentComponentData = PaymentComponentData<PaymentMethodDetails>()
        paymentComponentData.paymentMethod = GenericPaymentMethod(type)
        val paymentComponentState = GenericComponentState(paymentComponentData, true, true)
        protocol.requestPaymentsCall(paymentComponentState)
    }
}

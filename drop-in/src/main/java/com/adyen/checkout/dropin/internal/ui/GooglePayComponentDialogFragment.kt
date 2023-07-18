/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/1/2022.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.internal.provider.getComponentFor
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentState
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class GooglePayComponentDialogFragment :
    DropInBottomSheetDialogFragment(),
    ComponentCallback<GooglePayComponentState> {

    private val googlePayViewModel: GooglePayViewModel by viewModels()

    private lateinit var paymentMethod: PaymentMethod
    private lateinit var component: GooglePayComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        arguments?.let {
            paymentMethod = it.getParcelable(PAYMENT_METHOD) ?: throw IllegalArgumentException("Payment method is null")
        }

        googlePayViewModel.fragmentLoaded()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_google_pay_component, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.d(TAG, "onViewCreated")

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                googlePayViewModel.eventsFlow.collect { handleEvent(it) }
            }
        }

        loadComponent()
    }

    private fun loadComponent() {
        @Suppress("SwallowedException")
        try {
            component = getComponentFor(
                fragment = this,
                paymentMethod = paymentMethod,
                dropInConfiguration = dropInViewModel.dropInConfiguration,
                amount = dropInViewModel.amount,
                componentCallback = this,
                sessionDetails = dropInViewModel.sessionDetails,
                analyticsRepository = dropInViewModel.analyticsRepository,
                onRedirect = protocol::onRedirect,
            ) as GooglePayComponent
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
            return
        } catch (e: ClassCastException) {
            throw CheckoutException("Component is not GooglePayComponent")
        }
    }

    override fun onSubmit(state: GooglePayComponentState) {
        protocol.requestPaymentsCall(state)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        error("This event should not be used in drop-in")
    }

    override fun onError(componentError: ComponentError) {
        handleError(componentError)
    }

    private fun handleEvent(event: GooglePayFragmentEvent) {
        when (event) {
            is GooglePayFragmentEvent.StartGooglePay -> {
                component.startGooglePayScreen(requireActivity(), GOOGLE_PAY_REQUEST_CODE)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        Logger.d(TAG, "onBackPressed")
        return performBackAction()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        protocol.terminateDropIn()
    }

    private fun handleError(componentError: ComponentError) {
        Logger.e(TAG, componentError.errorMessage)
        // TODO find a way to show an error dialog unless the payment is cancelled by the user
        //  then move back to the payment methods screen afterwards
        performBackAction()
    }

    private fun performBackAction(): Boolean {
        when {
            dropInViewModel.shouldSkipToSinglePaymentMethod() -> protocol.terminateDropIn()
            else -> protocol.showPaymentMethodsDialog()
        }
        return true
    }

    fun handleActivityResult(resultCode: Int, data: Intent?) {
        component.handleActivityResult(resultCode, data)
    }

    companion object {

        private val TAG = LogUtil.getTag()
        private const val PAYMENT_METHOD = "PAYMENT_METHOD"

        fun newInstance(
            paymentMethod: PaymentMethod
        ): GooglePayComponentDialogFragment {
            val args = Bundle()
            args.putParcelable(PAYMENT_METHOD, paymentMethod)

            return GooglePayComponentDialogFragment().apply {
                arguments = args
            }
        }
    }
}

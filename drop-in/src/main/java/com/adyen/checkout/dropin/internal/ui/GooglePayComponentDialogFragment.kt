/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/1/2022.
 */

package com.adyen.checkout.dropin.internal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.dropin.databinding.FragmentGooglePayComponentBinding
import com.adyen.checkout.dropin.internal.provider.getComponentFor
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
internal class GooglePayComponentDialogFragment :
    DropInBottomSheetDialogFragment(),
    ComponentCallback<GooglePayComponentState> {

    private var _binding: FragmentGooglePayComponentBinding? = null
    private val binding: FragmentGooglePayComponentBinding get() = requireNotNull(_binding)

    private val googlePayViewModel: GooglePayViewModel by viewModels()

    private lateinit var paymentMethod: PaymentMethod
    private lateinit var component: GooglePayComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        adyenLog(AdyenLogLevel.DEBUG) { "onCreate" }
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            paymentMethod = it.getParcelable(PAYMENT_METHOD) ?: throw IllegalArgumentException("Payment method is null")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        adyenLog(AdyenLogLevel.DEBUG) { "onCreateView" }
        _binding = FragmentGooglePayComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adyenLog(AdyenLogLevel.DEBUG) { "onViewCreated" }

        loadComponent()

        binding.componentView.attach(component, viewLifecycleOwner)

        googlePayViewModel.onFragmentLoaded()

        googlePayViewModel.eventsFlow
            .onEach(::handleEvent)
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun loadComponent() {
        @Suppress("SwallowedException")
        try {
            component = getComponentFor(
                fragment = this,
                paymentMethod = paymentMethod,
                checkoutConfiguration = dropInViewModel.checkoutConfiguration,
                dropInOverrideParams = dropInViewModel.getDropInOverrideParams(),
                componentCallback = this,
                analyticsManager = dropInViewModel.analyticsManager,
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
                component.startGooglePayScreen()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        adyenLog(AdyenLogLevel.DEBUG) { "onBackPressed" }
        return performBackAction()
    }

    private fun handleError(componentError: ComponentError) {
        adyenLog(AdyenLogLevel.ERROR) { componentError.errorMessage }
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
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

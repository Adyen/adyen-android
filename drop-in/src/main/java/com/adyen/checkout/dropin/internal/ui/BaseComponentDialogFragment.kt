/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/8/2019.
 */

package com.adyen.checkout.dropin.internal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.dropin.internal.provider.getComponentFor
import com.adyen.checkout.ui.core.R as UICoreR

private const val STORED_PAYMENT_METHOD = "STORED_PAYMENT_METHOD"
private const val NAVIGATED_FROM_PRESELECTED = "NAVIGATED_FROM_PRESELECTED"
private const val PAYMENT_METHOD = "PAYMENT_METHOD"

@Suppress("TooManyFunctions")
internal abstract class BaseComponentDialogFragment :
    DropInBottomSheetDialogFragment(),
    ComponentCallback<PaymentComponentState<*>> {

    var paymentMethod: PaymentMethod = PaymentMethod()
    var storedPaymentMethod: StoredPaymentMethod = StoredPaymentMethod()
    lateinit var component: PaymentComponent
    protected var isStoredPayment = false
    private var navigatedFromPreselected = false

    open class BaseCompanion<T : BaseComponentDialogFragment>(private var classes: Class<T>) {

        fun newInstance(
            paymentMethod: PaymentMethod
        ): T {
            val args = Bundle()
            args.putParcelable(PAYMENT_METHOD, paymentMethod)

            val dialogFragment = classes.getDeclaredConstructor().newInstance()
            dialogFragment.arguments = args
            return dialogFragment
        }

        fun newInstance(
            storedPaymentMethod: StoredPaymentMethod,
            navigatedFromPreselected: Boolean
        ): T {
            val args = Bundle()
            args.putParcelable(STORED_PAYMENT_METHOD, storedPaymentMethod)
            args.putBoolean(NAVIGATED_FROM_PRESELECTED, navigatedFromPreselected)

            val dialogFragment = classes.getDeclaredConstructor().newInstance()
            dialogFragment.arguments = args
            return dialogFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            storedPaymentMethod = it.getParcelable(STORED_PAYMENT_METHOD) ?: storedPaymentMethod
            paymentMethod = it.getParcelable(PAYMENT_METHOD) ?: paymentMethod
            isStoredPayment = !storedPaymentMethod.type.isNullOrEmpty()
            navigatedFromPreselected = it.getBoolean(NAVIGATED_FROM_PRESELECTED, false)
        }
    }

    abstract override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?

    override fun onBackPressed(): Boolean {
        adyenLog(AdyenLogLevel.DEBUG) { "onBackPressed - $navigatedFromPreselected" }

        when {
            navigatedFromPreselected -> protocol.showPreselectedDialog()
            dropInViewModel.shouldSkipToSinglePaymentMethod() -> protocol.terminateDropIn()
            else -> protocol.showPaymentMethodsDialog()
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadComponent()
    }

    private fun loadComponent() {
        try {
            component = if (isStoredPayment) {
                getComponentFor(
                    fragment = this,
                    storedPaymentMethod = storedPaymentMethod,
                    checkoutConfiguration = dropInViewModel.checkoutConfiguration,
                    dropInOverrideParams = dropInViewModel.getDropInOverrideParams(),
                    componentCallback = this,
                    analyticsRepository = dropInViewModel.analyticsRepository,
                    onRedirect = protocol::onRedirect,
                )
            } else {
                getComponentFor(
                    fragment = this,
                    paymentMethod = paymentMethod,
                    checkoutConfiguration = dropInViewModel.checkoutConfiguration,
                    dropInOverrideParams = dropInViewModel.getDropInOverrideParams(),
                    componentCallback = this,
                    analyticsRepository = dropInViewModel.analyticsRepository,
                    analyticsManager = dropInViewModel.analyticsManager,
                    onRedirect = protocol::onRedirect,
                )
            }
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    override fun onSubmit(state: PaymentComponentState<*>) {
        startPayment(state)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        error("This event should not be used in drop-in")
    }

    override fun onError(componentError: ComponentError) {
        onComponentError(componentError)
    }

    private fun startPayment(componentState: PaymentComponentState<out PaymentMethodDetails>) {
        try {
            if (componentState.isValid) {
                requestProtocolCall(componentState)
            } else {
                throw CheckoutException("PaymentComponentState are not valid.")
            }
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    open fun requestProtocolCall(componentState: PaymentComponentState<*>) {
        protocol.requestPaymentsCall(componentState)
    }

    private fun onComponentError(componentError: ComponentError) {
        adyenLog(AdyenLogLevel.ERROR, componentError.exception) { "ComponentError" }
        handleError(componentError)
    }

    fun handleError(componentError: ComponentError) {
        adyenLog(AdyenLogLevel.ERROR) { componentError.errorMessage }
        protocol.showError(null, getString(UICoreR.string.component_error), componentError.errorMessage, true)
    }
}

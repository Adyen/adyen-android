/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/3/2019.
 */

package com.adyen.checkout.dropin.activity

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.adyen.checkout.base.ComponentError
import com.adyen.checkout.base.ComponentView
import com.adyen.checkout.base.PaymentComponent
import com.adyen.checkout.base.PaymentComponentState
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.core.exeption.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.ComponentParsingProvider
import com.adyen.checkout.dropin.R
import kotlinx.android.synthetic.main.activity_component.*

/**
 * Activity that presents the PaymentComponent of the selected Payment Method for the shopper to fill in the required information and continue.
 */
class ComponentActivity : AppCompatActivity(), Observer<PaymentComponentState<in PaymentComponentData>> {

    companion object {
        private val TAG = LogUtil.getTag()

        private const val PAYMENT_METHOD_KEY = "payment_method"

        fun createIntent(context: Context, paymentMethod: PaymentMethod): Intent {
            val intent = Intent(context, ComponentActivity::class.java)
            intent.putExtra(PAYMENT_METHOD_KEY, paymentMethod)
            return intent
        }
    }

    private lateinit var paymentMethod: PaymentMethod

    private lateinit var component: PaymentComponent
    private lateinit var componentView: ComponentView<PaymentComponent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_component)

        try {
            paymentMethod = if (intent.extras?.containsKey(PAYMENT_METHOD_KEY) == true) {
                Logger.d(TAG, "paymentMethod received")
                intent.getParcelableExtra(PAYMENT_METHOD_KEY)
            } else if (savedInstanceState != null && savedInstanceState.containsKey(PAYMENT_METHOD_KEY)) {
                Logger.d(TAG, "paymentMethod restored")
                savedInstanceState.getParcelable(PAYMENT_METHOD_KEY)!!
            } else {
                throw CheckoutException("PaymentMethod not found.")
            }
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
            return
        }

        supportActionBar?.title = paymentMethod.name

        attachComponent(paymentMethod)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable(PAYMENT_METHOD_KEY, paymentMethod)
    }

    override fun onChanged(paymentComponentState: PaymentComponentState<in PaymentComponentData>?) {
        if (componentView.isConfirmationRequired) {
            @Suppress("UsePropertyAccessSyntax")
            payButton.isEnabled = paymentComponentState != null && paymentComponentState.isValid()
        } else {
            startPayment()
        }
    }

    private fun attachComponent(paymentMethod: PaymentMethod) {
        try {
            component = ComponentParsingProvider.getComponentFor(this@ComponentActivity, paymentMethod)
            componentView = ComponentParsingProvider.getViewFor(this@ComponentActivity, paymentMethod)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
            return
        }

        component.observe(this@ComponentActivity, this@ComponentActivity)
        component.observeErrors(this@ComponentActivity, createErrorHandlerObserver())
        componentContainer.addView(componentView as View)
        componentView.attach(component, this@ComponentActivity)

        if (componentView.isConfirmationRequired) {
            payButton.setOnClickListener {
                startPayment()
            }
        } else {
            payButton.visibility = View.GONE
        }
    }

    private fun startPayment() {
        val componentState = component.state
        try {
            if (componentState != null) {
                if (componentState.isValid) {
                    Logger.d(TAG, "Making payment with type ${componentState.data.type}")
                    val intent = LoadingActivity.getIntentForPayments(this, componentState.data)
                    startActivity(intent)
                } else {
                    throw CheckoutException("PaymentComponentState are not valid.")
                }
            } else {
                throw CheckoutException("PaymentComponentState are null.")
            }
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    private fun createErrorHandlerObserver(): Observer<ComponentError> {
        return Observer {
            if (it != null) {
                handleError(it)
            }
        }
    }

    private fun handleError(componentError: ComponentError) {
        Logger.e(TAG, "handleError", componentError.exception)
        Toast.makeText(this@ComponentActivity, R.string.component_error, Toast.LENGTH_LONG).show()
        finish()
    }
}

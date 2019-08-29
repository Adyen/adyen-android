/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/4/2019.
 */

package com.adyen.checkout.dropin.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import com.adyen.checkout.base.ComponentError
import com.adyen.checkout.base.PaymentComponentState
import com.adyen.checkout.base.analytics.AnalyticEvent
import com.adyen.checkout.base.analytics.AnalyticsDispatcher
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.adyen.checkout.dropin.ui.component.ComponentDialogFragment
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListDialogFragment
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayConfiguration

/**
 * Activity that presents the available PaymentMethods to the Shopper.
 */
@Suppress("TooManyFunctions")
class DropInActivity : AppCompatActivity(), DropInBottomSheetDialogFragment.Protocol {

    companion object {
        private val TAG = LogUtil.getTag()

        private const val PAYMENT_METHOD_FRAGMENT_TAG = "PAYMENT_METHODS_DIALOG_FRAGMENT"
        private const val COMPONENT_FRAGMENT_TAG = "COMPONENT_DIALOG_FRAGMENT"

        private const val PAYMENT_METHODS_RESPONSE_KEY = "PAYMENT_METHODS_RESPONSE_KEY"
        private const val DROP_IN_CONFIGURATION_KEY = "DROP_IN_CONFIGURATION_KEY"
        private const val DROP_IN_INTENT = "DROP_IN_INTENT"

        private const val GOOGLE_PAY_REQUEST_CODE = 1

        const val CALL_RESULT_KEY_FROM_RESULT = "CALL_RESULT_KEY_FROM_RESULT"
        const val CALL_RESULT_REQUEST_CODE = 2

        fun createIntent(context: Context, dropInConfiguration: DropInConfiguration, paymentMethodsApiResponse: PaymentMethodsApiResponse): Intent {
            val intent = Intent(context, DropInActivity::class.java)
            intent.putExtra(PAYMENT_METHODS_RESPONSE_KEY, paymentMethodsApiResponse)
            intent.putExtra(DROP_IN_CONFIGURATION_KEY, dropInConfiguration)
            intent.putExtra(DROP_IN_INTENT, dropInConfiguration.resultHandlerIntent)
            return intent
        }
    }

    private lateinit var dropInConfiguration: DropInConfiguration
    private lateinit var dropInViewModel: DropInViewModel
    private lateinit var resultIntent: Intent
    private lateinit var googlePayComponent: GooglePayComponent

    private val googlePayObserver: Observer<PaymentComponentState<PaymentMethodDetails>> = Observer {
        if (it!!.isValid) {
            sendPaymentRequest(it.data)
        }
    }

    private val googlePayErrorObserver: Observer<ComponentError> = Observer {
        Logger.d(TAG, "GooglePay error - ${it?.errorMessage}")
        showPaymentMethodsDialog(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate - $savedInstanceState")
        setContentView(R.layout.activity_payment_method_picker)
        overridePendingTransition(0, 0)

        dropInViewModel = ViewModelProviders.of(this).get(DropInViewModel::class.java)

        dropInConfiguration = if (savedInstanceState != null && savedInstanceState.containsKey(DROP_IN_CONFIGURATION_KEY)) {
            savedInstanceState.getParcelable(DROP_IN_CONFIGURATION_KEY)!!
        } else {
            intent.getParcelableExtra(DROP_IN_CONFIGURATION_KEY)
        }

        dropInViewModel.dropInConfiguration = dropInConfiguration

        dropInViewModel.paymentMethodsApiResponse =
                if (savedInstanceState != null && savedInstanceState.containsKey(PAYMENT_METHODS_RESPONSE_KEY)) {
                    savedInstanceState.getParcelable(PAYMENT_METHODS_RESPONSE_KEY)!!
                } else {
                    intent.getParcelableExtra(PAYMENT_METHODS_RESPONSE_KEY)
                }

        if (getFragmentByTag(COMPONENT_FRAGMENT_TAG) == null && getFragmentByTag(PAYMENT_METHOD_FRAGMENT_TAG) == null) {
            PaymentMethodListDialogFragment.newInstance(false).show(supportFragmentManager, PAYMENT_METHOD_FRAGMENT_TAG)
        }

        resultIntent = if (savedInstanceState != null && savedInstanceState.containsKey(DROP_IN_INTENT)) {
            savedInstanceState.getParcelable(DROP_IN_INTENT)!!
        } else {
            intent.getParcelableExtra(DROP_IN_INTENT)
        }

        sendAnalyticsEvent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GOOGLE_PAY_REQUEST_CODE -> googlePayComponent.handleActivityResult(resultCode, data)
            CALL_RESULT_REQUEST_CODE -> {
                data?.let {
                    dropInConfiguration.resultHandlerIntent.putExtra(DropIn.RESULT_KEY,
                            it.getStringExtra(CALL_RESULT_KEY_FROM_RESULT)).let { intent ->
                        startActivity(intent)
                        overridePendingTransition(0, R.anim.fade_out)
                    }
                }
            }
        }
    }

    override fun sendPaymentRequest(paymentComponentData: PaymentComponentData<in PaymentMethodDetails>) {
        val loadingActivity = LoadingActivity.getIntentForPayments(this, paymentComponentData, dropInConfiguration)
        startActivityForResult(loadingActivity, CALL_RESULT_REQUEST_CODE)
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)

        Logger.d(TAG, "onSaveInstanceState")

        outState?.putParcelable(PAYMENT_METHODS_RESPONSE_KEY, dropInViewModel.paymentMethodsApiResponse)
        outState?.putParcelable(DROP_IN_CONFIGURATION_KEY, dropInConfiguration)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy")
    }

    override fun showComponentDialog(paymentMethod: PaymentMethod, wasInExpandMode: Boolean) {
        Logger.d(TAG, "showComponentDialog")
        hideFragmentDialog(PAYMENT_METHOD_FRAGMENT_TAG)
        ComponentDialogFragment.newInstance(paymentMethod, dropInConfiguration, wasInExpandMode).show(supportFragmentManager, COMPONENT_FRAGMENT_TAG)
    }

    override fun showPaymentMethodsDialog(showInExpandStatus: Boolean) {
        Logger.d(TAG, "showPaymentMethodsDialog")
        hideFragmentDialog(COMPONENT_FRAGMENT_TAG)
        PaymentMethodListDialogFragment.newInstance(showInExpandStatus).show(supportFragmentManager, PAYMENT_METHOD_FRAGMENT_TAG)
    }

    override fun terminateDropIn() {
        Logger.d(TAG, "terminateDropIn")
        finish()
        overridePendingTransition(0, R.anim.fade_out)
    }

    override fun startGooglePay(paymentMethod: PaymentMethod, googlePayConfiguration: GooglePayConfiguration) {
        Logger.d(TAG, "startGooglePay")
        googlePayComponent = GooglePayComponent.PROVIDER.get(this, paymentMethod, googlePayConfiguration)
        googlePayComponent.observe(this@DropInActivity, googlePayObserver)
        googlePayComponent.observeErrors(this@DropInActivity, googlePayErrorObserver)

        hideFragmentDialog(PAYMENT_METHOD_FRAGMENT_TAG)
        googlePayComponent.startGooglePayScreen(this, GOOGLE_PAY_REQUEST_CODE)
    }

    private fun sendAnalyticsEvent() {
        Logger.d(TAG, "sendAnalyticsEvent")
        val analyticEvent = AnalyticEvent.create(this, AnalyticEvent.Flavor.DROPIN,
                "dropin", dropInConfiguration.shopperLocale)
        AnalyticsDispatcher.dispatchEvent(this, dropInConfiguration.environment, analyticEvent)
    }

    private fun hideFragmentDialog(tag: String) {
        getFragmentByTag(tag)?.dismiss()
    }

    private fun getFragmentByTag(tag: String): DialogFragment? {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        return fragment as DialogFragment?
    }
}

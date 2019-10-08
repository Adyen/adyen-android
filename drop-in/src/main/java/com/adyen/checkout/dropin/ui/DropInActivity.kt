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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.adyen.checkout.base.ActionComponentData
import com.adyen.checkout.base.ComponentError
import com.adyen.checkout.base.PaymentComponentState
import com.adyen.checkout.base.analytics.AnalyticEvent
import com.adyen.checkout.base.analytics.AnalyticsDispatcher
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.core.code.Lint
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.ActionHandler
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.adyen.checkout.dropin.ui.component.CardComponentDialogFragment
import com.adyen.checkout.dropin.ui.component.GenericComponentDialogFragment
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListDialogFragment
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.redirect.RedirectUtil
import org.json.JSONObject

/**
 * Activity that presents the available PaymentMethods to the Shopper.
 */
@Suppress("TooManyFunctions", "SyntheticAccessor")
class DropInActivity : AppCompatActivity(), DropInBottomSheetDialogFragment.Protocol, ActionHandler.DetailsRequestedInterface {

    companion object {
        private val TAG = LogUtil.getTag()

        private const val PAYMENT_METHOD_FRAGMENT_TAG = "PAYMENT_METHODS_DIALOG_FRAGMENT"
        private const val COMPONENT_FRAGMENT_TAG = "COMPONENT_DIALOG_FRAGMENT"
        private const val LOADING_FRAGMENT_TAG = "LOADING_DIALOG_FRAGMENT"

        private const val PAYMENT_METHODS_RESPONSE_KEY = "PAYMENT_METHODS_RESPONSE_KEY"
        private const val DROP_IN_CONFIGURATION_KEY = "DROP_IN_CONFIGURATION_KEY"
        private const val IS_LOADING_DIALOG_VISIBLE = "IS_LOADING_DIALOG_VISIBLE"
        private const val DROP_IN_INTENT = "DROP_IN_INTENT"

        private const val GOOGLE_PAY_REQUEST_CODE = 1

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

    private lateinit var callResultIntentFilter: IntentFilter

    private lateinit var localBroadcastManager: LocalBroadcastManager

    @Suppress(Lint.PROTECTED_IN_FINAL)
    protected lateinit var actionHandler: ActionHandler

    // If a new intent is received we can continue processing, otherwise we might need to time out
    @Suppress(Lint.PROTECTED_IN_FINAL)
    private var newIntentReceived = false

    private val loadingDialog = LoadingDialogFragment.newInstance()
    private var isLoadingVisible = false

    private val callResultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Logger.d(TAG, "callResultReceiver onReceive")
            if (context != null && intent != null) {
                newIntentReceived = false
                if (intent.hasExtra(DropInService.API_CALL_RESULT_KEY)) {
                    val callResult = intent.getParcelableExtra<CallResult>(DropInService.API_CALL_RESULT_KEY)
                    handleCallResult(callResult)
                } else {
                    throw CheckoutException("No extra on callResultReceiver")
                }
            }
        }
    }

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
        setContentView(R.layout.activity_drop_in)
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

        savedInstanceState?.let { isLoadingVisible = it.getBoolean(IS_LOADING_DIALOG_VISIBLE, false) }

        callResultIntentFilter = IntentFilter(DropInService.getCallResultAction(this))

        // registerBroadcastReceivers
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(callResultReceiver, callResultIntentFilter)

        actionHandler = ActionHandler(this, this)
        actionHandler.restoreState(savedInstanceState)

        sendAnalyticsEvent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GOOGLE_PAY_REQUEST_CODE -> googlePayComponent.handleActivityResult(resultCode, data)
        }
    }

    override fun sendPaymentRequest(paymentComponentData: PaymentComponentData<in PaymentMethodDetails>) {
        loadingDialog.show(supportFragmentManager, LOADING_FRAGMENT_TAG)
        isLoadingVisible = true
        DropInService.requestPaymentsCall(this, paymentComponentData, dropInConfiguration.serviceComponentName)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Logger.d(TAG, "onNewIntent")
        if (intent != null) {
            handleIntent(intent)
        } else {
            Logger.e(TAG, "Null intent")
        }
    }

    override fun requestDetailsCall(actionComponentData: ActionComponentData) {
        DropInService.requestDetailsCall(this,
            ActionComponentData.SERIALIZER.serialize(actionComponentData),
            dropInConfiguration.serviceComponentName)
    }

    override fun onError(errorMessage: String) {
        Toast.makeText(this, R.string.action_failed, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        Logger.d(TAG, "onSaveInstanceState")

        outState?.let {
            it.putParcelable(PAYMENT_METHODS_RESPONSE_KEY, dropInViewModel.paymentMethodsApiResponse)
            it.putParcelable(DROP_IN_CONFIGURATION_KEY, dropInConfiguration)
            it.putBoolean(IS_LOADING_DIALOG_VISIBLE, isLoadingVisible)

            actionHandler.saveState(it)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!newIntentReceived && isLoadingVisible) {
            getFragmentByTag(LOADING_FRAGMENT_TAG)?.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy")
        localBroadcastManager.unregisterReceiver(callResultReceiver)
    }

    override fun showComponentDialog(paymentMethod: PaymentMethod, wasInExpandMode: Boolean) {
        Logger.d(TAG, "showComponentDialog")
        hideFragmentDialog(PAYMENT_METHOD_FRAGMENT_TAG)
        val dialogFragment = when (paymentMethod.type) {
            PaymentMethodTypes.SCHEME -> CardComponentDialogFragment
            else -> GenericComponentDialogFragment
        }.newInstance(paymentMethod, dropInConfiguration, wasInExpandMode)

        dialogFragment.show(supportFragmentManager, COMPONENT_FRAGMENT_TAG)
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

    @Suppress(Lint.PROTECTED_IN_FINAL)
    protected fun handleCallResult(callResult: CallResult) {
        Logger.d(TAG, "handleCallResult - ${callResult.type.name}")
        when (callResult.type) {
            CallResult.ResultType.FINISHED -> {
                this.sendResult(callResult.content)
            }
            CallResult.ResultType.ACTION -> {
                val action = Action.SERIALIZER.deserialize(JSONObject(callResult.content))
                actionHandler.handleAction(this, action, this::sendResult)
            }
            CallResult.ResultType.ERROR -> {
                Logger.d(TAG, "ERROR - ${callResult.content}")
                Toast.makeText(this, R.string.payment_failed, Toast.LENGTH_LONG).show()
                finish()
            }
            CallResult.ResultType.WAIT -> {
                throw CheckoutException("WAIT CallResult is not expected to be propagated.")
            }
        }
    }

    private fun sendResult(content: String) {
        dropInConfiguration.resultHandlerIntent.putExtra(DropIn.RESULT_KEY, content).let { intent ->
            startActivity(intent)
            terminateDropIn()
        }
    }

    private fun handleIntent(intent: Intent) {
        Logger.d(TAG, "handleIntent - ${intent.action}")
        newIntentReceived = true
        when (intent.action) {
            // Redirect response
            Intent.ACTION_VIEW -> {
                val data = intent.data
                if (data != null && data.toString().startsWith(RedirectUtil.REDIRECT_RESULT_SCHEME)) {
                    actionHandler.handleRedirectResponse(data)
                } else {
                    Logger.e(TAG, "Unexpected response from ACTION_VIEW - ${intent.data}")
                }
            }
            else -> {
                Logger.e(TAG, "Unable to find action")
            }
        }
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

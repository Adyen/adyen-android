/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/4/2019.
 */

package com.adyen.checkout.dropin.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.adyen.checkout.base.ActionComponentData
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.core.code.Lint
import com.adyen.checkout.core.exeption.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.ActionHandler
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInService
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.api.CallResult
import com.adyen.checkout.redirect.RedirectUtil
import org.json.JSONObject

/**
 * Activity that presents a loading state to the user. Used to wait for API calls and handle Redirects.
 */
class LoadingActivity : AppCompatActivity(), ActionHandler.DetailsRequestedInterface {

    companion object {
        @Suppress(Lint.PROTECTED_IN_FINAL)
        protected val TAG = LogUtil.getTag()

        private const val PAYMENT_METHOD_REQUEST_KEY = "payment_method_request"
        private const val ACTION_PAYMENTS = "payments"

        private const val TIME_OUT_DELAY = 10000L

        // False positive
        @Suppress("FunctionParameterNaming")
        fun getIntentForPayments(context: Context, paymentMethod: PaymentComponentData): Intent {
            val intent = Intent(context, LoadingActivity::class.java)
            intent.putExtra(PAYMENT_METHOD_REQUEST_KEY, paymentMethod)
            intent.action = ACTION_PAYMENTS
            return intent
        }
    }

    private val timeoutHandler = Handler()
    private val timeoutRunnable = Runnable {
        Logger.e(TAG, "Timed out without a response.")
        Toast.makeText(this@LoadingActivity, R.string.time_out_error, Toast.LENGTH_LONG).show()
        finish()
    }

    private lateinit var callResultIntentFilter: IntentFilter

    @Suppress(Lint.PROTECTED_IN_FINAL)
    protected lateinit var actionHandler: ActionHandler

    // If a new intent is received we can continue processing, otherwise we might need to time out
    @Suppress(Lint.PROTECTED_IN_FINAL)
    protected var newIntentReceived = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate - $savedInstanceState")

        setContentView(R.layout.loading)
        overridePendingTransition(0, 0)

        actionHandler = ActionHandler(this, this)

        callResultIntentFilter = IntentFilter(DropInService.getCallResultAction(this))

        // registerBroadcastReceivers
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(callResultReceiver, callResultIntentFilter)

        if (savedInstanceState == null) {
            handleIntent(intent)
        } else {
            Logger.d(TAG, "Restoring saved stave from ${intent.action}")
        }
    }

    override fun onResume() {
        super.onResume()
        // Back from other Activity
        if (!newIntentReceived) {
            Logger.d(TAG, "onResume without response, setting time out")
            timeoutHandler.postDelayed(timeoutRunnable, TIME_OUT_DELAY)
        }
    }

    override fun onPause() {
        super.onPause()
        Logger.d(TAG, "onPause")
        timeoutHandler.removeCallbacks(timeoutRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy")
        // unregisterBroadcastReceivers
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.unregisterReceiver(callResultReceiver)
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

    override fun onBackPressed() {
        // Don't finish here, instead wait for onNewIntent() or to be finished in onResume().
        // TODO create time out?
    }

    override fun requestDetailsCall(componentData: ActionComponentData) {
        DropInService.requestDetailsCall(this@LoadingActivity, componentData.data)
    }

    private fun handleIntent(intent: Intent) {
        Logger.d(TAG, "handleIntent - ${intent.action}")
        newIntentReceived = true
        when (intent.action) {
            ACTION_PAYMENTS -> {
                val paymentMethod: PaymentComponentData = intent.getParcelableExtra(PAYMENT_METHOD_REQUEST_KEY)
                DropInService.requestPaymentsCall(this, paymentMethod)
            }
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

    @Suppress(Lint.PROTECTED_IN_FINAL)
    protected fun handleCallResult(callResult: CallResult) {
        Logger.d(TAG, "handleCallResult - ${callResult.type.name}")
        when (callResult.type) {
            CallResult.ResultType.FINISHED -> {
                DropIn.INSTANCE.sendResult(this, callResult.content)
                finish()
            }
            CallResult.ResultType.ACTION -> {
                val action = Action.SERIALIZER.deserialize(JSONObject(callResult.content))
                actionHandler.handleAction(this@LoadingActivity, action)
            }
            CallResult.ResultType.ERROR -> {
                Logger.d(TAG, "ERROR - ${callResult.content}")
                Toast.makeText(this@LoadingActivity, R.string.payment_failed, Toast.LENGTH_SHORT).show()
                finish()
            }
            CallResult.ResultType.WAIT -> {
                throw CheckoutException("WAIT CallResult is not expected to be propagated.")
            }
        }
    }
}

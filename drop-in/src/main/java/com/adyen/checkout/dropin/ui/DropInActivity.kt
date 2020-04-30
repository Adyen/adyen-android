/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/4/2019.
 */

package com.adyen.checkout.dropin.ui

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.adyen.checkout.base.ActionComponentData
import com.adyen.checkout.base.ComponentError
import com.adyen.checkout.base.analytics.AnalyticEvent
import com.adyen.checkout.base.analytics.AnalyticsDispatcher
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.core.code.Lint
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.LocaleUtil
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
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.redirect.RedirectUtil
import com.adyen.checkout.wechatpay.WeChatPayUtils
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
        private const val IS_WAITING_FOR_RESULT = "IS_WAITING_FOR_RESULT"

        private const val GOOGLE_PAY_REQUEST_CODE = 1

        fun createIntent(context: Context, dropInConfiguration: DropInConfiguration, paymentMethodsApiResponse: PaymentMethodsApiResponse): Intent {
            val intent = Intent(context, DropInActivity::class.java)
            intent.putExtra(PAYMENT_METHODS_RESPONSE_KEY, paymentMethodsApiResponse)
            intent.putExtra(DROP_IN_CONFIGURATION_KEY, dropInConfiguration)
            return intent
        }
    }

    private lateinit var dropInViewModel: DropInViewModel
    private lateinit var googlePayComponent: GooglePayComponent

    private lateinit var serviceResultIntentFilter: IntentFilter

    private lateinit var localBroadcastManager: LocalBroadcastManager

    @Suppress(Lint.PROTECTED_IN_FINAL)
    protected lateinit var actionHandler: ActionHandler

    @Suppress(Lint.PROTECTED_IN_FINAL)
    private var isWaitingResult = false

    private val loadingDialog = LoadingDialogFragment.newInstance()

    private val callResultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Logger.d(TAG, "callResultReceiver onReceive")
            if (context != null && intent != null) {
                isWaitingResult = false
                if (intent.hasExtra(DropInService.API_CALL_RESULT_KEY)) {
                    val callResult = intent.getParcelableExtra<CallResult>(DropInService.API_CALL_RESULT_KEY)
                    handleCallResult(callResult)
                } else {
                    throw CheckoutException("No extra on callResultReceiver")
                }
            }
        }
    }

    private val googlePayObserver: Observer<GooglePayComponentState> = Observer {
        if (it!!.isValid) {
            requestPaymentsCall(it.data)
        }
    }

    private val googlePayErrorObserver: Observer<ComponentError> = Observer {
        Logger.d(TAG, "GooglePay error - ${it?.errorMessage}")
        showPaymentMethodsDialog(true)
    }

    override fun attachBaseContext(newBase: Context?) {
        Logger.d(TAG, "attachBaseContext")
        super.attachBaseContext(createLocalizedContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate - $savedInstanceState")
        setContentView(R.layout.activity_drop_in)
        overridePendingTransition(0, 0)

        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        dropInViewModel = ViewModelProviders.of(this).get(DropInViewModel::class.java)

        val bundle = savedInstanceState ?: intent.extras

        val initializationSuccessful = initializeBundleVariables(bundle)
        if (!initializationSuccessful) {
            showError(getString(R.string.action_failed), true)
            return
        }

        if (getFragmentByTag(COMPONENT_FRAGMENT_TAG) == null && getFragmentByTag(PAYMENT_METHOD_FRAGMENT_TAG) == null) {
            PaymentMethodListDialogFragment.newInstance(false).show(supportFragmentManager, PAYMENT_METHOD_FRAGMENT_TAG)
        }

        serviceResultIntentFilter = IntentFilter(DropInService.getServiceResultAction(this))
        localBroadcastManager.registerReceiver(callResultReceiver, serviceResultIntentFilter)

        actionHandler = ActionHandler(this, this)
        actionHandler.restoreState(savedInstanceState)

        sendAnalyticsEvent()
    }

    // False positive from countryStartPosition
    @Suppress("MagicNumber")
    private fun createLocalizedContext(baseContext: Context?): Context? {
        if (baseContext == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return baseContext
        }

        // We needs to get the Locale from sharedPrefs because attachBaseContext is called before onCreate, so we don't have the Config object yet.
        val localeString = baseContext.getSharedPreferences(DropIn.DROP_IN_PREFS, Context.MODE_PRIVATE).getString(DropIn.LOCALE_PREF, "")
        val config = Configuration(baseContext.resources.configuration)

        return try {
            val locale = LocaleUtil.fromLanguageTag(localeString)
            config.setLocale(locale)
            baseContext.createConfigurationContext(config)
        } catch (e: IllegalArgumentException) {
            Logger.e(TAG, "Failed to parse locale $localeString")
            baseContext
        }
    }

    private fun initializeBundleVariables(bundle: Bundle): Boolean {
        isWaitingResult = bundle.getBoolean(IS_WAITING_FOR_RESULT, false)

        var variablesLoaded = true

        if (bundle.containsKey(DROP_IN_CONFIGURATION_KEY)) {
            dropInViewModel.dropInConfiguration = bundle.getParcelable(DROP_IN_CONFIGURATION_KEY)!!
        } else {
            Logger.e(TAG, "DropInConfiguration not found")
            variablesLoaded = false
        }

        if (bundle.containsKey(PAYMENT_METHODS_RESPONSE_KEY)) {
            dropInViewModel.paymentMethodsApiResponse = bundle.getParcelable(PAYMENT_METHODS_RESPONSE_KEY)!!
        } else {
            Logger.e(TAG, "PaymentMethods response not found")
            variablesLoaded = false
        }

        return variablesLoaded
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GOOGLE_PAY_REQUEST_CODE -> googlePayComponent.handleActivityResult(resultCode, data)
        }
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

    override fun requestPaymentsCall(paymentComponentData: PaymentComponentData<*>) {
        isWaitingResult = true
        setLoading(true)
        // include amount value if merchant passed it to the DropIn
        if (!dropInViewModel.dropInConfiguration.amount.isEmpty) {
            paymentComponentData.amount = dropInViewModel.dropInConfiguration.amount
        }
        DropInService.requestPaymentsCall(this, paymentComponentData, dropInViewModel.dropInConfiguration.serviceComponentName)
    }

    override fun showError(errorMessage: String, terminate: Boolean) {
        AlertDialog.Builder(this)
                .setTitle(R.string.error_dialog_title)
                .setMessage(errorMessage)
                .setOnDismissListener { this@DropInActivity.shouldFinish(terminate) }
                .setPositiveButton(R.string.error_dialog_button) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    override fun requestDetailsCall(actionComponentData: ActionComponentData) {
        isWaitingResult = true
        setLoading(true)
        DropInService.requestDetailsCall(this,
            ActionComponentData.SERIALIZER.serialize(actionComponentData),
                dropInViewModel.dropInConfiguration.serviceComponentName)
    }

    override fun onError(errorMessage: String) {
        showError(getString(R.string.action_failed), true)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        Logger.d(TAG, "onSaveInstanceState")

        outState?.run {
            putParcelable(PAYMENT_METHODS_RESPONSE_KEY, dropInViewModel.paymentMethodsApiResponse)
            putParcelable(DROP_IN_CONFIGURATION_KEY, dropInViewModel.dropInConfiguration)
            putBoolean(IS_WAITING_FOR_RESULT, isWaitingResult)

            actionHandler.saveState(this)
        }
    }

    override fun onResume() {
        super.onResume()
        setLoading(isWaitingResult)
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
        }.newInstance(paymentMethod, dropInViewModel.dropInConfiguration, wasInExpandMode)

        dialogFragment.show(supportFragmentManager, COMPONENT_FRAGMENT_TAG)
    }

    override fun showPaymentMethodsDialog(showInExpandStatus: Boolean) {
        Logger.d(TAG, "showPaymentMethodsDialog")
        hideFragmentDialog(COMPONENT_FRAGMENT_TAG)
        PaymentMethodListDialogFragment.newInstance(showInExpandStatus).show(supportFragmentManager, PAYMENT_METHOD_FRAGMENT_TAG)
    }

    override fun terminateDropIn() {
        Logger.d(TAG, "terminateDropIn")
        setResult(Activity.RESULT_CANCELED)
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
                showError(getString(R.string.payment_failed), callResult.dismissDropIn)
            }
            CallResult.ResultType.ERROR_WITH_MESSAGE -> {
                Logger.d(TAG, "ERROR_WITH_MESSAGE - ${callResult.content}")
                showError(callResult.content, callResult.dismissDropIn)
            }
            CallResult.ResultType.WAIT -> {
                throw CheckoutException("WAIT CallResult is not expected to be propagated.")
            }
        }
    }

    private fun shouldFinish(dismissDropIn: Boolean) {
        if (dismissDropIn) {
            terminateDropIn()
        } else {
            setLoading(false)
        }
    }

    private fun sendResult(content: String) {
        dropInViewModel.dropInConfiguration.resultHandlerIntent.putExtra(DropIn.RESULT_KEY, content).let { intent ->
            startActivity(intent)
            terminateDropIn()
        }
    }

    private fun handleIntent(intent: Intent) {
        Logger.d(TAG, "handleIntent: action - ${intent.action}")
        isWaitingResult = false

        if (WeChatPayUtils.isResultIntent(intent)) {
            Logger.d(TAG, "isResultIntent")
            actionHandler.handleWeChatPayResponse(intent)
        }

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
            "dropin", dropInViewModel.dropInConfiguration.shopperLocale)
        AnalyticsDispatcher.dispatchEvent(this, dropInViewModel.dropInConfiguration.environment, analyticEvent)
    }

    private fun hideFragmentDialog(tag: String) {
        getFragmentByTag(tag)?.dismiss()
    }

    private fun getFragmentByTag(tag: String): DialogFragment? {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        return fragment as DialogFragment?
    }

    private fun setLoading(showLoading: Boolean) {
        if (showLoading) {
            if (!loadingDialog.isAdded) {
                loadingDialog.show(supportFragmentManager, LOADING_FRAGMENT_TAG)
            }
        } else {
            getFragmentByTag(LOADING_FRAGMENT_TAG)?.dismiss()
        }
    }
}

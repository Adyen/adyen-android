/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/4/2019.
 */

package com.adyen.checkout.dropin.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.analytics.AnalyticEvent
import com.adyen.checkout.components.analytics.AnalyticsDispatcher
import com.adyen.checkout.components.extensions.createLocalizedContext
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.BalanceResult
import com.adyen.checkout.components.model.payments.response.OrderResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.ActionHandler
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.DropInPrefs
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.service.BalanceDropInServiceResult
import com.adyen.checkout.dropin.service.BaseDropInServiceResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceInterface
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.service.DropInServiceResultError
import com.adyen.checkout.dropin.service.OrderDropInServiceResult
import com.adyen.checkout.dropin.service.RecurringDropInServiceResult
import com.adyen.checkout.dropin.service.SessionDropInServiceInterface
import com.adyen.checkout.dropin.service.SessionDropInServiceResult
import com.adyen.checkout.dropin.ui.action.ActionComponentDialogFragment
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.adyen.checkout.dropin.ui.component.BacsDirectDebitDialogFragment
import com.adyen.checkout.dropin.ui.component.CardComponentDialogFragment
import com.adyen.checkout.dropin.ui.component.GenericComponentDialogFragment
import com.adyen.checkout.dropin.ui.component.GiftCardComponentDialogFragment
import com.adyen.checkout.dropin.ui.component.GooglePayComponentDialogFragment
import com.adyen.checkout.dropin.ui.giftcard.GiftCardBalanceResult
import com.adyen.checkout.dropin.ui.giftcard.GiftCardPaymentConfirmationData
import com.adyen.checkout.dropin.ui.giftcard.GiftCardPaymentConfirmationDialogFragment
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListDialogFragment
import com.adyen.checkout.dropin.ui.stored.PreselectedStoredPaymentMethodFragment
import com.adyen.checkout.dropin.ui.viewmodel.DropInActivityEvent
import com.adyen.checkout.dropin.ui.viewmodel.DropInDestination
import com.adyen.checkout.dropin.ui.viewmodel.DropInViewModel
import com.adyen.checkout.dropin.ui.viewmodel.DropInViewModelFactory
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.sessions.model.Session
import com.adyen.checkout.wechatpay.WeChatPayUtils
import kotlinx.coroutines.launch

private val TAG = LogUtil.getTag()

private const val PRESELECTED_PAYMENT_METHOD_FRAGMENT_TAG = "PRESELECTED_PAYMENT_METHOD_FRAGMENT"
private const val PAYMENT_METHODS_LIST_FRAGMENT_TAG = "PAYMENT_METHODS_LIST_FRAGMENT"
private const val COMPONENT_FRAGMENT_TAG = "COMPONENT_DIALOG_FRAGMENT"
private const val ACTION_FRAGMENT_TAG = "ACTION_DIALOG_FRAGMENT"
private const val LOADING_FRAGMENT_TAG = "LOADING_DIALOG_FRAGMENT"
private const val GIFT_CARD_PAYMENT_CONFIRMATION_FRAGMENT_TAG = "GIFT_CARD_PAYMENT_CONFIRMATION_FRAGMENT"

internal const val GOOGLE_PAY_REQUEST_CODE = 1

/**
 * Activity that presents the available PaymentMethods to the Shopper.
 */
@Suppress("TooManyFunctions")
class DropInActivity :
    AppCompatActivity(),
    DropInBottomSheetDialogFragment.Protocol,
    ActionHandler.ActionHandlingInterface {

    private val dropInViewModel: DropInViewModel by viewModels { DropInViewModelFactory(this) }

    private lateinit var actionHandler: ActionHandler

    private val loadingDialog = LoadingDialogFragment.newInstance()

    private var dropInService: DropInServiceInterface? = null
    private var serviceBound: Boolean = false

    // these queues exist for when a call is requested before the service is bound
    private var paymentDataQueue: PaymentComponentState<*>? = null
    private var actionDataQueue: ActionComponentData? = null
    private var balanceDataQueue: GiftCardComponentState? = null
    private var orderDataQueue: Unit? = null
    private var orderCancellationQueue: OrderRequest? = null

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            Logger.d(TAG, "onServiceConnected")
            val dropInBinder = binder as? DropInService.DropInBinder ?: return
            dropInService = dropInBinder.getService()

            dropInViewModel.onDropInServiceConnected()

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    dropInService?.observeResult {
                        handleDropInServiceResult(it)
                    }
                }
            }

            paymentDataQueue?.let {
                Logger.d(TAG, "Sending queued payment request")
                requestPaymentsCall(it)
                paymentDataQueue = null
            }

            actionDataQueue?.let {
                Logger.d(TAG, "Sending queued action request")
                requestDetailsCall(it)
                actionDataQueue = null
            }
            balanceDataQueue?.let {
                Logger.d(TAG, "Sending queued action request")
                requestBalanceCall(it)
                balanceDataQueue = null
            }
            orderDataQueue?.let {
                Logger.d(TAG, "Sending queued order request")
                requestOrdersCall()
                orderDataQueue = null
            }
            orderCancellationQueue?.let {
                Logger.d(TAG, "Sending queued cancel order request")
                requestCancelOrderCall(it, true)
                orderCancellationQueue = null
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Logger.d(TAG, "onServiceDisconnected")
            dropInService = null
        }
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

        val bundle = savedInstanceState ?: intent.extras

        val initializationSuccessful = assertBundleExists(bundle)
        if (!initializationSuccessful) {
            terminateWithError("Initialization failed")
            return
        }

        if (noDialogPresent()) {
            dropInViewModel.onCreated()
        }

        actionHandler = ActionHandler(this, dropInViewModel.dropInConfiguration)
        actionHandler.restoreState(this, savedInstanceState)

        handleIntent(intent)

        sendAnalyticsEvent()

        initObservers()

        startDropInService()
    }

    private fun noDialogPresent(): Boolean {
        return getFragmentByTag(PRESELECTED_PAYMENT_METHOD_FRAGMENT_TAG) == null &&
            getFragmentByTag(PAYMENT_METHODS_LIST_FRAGMENT_TAG) == null &&
            getFragmentByTag(COMPONENT_FRAGMENT_TAG) == null &&
            getFragmentByTag(ACTION_FRAGMENT_TAG) == null &&
            getFragmentByTag(GIFT_CARD_PAYMENT_CONFIRMATION_FRAGMENT_TAG) == null
    }

    private fun createLocalizedContext(baseContext: Context?): Context? {
        if (baseContext == null) return baseContext

        // We need to get the Locale from sharedPrefs because attachBaseContext is called before onCreate, so we don't have the Config object yet.
        val locale = DropInPrefs.getShopperLocale(baseContext)
        return baseContext.createLocalizedContext(locale)
    }

    private fun assertBundleExists(bundle: Bundle?): Boolean {
        if (bundle == null) {
            Logger.e(TAG, "Failed to initialize - bundle is null")
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkGooglePayActivityResult(requestCode, resultCode, data)
    }

    private fun checkGooglePayActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != GOOGLE_PAY_REQUEST_CODE) return
        val fragment = getFragmentByTag(COMPONENT_FRAGMENT_TAG) as? GooglePayComponentDialogFragment
        if (fragment == null) {
            Logger.e(TAG, "GooglePayComponentDialogFragment is not loaded")
            return
        }
        fragment.handleActivityResult(resultCode, data)
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

    override fun onStart() {
        Logger.v(TAG, "onStart")
        super.onStart()
    }

    private fun startDropInService() {
        val bound = DropInService.startService(
            context = this,
            connection = serviceConnection,
            merchantService = dropInViewModel.serviceComponentName,
            additionalData = dropInViewModel.dropInConfiguration.additionalDataForDropInService,
        )
        if (bound) {
            serviceBound = true
        } else {
            Logger.e(
                TAG,
                "Error binding to ${dropInViewModel.serviceComponentName.className}. " +
                    "The system couldn't find the service or your client doesn't have permission to bind to it"
            )
        }
    }

    override fun onStop() {
        Logger.v(TAG, "onStop")
        super.onStop()
    }

    private fun stopDropInService() {
        if (serviceBound) {
            DropInService.stopService(
                context = this,
                merchantService = dropInViewModel.serviceComponentName,
                connection = serviceConnection,
            )
            serviceBound = false
        }
    }

    override fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        Logger.d(TAG, "requestPaymentsCall")
        if (dropInService == null) {
            Logger.e(TAG, "service is disconnected, adding to queue")
            paymentDataQueue = paymentComponentState
            return
        }
        dropInViewModel.isWaitingResult = true
        setLoading(true)
        dropInViewModel.updatePaymentComponentStateForPaymentsCall(paymentComponentState)
        dropInService?.requestPaymentsCall(paymentComponentState)
    }

    override fun requestDetailsCall(actionComponentData: ActionComponentData) {
        Logger.d(TAG, "requestDetailsCall")
        if (dropInService == null) {
            Logger.e(TAG, "service is disconnected, adding to queue")
            actionDataQueue = actionComponentData
            return
        }
        dropInViewModel.isWaitingResult = true
        setLoading(true)
        dropInService?.requestDetailsCall(actionComponentData)
    }

    override fun showError(errorMessage: String, reason: String, terminate: Boolean) {
        Logger.d(TAG, "showError - message: $errorMessage")
        AlertDialog.Builder(this)
            .setTitle(R.string.error_dialog_title)
            .setMessage(errorMessage)
            .setOnDismissListener { this@DropInActivity.errorDialogDismissed(reason, terminate) }
            .setPositiveButton(R.string.error_dialog_button) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun errorDialogDismissed(reason: String, terminateDropIn: Boolean) {
        if (terminateDropIn) {
            terminateWithError(reason)
        } else {
            setLoading(false)
        }
    }

    override fun displayAction(action: Action) {
        Logger.d(TAG, "showActionDialog")
        setLoading(false)
        hideAllScreens()
        val actionFragment = ActionComponentDialogFragment.newInstance(action)
        actionFragment.show(supportFragmentManager, ACTION_FRAGMENT_TAG)
        actionFragment.setToHandleWhenStarting()
    }

    override fun onActionError(errorMessage: String) {
        showError(getString(R.string.action_failed), errorMessage, true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Logger.d(TAG, "onSaveInstanceState")
        actionHandler.saveState(outState)
    }

    override fun onResume() {
        Logger.v(TAG, "onResume")
        super.onResume()
        setLoading(dropInViewModel.isWaitingResult)
    }

    override fun onDestroy() {
        Logger.v(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun showPreselectedDialog() {
        Logger.d(TAG, "showPreselectedDialog")
        hideAllScreens()
        PreselectedStoredPaymentMethodFragment.newInstance(dropInViewModel.getPreselectedStoredPaymentMethod())
            .show(supportFragmentManager, PRESELECTED_PAYMENT_METHOD_FRAGMENT_TAG)
    }

    override fun showPaymentMethodsDialog() {
        Logger.d(TAG, "showPaymentMethodsDialog")
        hideAllScreens()
        PaymentMethodListDialogFragment().show(supportFragmentManager, PAYMENT_METHODS_LIST_FRAGMENT_TAG)
    }

    override fun showStoredComponentDialog(storedPaymentMethod: StoredPaymentMethod, fromPreselected: Boolean) {
        Logger.d(TAG, "showStoredComponentDialog")
        hideAllScreens()
        val dialogFragment = when {
            CardComponent.PAYMENT_METHOD_TYPES.contains(storedPaymentMethod.type) -> CardComponentDialogFragment
            else -> GenericComponentDialogFragment
        }.newInstance(storedPaymentMethod, fromPreselected)

        dialogFragment.show(supportFragmentManager, COMPONENT_FRAGMENT_TAG)
    }

    override fun showComponentDialog(paymentMethod: PaymentMethod) {
        Logger.d(TAG, "showComponentDialog")
        hideAllScreens()
        val dialogFragment = when {
            CardComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type) ->
                CardComponentDialogFragment.newInstance(paymentMethod)
            BacsDirectDebitComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type) ->
                BacsDirectDebitDialogFragment.newInstance(paymentMethod)
            GiftCardComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type) ->
                GiftCardComponentDialogFragment.newInstance(paymentMethod)
            GooglePayComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type) ->
                GooglePayComponentDialogFragment.newInstance(paymentMethod)
            else -> GenericComponentDialogFragment.newInstance(paymentMethod)
        }

        dialogFragment.show(supportFragmentManager, COMPONENT_FRAGMENT_TAG)
    }

    private fun hideAllScreens() {
        hideFragmentDialog(PRESELECTED_PAYMENT_METHOD_FRAGMENT_TAG)
        hideFragmentDialog(PAYMENT_METHODS_LIST_FRAGMENT_TAG)
        hideFragmentDialog(COMPONENT_FRAGMENT_TAG)
        hideFragmentDialog(ACTION_FRAGMENT_TAG)
        hideFragmentDialog(GIFT_CARD_PAYMENT_CONFIRMATION_FRAGMENT_TAG)
    }

    override fun terminateDropIn() {
        Logger.d(TAG, "terminateDropIn")
        dropInViewModel.cancelDropIn()
    }

    override fun requestBalanceCall(giftCardComponentState: GiftCardComponentState) {
        Logger.d(TAG, "requestCheckBalanceCall")
        val paymentMethod = dropInViewModel.onBalanceCallRequested(giftCardComponentState) ?: return
        if (dropInService == null) {
            Logger.e(TAG, "requestBalanceCall - service is disconnected")
            balanceDataQueue = giftCardComponentState
            return
        }
        dropInViewModel.isWaitingResult = true
        setLoading(true)
        dropInService?.requestBalanceCall(paymentMethod)
    }

    private fun requestOrdersCall() {
        Logger.d(TAG, "requestOrdersCall")
        if (dropInService == null) {
            Logger.e(TAG, "requestOrdersCall - service is disconnected")
            orderDataQueue = Unit
            return
        }
        dropInViewModel.isWaitingResult = true
        setLoading(true)
        dropInService?.requestOrdersCall()
    }

    private fun requestCancelOrderCall(order: OrderRequest, isDropInCancelledByUser: Boolean) {
        Logger.d(TAG, "requestCancelOrderCall")
        if (dropInService == null) {
            Logger.e(TAG, "requestOrdersCall - service is disconnected")
            orderCancellationQueue = order
            return
        }
        dropInViewModel.isWaitingResult = true
        setLoading(true)
        dropInService?.requestCancelOrder(order, isDropInCancelledByUser)
    }

    override fun finishWithAction() {
        Logger.d(TAG, "finishWithActionCall")
        sendResult(DropIn.FINISHED_WITH_ACTION)
    }

    override fun removeStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod) {
        dropInService?.requestRemoveStoredPaymentMethod(storedPaymentMethod)
        setLoading(true)
    }

    private fun handleDropInServiceResult(dropInServiceResult: BaseDropInServiceResult) {
        Logger.d(TAG, "handleDropInServiceResult - ${dropInServiceResult::class.simpleName}")
        dropInViewModel.isWaitingResult = false
        when (dropInServiceResult) {
            is DropInServiceResult -> handleDropInServiceResult(dropInServiceResult)
            is BalanceDropInServiceResult -> handleDropInServiceResult(dropInServiceResult)
            is OrderDropInServiceResult -> handleDropInServiceResult(dropInServiceResult)
            is RecurringDropInServiceResult -> handleDropInServiceResult(dropInServiceResult)
            is SessionDropInServiceResult -> handleDropInServiceResult(dropInServiceResult)
        }
    }

    private fun handleDropInServiceResult(dropInServiceResult: DropInServiceResult) {
        when (dropInServiceResult) {
            is DropInServiceResult.Finished -> sendResult(dropInServiceResult.result)
            is DropInServiceResult.Action -> handleAction(dropInServiceResult.action)
            is DropInServiceResult.Update -> handlePaymentMethodsUpdate(dropInServiceResult)
            is DropInServiceResult.Error -> handleErrorDropInServiceResult(dropInServiceResult)
        }
    }

    private fun handleDropInServiceResult(dropInServiceResult: BalanceDropInServiceResult) {
        when (dropInServiceResult) {
            is BalanceDropInServiceResult.Balance -> handleBalanceResult(dropInServiceResult.balance)
            is BalanceDropInServiceResult.Error -> handleErrorDropInServiceResult(dropInServiceResult)
        }
    }

    private fun handleDropInServiceResult(dropInServiceResult: OrderDropInServiceResult) {
        when (dropInServiceResult) {
            is OrderDropInServiceResult.OrderCreated -> handleOrderResult(dropInServiceResult.order)
            is OrderDropInServiceResult.Error -> handleErrorDropInServiceResult(dropInServiceResult)
        }
    }

    private fun handleDropInServiceResult(dropInServiceResult: RecurringDropInServiceResult) {
        when (dropInServiceResult) {
            is RecurringDropInServiceResult.PaymentMethodRemoved ->
                handleRemovePaymentMethodResult(dropInServiceResult.id)
            is RecurringDropInServiceResult.Error -> handleErrorDropInServiceResult(dropInServiceResult)
        }
    }

    private fun handleDropInServiceResult(dropInServiceResult: SessionDropInServiceResult) {
        when (dropInServiceResult) {
            is SessionDropInServiceResult.SetupDone ->
                dropInViewModel.onSessionSetupSuccessful(dropInServiceResult.paymentMethods)
            is SessionDropInServiceResult.SessionDataChanged ->
                dropInViewModel.onSessionDataChanged(dropInServiceResult.sessionData)
            is SessionDropInServiceResult.SessionTakenOverUpdated ->
                dropInViewModel.onSessionTakenOverUpdated(dropInServiceResult.isFlowTakenOver)
            is SessionDropInServiceResult.Error -> handleErrorDropInServiceResult(dropInServiceResult)
        }
    }

    private fun handleErrorDropInServiceResult(dropInServiceResult: DropInServiceResultError) {
        Logger.d(TAG, "handleDropInServiceResult ERROR - reason: ${dropInServiceResult.reason}")
        val reason = dropInServiceResult.reason ?: "Unspecified reason"
        val errorMessage = dropInServiceResult.errorMessage ?: getString(R.string.payment_failed)
        showError(errorMessage, reason, dropInServiceResult.dismissDropIn)
    }

    private fun handleAction(action: Action) {
        actionHandler.handleAction(this, action, ::sendResult)
    }

    private fun handlePaymentMethodsUpdate(dropInServiceResult: DropInServiceResult.Update) {
        dropInViewModel.handlePaymentMethodsUpdate(
            dropInServiceResult.paymentMethodsApiResponse,
            dropInServiceResult.order
        )
    }

    private fun sendResult(content: String) {
        val resultIntent = Intent().putExtra(DropIn.RESULT_KEY, content)
        setResult(Activity.RESULT_OK, resultIntent)
        terminateSuccessfully()
    }

    private fun terminateSuccessfully() {
        Logger.d(TAG, "terminateSuccessfully")
        terminate()
    }

    private fun terminateWithError(reason: String) {
        Logger.d(TAG, "terminateWithError")
        val resultIntent = Intent().putExtra(DropIn.ERROR_REASON_KEY, reason)
        setResult(Activity.RESULT_CANCELED, resultIntent)
        terminate()
    }

    private fun terminate() {
        Logger.d(TAG, "terminate")
        stopDropInService()
        finish()
        overridePendingTransition(0, R.anim.fade_out)
    }

    private fun handleIntent(intent: Intent) {
        Logger.d(TAG, "handleIntent: action - ${intent.action}")
        dropInViewModel.isWaitingResult = false

        if (WeChatPayUtils.isResultIntent(intent)) {
            Logger.d(TAG, "isResultIntent")
            actionHandler.handleWeChatPayResponse(intent)
        }

        when (intent.action) {
            // Redirect response
            Intent.ACTION_VIEW -> {
                val data = intent.data
                if (data != null && data.toString().startsWith(RedirectComponent.REDIRECT_RESULT_SCHEME)) {
                    actionHandler.handleRedirectResponse(intent)
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
        val analyticEvent = AnalyticEvent.create(
            this,
            AnalyticEvent.Flavor.DROPIN,
            "dropin",
            dropInViewModel.dropInConfiguration.shopperLocale
        )
        AnalyticsDispatcher.dispatchEvent(this, dropInViewModel.dropInConfiguration.environment, analyticEvent)
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dropInViewModel.eventsFlow.collect { handleEvent(it) }
            }
        }
    }

    private fun handleEvent(event: DropInActivityEvent) {
        when (event) {
            is DropInActivityEvent.MakePartialPayment -> requestPaymentsCall(event.paymentComponentState)
            is DropInActivityEvent.ShowPaymentMethods -> {
                setLoading(false)
                showPaymentMethodsDialog()
            }
            is DropInActivityEvent.CancelOrder -> requestCancelOrderCall(event.order, event.isDropInCancelledByUser)
            is DropInActivityEvent.CancelDropIn -> terminateWithError(DropIn.ERROR_REASON_USER_CANCELED)
            is DropInActivityEvent.NavigateTo -> loadFragment(event.destination)
            is DropInActivityEvent.SessionServiceConnected -> onSessionServiceConnected(event)
        }
    }

    private fun onSessionServiceConnected(event: DropInActivityEvent.SessionServiceConnected) {
        (dropInService as? SessionDropInServiceInterface)?.initialize(
            session = event.session,
            clientKey = event.clientKey,
            baseUrl = event.baseUrl,
            shouldFetchPaymentMethods = event.shouldFetchPaymentMethods,
            isFlowTakenOver = event.isFlowTakenOver,
        )
    }

    private fun loadFragment(destination: DropInDestination) {
        when (destination) {
            is DropInDestination.ActionComponent -> displayAction(destination.action)
            is DropInDestination.GiftCardPaymentConfirmation -> showGiftCardPaymentConfirmationDialog(destination.data)
            is DropInDestination.PaymentComponent -> showComponentDialog(destination.paymentMethod)
            is DropInDestination.PaymentMethods -> showPaymentMethodsDialog()
            is DropInDestination.PreselectedStored -> showPreselectedDialog()
        }
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

    private fun handleBalanceResult(balanceResult: BalanceResult) {
        Logger.v(TAG, "handleBalanceResult")
        val result = dropInViewModel.handleBalanceResult(balanceResult)
        Logger.d(TAG, "handleBalanceResult: ${result::class.java.simpleName}")
        when (result) {
            is GiftCardBalanceResult.Error -> showError(
                getString(result.errorMessage),
                result.reason,
                result.terminateDropIn
            )
            is GiftCardBalanceResult.FullPayment -> handleGiftCardFullPayment(result)
            is GiftCardBalanceResult.RequestOrderCreation -> requestOrdersCall()
            is GiftCardBalanceResult.RequestPartialPayment -> requestPartialPayment()
        }
    }

    private fun handleGiftCardFullPayment(fullPayment: GiftCardBalanceResult.FullPayment) {
        Logger.d(TAG, "handleGiftCardFullPayment")
        setLoading(false)
        showGiftCardPaymentConfirmationDialog(fullPayment.data)
    }

    private fun showGiftCardPaymentConfirmationDialog(data: GiftCardPaymentConfirmationData) {
        Logger.d(TAG, "showGiftCardPaymentConfirmationDialog")
        hideAllScreens()
        GiftCardPaymentConfirmationDialogFragment.newInstance(data)
            .show(supportFragmentManager, GIFT_CARD_PAYMENT_CONFIRMATION_FRAGMENT_TAG)
    }

    private fun handleOrderResult(order: OrderResponse) {
        Logger.v(TAG, "handleOrderResult")
        dropInViewModel.handleOrderCreated(order)
    }

    override fun requestPartialPayment() {
        dropInViewModel.partialPaymentRequested()
    }

    override fun requestOrderCancellation() {
        dropInViewModel.orderCancellationRequested()
    }

    private fun handleRemovePaymentMethodResult(id: String) {
        setLoading(false)
        dropInViewModel.removeStoredPaymentMethodWithId(id)

        val preselectedStoredPaymentMethodFragment =
            getFragmentByTag(PRESELECTED_PAYMENT_METHOD_FRAGMENT_TAG) as? PreselectedStoredPaymentMethodFragment
        if (preselectedStoredPaymentMethodFragment != null) {
            showPaymentMethodsDialog()
            return
        }

        val paymentMethodListDialogFragment =
            getFragmentByTag(PAYMENT_METHODS_LIST_FRAGMENT_TAG) as? PaymentMethodListDialogFragment
        if (paymentMethodListDialogFragment != null) {
            paymentMethodListDialogFragment.removeStoredPaymentMethod(id)
            return
        }
    }

    companion object {

        fun createIntent(
            context: Context,
            dropInConfiguration: DropInConfiguration,
            paymentMethodsApiResponse: PaymentMethodsApiResponse,
            service: ComponentName,
        ): Intent {
            val intent = Intent(context, DropInActivity::class.java)
            DropInViewModel.putIntentExtras(intent, dropInConfiguration, paymentMethodsApiResponse, service)
            return intent
        }

        fun createIntent(
            context: Context,
            dropInConfiguration: DropInConfiguration,
            session: Session,
            service: ComponentName,
        ): Intent {
            val intent = Intent(context, DropInActivity::class.java)
            DropInViewModel.putIntentExtras(intent, dropInConfiguration, session, service)
            return intent
        }
    }
}

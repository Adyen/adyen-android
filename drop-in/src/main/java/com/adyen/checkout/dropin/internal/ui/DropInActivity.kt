/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/4/2019.
 */

package com.adyen.checkout.dropin.internal.ui

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
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.util.createLocalizedContext
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.dropin.AddressLookupDropInServiceResult
import com.adyen.checkout.dropin.BalanceDropInServiceResult
import com.adyen.checkout.dropin.BaseDropInServiceResult
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInServiceResult
import com.adyen.checkout.dropin.DropInServiceResultError
import com.adyen.checkout.dropin.OrderDropInServiceResult
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.RecurringDropInServiceResult
import com.adyen.checkout.dropin.SessionDropInServiceResult
import com.adyen.checkout.dropin.databinding.ActivityDropInBinding
import com.adyen.checkout.dropin.internal.provider.getFragmentForPaymentMethod
import com.adyen.checkout.dropin.internal.provider.getFragmentForStoredPaymentMethod
import com.adyen.checkout.dropin.internal.service.BaseDropInService
import com.adyen.checkout.dropin.internal.service.BaseDropInServiceInterface
import com.adyen.checkout.dropin.internal.service.SessionDropInServiceInterface
import com.adyen.checkout.dropin.internal.ui.model.DropInActivityEvent
import com.adyen.checkout.dropin.internal.ui.model.DropInDestination
import com.adyen.checkout.dropin.internal.ui.model.GiftCardPaymentConfirmationData
import com.adyen.checkout.dropin.internal.util.DropInPrefs
import com.adyen.checkout.dropin.internal.util.checkCompileOnly
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.adyen.checkout.wechatpay.WeChatPayUtils
import kotlinx.coroutines.launch
import com.adyen.checkout.ui.core.R as UICoreR

/**
 * Activity that presents the available PaymentMethods to the Shopper.
 */
@Suppress("TooManyFunctions")
internal class DropInActivity :
    AppCompatActivity(),
    DropInBottomSheetDialogFragment.Protocol {

    private val dropInViewModel: DropInViewModel by viewModels { DropInViewModelFactory(this) }

    private var dropInService: BaseDropInServiceInterface? = null
    private var serviceBound: Boolean = false

    // these queues exist for when a call is requested before the service is bound
    private var paymentDataQueue: PaymentComponentState<*>? = null
    private var actionDataQueue: ActionComponentData? = null
    private var balanceDataQueue: GiftCardComponentState? = null
    private var orderDataQueue: Unit? = null
    private var orderCancellationQueue: OrderRequest? = null

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            adyenLog(AdyenLogLevel.DEBUG) { "onServiceConnected" }
            val dropInBinder = binder as? BaseDropInService.DropInBinder ?: return
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
                adyenLog(AdyenLogLevel.DEBUG) { "Sending queued payment request" }
                requestPaymentsCall(it)
                paymentDataQueue = null
            }

            actionDataQueue?.let {
                adyenLog(AdyenLogLevel.DEBUG) { "Sending queued action request" }
                requestDetailsCall(it)
                actionDataQueue = null
            }
            balanceDataQueue?.let {
                adyenLog(AdyenLogLevel.DEBUG) { "Sending queued action request" }
                requestBalanceCall(it)
                balanceDataQueue = null
            }
            orderDataQueue?.let {
                adyenLog(AdyenLogLevel.DEBUG) { "Sending queued order request" }
                requestOrdersCall()
                orderDataQueue = null
            }
            orderCancellationQueue?.let {
                adyenLog(AdyenLogLevel.DEBUG) { "Sending queued cancel order request" }
                requestCancelOrderCall(it, true)
                orderCancellationQueue = null
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            adyenLog(AdyenLogLevel.DEBUG) { "onServiceDisconnected" }
            serviceBound = false
            dropInService = null
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        adyenLog(AdyenLogLevel.DEBUG) { "attachBaseContext" }
        super.attachBaseContext(createLocalizedContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adyenLog(AdyenLogLevel.DEBUG) { "onCreate - $savedInstanceState" }
        val binding = ActivityDropInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(0, 0)

        if (!DropInBundleHandler.assertBundleExists(intent.extras)) {
            terminateWithError("Initialization failed")
            return
        }

        dropInViewModel.onCreated(noDialogPresent())

        handleIntent(intent)

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

    @Suppress("ReturnCount")
    private fun createLocalizedContext(baseContext: Context?): Context? {
        if (baseContext == null) return null

        // We need to get the Locale from sharedPrefs because attachBaseContext is called before onCreate, so we don't
        // have the Config object yet.
        val locale = DropInPrefs.getShopperLocale(baseContext) ?: return baseContext
        return baseContext.createLocalizedContext(locale)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        adyenLog(AdyenLogLevel.DEBUG) { "onNewIntent" }
        handleIntent(intent)
    }

    override fun onStart() {
        adyenLog(AdyenLogLevel.VERBOSE) { "onStart" }
        super.onStart()
    }

    private fun startDropInService() {
        val bound = BaseDropInService.bindService(
            context = this,
            connection = serviceConnection,
            merchantService = dropInViewModel.serviceComponentName,
            additionalData = dropInViewModel.dropInParams.additionalDataForDropInService,
        )
        if (bound) {
            serviceBound = true
        } else {
            adyenLog(AdyenLogLevel.ERROR) {
                "Error binding to ${dropInViewModel.serviceComponentName.className}. " +
                    "The system couldn't find the service or your client doesn't have permission to bind to it"
            }
        }
    }

    override fun onStop() {
        adyenLog(AdyenLogLevel.VERBOSE) { "onStop" }
        super.onStop()
    }

    private fun stopDropInService() {
        if (serviceBound) {
            BaseDropInService.unbindService(
                context = this,
                connection = serviceConnection,
            )
            serviceBound = false
        }
    }

    override fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        adyenLog(AdyenLogLevel.DEBUG) { "requestPaymentsCall" }
        if (dropInService == null) {
            adyenLog(AdyenLogLevel.ERROR) { "service is disconnected, adding to queue" }
            paymentDataQueue = paymentComponentState
            return
        }
        dropInViewModel.isWaitingResult = true
        setLoading(true)
        dropInViewModel.updatePaymentComponentStateForPaymentsCall(paymentComponentState)
        dropInService?.requestPaymentsCall(paymentComponentState)
    }

    override fun requestDetailsCall(actionComponentData: ActionComponentData) {
        adyenLog(AdyenLogLevel.DEBUG) { "requestDetailsCall" }
        if (dropInService == null) {
            adyenLog(AdyenLogLevel.ERROR) { "service is disconnected, adding to queue" }
            actionDataQueue = actionComponentData
            return
        }
        dropInViewModel.isWaitingResult = true
        setLoading(true)
        dropInService?.requestDetailsCall(actionComponentData)
    }

    override fun showError(dialogTitle: String?, errorMessage: String, reason: String, terminate: Boolean) {
        adyenLog(AdyenLogLevel.DEBUG) { "showError - message: $errorMessage" }
        val title = dialogTitle ?: getString(UICoreR.string.error_dialog_title)
        showDialog(title, errorMessage) {
            errorDialogDismissed(reason, terminate)
        }
    }

    private fun errorDialogDismissed(reason: String, terminateDropIn: Boolean) {
        if (terminateDropIn) {
            terminateWithError(reason)
        }
    }

    override fun onResume() {
        adyenLog(AdyenLogLevel.VERBOSE) { "onResume" }
        super.onResume()
        setLoading(dropInViewModel.isWaitingResult)
    }

    override fun onDestroy() {
        adyenLog(AdyenLogLevel.VERBOSE) { "onDestroy" }
        stopDropInService()
        super.onDestroy()
    }

    override fun showPreselectedDialog() {
        adyenLog(AdyenLogLevel.DEBUG) { "showPreselectedDialog" }
        hideAllScreens()
        PreselectedStoredPaymentMethodFragment.newInstance(dropInViewModel.getPreselectedStoredPaymentMethod())
            .show(supportFragmentManager, PRESELECTED_PAYMENT_METHOD_FRAGMENT_TAG)
    }

    override fun showPaymentMethodsDialog() {
        adyenLog(AdyenLogLevel.DEBUG) { "showPaymentMethodsDialog" }
        hideAllScreens()
        PaymentMethodListDialogFragment().show(supportFragmentManager, PAYMENT_METHODS_LIST_FRAGMENT_TAG)
    }

    override fun showStoredComponentDialog(storedPaymentMethod: StoredPaymentMethod, fromPreselected: Boolean) {
        adyenLog(AdyenLogLevel.DEBUG) { "showStoredComponentDialog" }
        hideAllScreens()
        val dialogFragment = getFragmentForStoredPaymentMethod(storedPaymentMethod, fromPreselected)
        dialogFragment.show(supportFragmentManager, COMPONENT_FRAGMENT_TAG)
    }

    override fun showComponentDialog(paymentMethod: PaymentMethod) {
        adyenLog(AdyenLogLevel.DEBUG) { "showComponentDialog" }
        hideAllScreens()
        val dialogFragment = getFragmentForPaymentMethod(paymentMethod)
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
        adyenLog(AdyenLogLevel.DEBUG) { "terminateDropIn" }
        dropInViewModel.cancelDropIn()
    }

    override fun requestBalanceCall(giftCardComponentState: GiftCardComponentState) {
        adyenLog(AdyenLogLevel.DEBUG) { "requestCheckBalanceCall" }
        dropInViewModel.onBalanceCallRequested(giftCardComponentState) ?: return
        if (dropInService == null) {
            adyenLog(AdyenLogLevel.ERROR) { "requestBalanceCall - service is disconnected" }
            balanceDataQueue = giftCardComponentState
            return
        }
        dropInViewModel.isWaitingResult = true
        setLoading(true)
        dropInService?.requestBalanceCall(giftCardComponentState)
    }

    private fun requestOrdersCall() {
        adyenLog(AdyenLogLevel.DEBUG) { "requestOrdersCall" }
        if (dropInService == null) {
            adyenLog(AdyenLogLevel.ERROR) { "requestOrdersCall - service is disconnected" }
            orderDataQueue = Unit
            return
        }
        dropInViewModel.isWaitingResult = true
        setLoading(true)
        dropInService?.requestOrdersCall()
    }

    private fun requestCancelOrderCall(order: OrderRequest, isDropInCancelledByUser: Boolean) {
        adyenLog(AdyenLogLevel.DEBUG) { "requestCancelOrderCall" }
        if (dropInService == null) {
            adyenLog(AdyenLogLevel.ERROR) { "requestOrdersCall - service is disconnected" }
            orderCancellationQueue = order
            return
        }
        dropInViewModel.isWaitingResult = true
        setLoading(true)
        dropInService?.requestCancelOrder(order, isDropInCancelledByUser)
    }

    override fun finishWithAction() {
        adyenLog(AdyenLogLevel.DEBUG) { "finishWithActionCall" }
        sendResult(DropIn.FINISHED_WITH_ACTION)
    }

    override fun removeStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod) {
        setLoading(true)
        dropInService?.requestRemoveStoredPaymentMethod(storedPaymentMethod)
    }

    private fun handleDropInServiceResult(dropInServiceResult: BaseDropInServiceResult) {
        adyenLog(AdyenLogLevel.DEBUG) { "handleDropInServiceResult - ${dropInServiceResult::class.simpleName}" }
        dropInViewModel.isWaitingResult = false
        setLoading(false)
        when (dropInServiceResult) {
            is DropInServiceResult -> handleDropInServiceResult(dropInServiceResult)
            is BalanceDropInServiceResult -> handleDropInServiceResult(dropInServiceResult)
            is OrderDropInServiceResult -> handleDropInServiceResult(dropInServiceResult)
            is RecurringDropInServiceResult -> handleDropInServiceResult(dropInServiceResult)
            is SessionDropInServiceResult -> handleDropInServiceResult(dropInServiceResult)
            is AddressLookupDropInServiceResult -> handleDropInServiceResult(dropInServiceResult)
        }
    }

    private fun handleDropInServiceResult(dropInServiceResult: DropInServiceResult) {
        when (dropInServiceResult) {
            is DropInServiceResult.Finished -> handleFinished(dropInServiceResult)
            is DropInServiceResult.Action -> handleAction(dropInServiceResult.action)
            is DropInServiceResult.Update -> handlePaymentMethodsUpdate(dropInServiceResult)
            is DropInServiceResult.Error -> handleErrorDropInServiceResult(dropInServiceResult)
            is DropInServiceResult.ToPaymentMethodsList -> dropInViewModel.onToPaymentMethodsList(
                dropInServiceResult.paymentMethodsApiResponse,
            )
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

    private fun handleDropInServiceResult(dropInServiceResult: AddressLookupDropInServiceResult) {
        when (dropInServiceResult) {
            is AddressLookupDropInServiceResult.LookupResult -> handleAddressLookupOptionsUpdate(dropInServiceResult)
            is AddressLookupDropInServiceResult.LookupComplete -> handleAddressLookupComplete(dropInServiceResult)
            is AddressLookupDropInServiceResult.Error -> handleErrorDropInServiceResult(dropInServiceResult)
        }
    }

    private fun handleDropInServiceResult(dropInServiceResult: SessionDropInServiceResult) {
        when (dropInServiceResult) {
            is SessionDropInServiceResult.SessionDataChanged ->
                dropInViewModel.onSessionDataChanged(dropInServiceResult.sessionData)

            is SessionDropInServiceResult.SessionTakenOverUpdated ->
                dropInViewModel.onSessionTakenOverUpdated(dropInServiceResult.isFlowTakenOver)

            is SessionDropInServiceResult.Error -> handleErrorDropInServiceResult(dropInServiceResult)
            is SessionDropInServiceResult.Finished -> sendResult(dropInServiceResult.result)
        }
    }

    private fun handleErrorDropInServiceResult(dropInServiceResult: DropInServiceResultError) {
        val reason = dropInServiceResult.reason ?: "Unspecified reason"
        adyenLog(AdyenLogLevel.DEBUG) { "handleDropInServiceResult ERROR - reason: $reason" }

        dropInServiceResult.errorDialog?.let { errorDialog ->
            val errorMessage = errorDialog.message ?: getString(R.string.unknown_error)
            showError(errorDialog.title, errorMessage, reason, dropInServiceResult.dismissDropIn)
        } ?: run {
            if (dropInServiceResult.dismissDropIn) {
                terminateWithError(reason)
            }
        }
    }

    private fun handleFinished(dropInServiceResult: DropInServiceResult.Finished) {
        if (dropInServiceResult.finishedDialog != null) {
            showDialog(dropInServiceResult.finishedDialog.title, dropInServiceResult.finishedDialog.message) {
                sendResult(dropInServiceResult.result)
            }
        } else {
            sendResult(dropInServiceResult.result)
        }
    }

    private fun handleAction(action: Action) {
        adyenLog(AdyenLogLevel.DEBUG) { "showActionDialog" }
        getExistingActionFragment()?.apply {
            handleAction(action)
            return
        }
        hideAllScreens()
        val actionFragment = ActionComponentDialogFragment.newInstance(action, dropInViewModel.checkoutConfiguration)
        actionFragment.show(supportFragmentManager, ACTION_FRAGMENT_TAG)
    }

    private fun handlePaymentMethodsUpdate(dropInServiceResult: DropInServiceResult.Update) {
        dropInViewModel.handlePaymentMethodsUpdate(
            dropInServiceResult.paymentMethodsApiResponse,
            dropInServiceResult.order,
        )
    }

    private fun handleAddressLookupOptionsUpdate(lookupResult: AddressLookupDropInServiceResult.LookupResult) {
        dropInViewModel.onAddressLookupOptions(lookupResult.options)
    }

    private fun handleAddressLookupComplete(lookupResult: AddressLookupDropInServiceResult.LookupComplete) {
        dropInViewModel.onAddressLookupComplete(lookupResult.lookupAddress)
    }

    private fun sendResult(result: String) {
        val resultIntent = Intent().putExtra(DropIn.RESULT_KEY, result)
        setResult(Activity.RESULT_OK, resultIntent)
        terminateSuccessfully()
    }

    private fun sendResult(result: SessionPaymentResult) {
        val resultIntent = Intent().putExtra(DropIn.SESSION_RESULT_KEY, result)
        setResult(Activity.RESULT_OK, resultIntent)
        terminateSuccessfully()
    }

    private fun terminateSuccessfully() {
        adyenLog(AdyenLogLevel.DEBUG) { "terminateSuccessfully" }
        terminate()
    }

    private fun terminateWithError(reason: String) {
        adyenLog(AdyenLogLevel.DEBUG) { "terminateWithError" }
        val resultIntent = Intent().putExtra(DropIn.ERROR_REASON_KEY, reason)
        setResult(Activity.RESULT_CANCELED, resultIntent)
        terminate()
    }

    private fun terminate() {
        adyenLog(AdyenLogLevel.DEBUG) { "terminate" }
        stopDropInService()
        finish()
        overridePendingTransition(0, R.anim.fade_out)
    }

    private fun handleIntent(intent: Intent) {
        adyenLog(AdyenLogLevel.DEBUG) { "handleIntent: action - ${intent.action}" }
        dropInViewModel.isWaitingResult = false

        if (isWeChatPayIntent(intent)) {
            adyenLog(AdyenLogLevel.DEBUG) { "isResultIntent" }
            handleActionIntentResponse(intent)
        }

        when (intent.action) {
            // Redirect response
            Intent.ACTION_VIEW -> {
                val data = intent.data
                if (data != null && data.toString().startsWith(RedirectComponent.REDIRECT_RESULT_SCHEME)) {
                    handleActionIntentResponse(intent)
                } else {
                    adyenLog(AdyenLogLevel.ERROR) { "Unexpected response from ACTION_VIEW - ${intent.data}" }
                }
            }

            else -> {
                adyenLog(AdyenLogLevel.ERROR) { "Unable to find action" }
            }
        }
    }

    private fun isWeChatPayIntent(intent: Intent): Boolean = checkCompileOnly { WeChatPayUtils.isResultIntent(intent) }

    private fun handleActionIntentResponse(intent: Intent) {
        val actionFragment = getExistingActionFragment()
        if (actionFragment == null) {
            adyenLog(AdyenLogLevel.ERROR) { "ActionComponentDialogFragment is not loaded" }
            return
        }
        actionFragment.handleIntent(intent)
    }

    private fun getExistingActionFragment(): ActionComponentDialogFragment? {
        return getFragmentByTag(ACTION_FRAGMENT_TAG) as? ActionComponentDialogFragment
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
            sessionModel = event.sessionModel,
            clientKey = event.clientKey,
            environment = event.environment,
            isFlowTakenOver = event.isFlowTakenOver,
        )
    }

    private fun loadFragment(destination: DropInDestination) {
        when (destination) {
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
        val loadingDialog = getFragmentByTag(LOADING_FRAGMENT_TAG)
        if (showLoading) {
            if (loadingDialog == null && !supportFragmentManager.isDestroyed) {
                LoadingDialogFragment.newInstance().show(supportFragmentManager, LOADING_FRAGMENT_TAG)
            }
        } else {
            // Make sure the loading fragment transaction is completed before trying to dismiss it.
            // This will prevent the loading fragment from overlapping any other dialogs.
            if (loadingDialog == null) {
                supportFragmentManager.executePendingTransactionsSafe()
            }
            getFragmentByTag(LOADING_FRAGMENT_TAG)?.dismiss()
        }
    }

    private fun FragmentManager.executePendingTransactionsSafe() {
        try {
            executePendingTransactions()
        } catch (e: IllegalStateException) {
            adyenLog(AdyenLogLevel.WARN, e) { "Executing pending transactions failed" }
        }
    }

    private fun handleBalanceResult(balanceResult: BalanceResult) {
        adyenLog(AdyenLogLevel.VERBOSE) { "handleBalanceResult" }
        val result = dropInViewModel.handleBalanceResult(balanceResult)
        adyenLog(AdyenLogLevel.DEBUG) { "handleBalanceResult: ${result::class.java.simpleName}" }
        when (result) {
            is GiftCardBalanceResult.Error -> showError(
                dialogTitle = null,
                errorMessage = getString(result.errorMessage),
                reason = result.reason,
                terminate = result.terminateDropIn,
            )

            is GiftCardBalanceResult.FullPayment -> handleGiftCardFullPayment(result)
            is GiftCardBalanceResult.RequestOrderCreation -> requestOrdersCall()
            is GiftCardBalanceResult.RequestPartialPayment -> requestPartialPayment()
        }
    }

    private fun handleGiftCardFullPayment(fullPayment: GiftCardBalanceResult.FullPayment) {
        adyenLog(AdyenLogLevel.DEBUG) { "handleGiftCardFullPayment" }
        showGiftCardPaymentConfirmationDialog(fullPayment.data)
    }

    private fun showGiftCardPaymentConfirmationDialog(data: GiftCardPaymentConfirmationData) {
        adyenLog(AdyenLogLevel.DEBUG) { "showGiftCardPaymentConfirmationDialog" }
        hideAllScreens()
        GiftCardPaymentConfirmationDialogFragment.newInstance(data)
            .show(supportFragmentManager, GIFT_CARD_PAYMENT_CONFIRMATION_FRAGMENT_TAG)
    }

    private fun handleOrderResult(order: OrderResponse) {
        adyenLog(AdyenLogLevel.VERBOSE) { "handleOrderResult" }
        dropInViewModel.handleOrderCreated(order)
    }

    override fun requestPartialPayment() {
        dropInViewModel.partialPaymentRequested()
    }

    override fun requestOrderCancellation() {
        dropInViewModel.orderCancellationRequested()
    }

    private fun handleRemovePaymentMethodResult(id: String) {
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

    override fun onRedirect() {
        dropInService?.onRedirectCalled()
    }

    override fun onBinValue(binValue: String) {
        dropInService?.onBinValueCalled(binValue)
    }

    override fun onBinLookup(data: List<BinLookupData>) {
        dropInService?.onBinLookupCalled(data)
    }

    override fun onAddressLookupQuery(query: String) {
        dropInService?.onAddressLookupQueryChangedCalled(query)
    }

    override fun onAddressLookupCompletion(lookupAddress: LookupAddress): Boolean {
        return dropInService?.onAddressLookupCompletionCalled(lookupAddress) ?: false
    }

    private fun showDialog(title: String, message: String, onDismiss: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setOnDismissListener { onDismiss() }
            .setPositiveButton(UICoreR.string.error_dialog_button) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    companion object {
        private const val PRESELECTED_PAYMENT_METHOD_FRAGMENT_TAG = "PRESELECTED_PAYMENT_METHOD_FRAGMENT"
        private const val PAYMENT_METHODS_LIST_FRAGMENT_TAG = "PAYMENT_METHODS_LIST_FRAGMENT"
        private const val COMPONENT_FRAGMENT_TAG = "COMPONENT_DIALOG_FRAGMENT"
        private const val ACTION_FRAGMENT_TAG = "ACTION_DIALOG_FRAGMENT"
        private const val LOADING_FRAGMENT_TAG = "LOADING_DIALOG_FRAGMENT"
        private const val GIFT_CARD_PAYMENT_CONFIRMATION_FRAGMENT_TAG = "GIFT_CARD_PAYMENT_CONFIRMATION_FRAGMENT"

        fun createIntent(
            context: Context,
            checkoutConfiguration: CheckoutConfiguration,
            paymentMethodsApiResponse: PaymentMethodsApiResponse,
            service: ComponentName,
        ): Intent {
            val intent = Intent(context, DropInActivity::class.java)
            DropInBundleHandler.putIntentExtras(
                intent = intent,
                checkoutConfiguration = checkoutConfiguration,
                paymentMethodsApiResponse = paymentMethodsApiResponse,
                service = service,
            )
            return intent
        }

        fun createIntent(
            context: Context,
            checkoutConfiguration: CheckoutConfiguration,
            checkoutSession: CheckoutSession,
            service: ComponentName,
        ): Intent {
            val intent = Intent(context, DropInActivity::class.java)
            DropInBundleHandler.putIntentExtras(
                intent = intent,
                checkoutConfiguration = checkoutConfiguration,
                checkoutSession = checkoutSession,
                service = service,
            )
            return intent
        }
    }
}

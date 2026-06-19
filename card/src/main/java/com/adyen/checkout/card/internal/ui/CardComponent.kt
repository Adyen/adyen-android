/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.card.OnBinChangeCallback
import com.adyen.checkout.card.OnBinLookupCallback
import com.adyen.checkout.card.internal.analytics.CardScannerEvents
import com.adyen.checkout.card.internal.data.api.DetectCardTypeRepository
import com.adyen.checkout.card.internal.helper.toBinLookupData
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.state.CardComponentStateFactory
import com.adyen.checkout.card.internal.ui.state.CardComponentStateReducer
import com.adyen.checkout.card.internal.ui.state.CardComponentStateValidator
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.card.internal.ui.state.CardViewStateProducer
import com.adyen.checkout.card.internal.ui.state.binValue
import com.adyen.checkout.card.internal.ui.state.toPaymentComponentState
import com.adyen.checkout.card.internal.ui.view.CardComponent
import com.adyen.checkout.card.internal.ui.view.InstallmentPicker
import com.adyen.checkout.card.internal.util.CardScannerWrapper
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.SecondaryScreenComponent
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.cse.internal.BaseGenericEncryptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class CardComponent
@Suppress("LongParameterList")
constructor(
    private val analyticsManager: AnalyticsManager,
    private val cardEncryptor: BaseCardEncryptor,
    private val genericEncryptor: BaseGenericEncryptor,
    private val componentParams: CardComponentParams,
    private val detectCardTypeRepository: DetectCardTypeRepository,
    private val componentStateValidator: CardComponentStateValidator,
    componentStateFactory: CardComponentStateFactory,
    componentStateReducer: CardComponentStateReducer,
    viewStateProducer: CardViewStateProducer,
    private val coroutineScope: CoroutineScope,
    private val sdkDataProvider: SdkDataProvider,
    private val paymentMethodType: String,
    private val onBinChangeCallback: OnBinChangeCallback?,
    private val onBinLookupCallback: OnBinLookupCallback?,
    private val cardScannerWrapper: CardScannerWrapper,
    private val publicKey: String?,
    private val environment: Environment,
) : PaymentComponent,
    SecondaryScreenComponent {

    private val eventChannel = bufferedChannel<PaymentComponentEvent>()
    override val eventFlow: Flow<PaymentComponentEvent> = eventChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = componentStateFactory.createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
        coroutineScope = coroutineScope,
    )

    private val viewState = componentState.viewState(viewStateProducer, coroutineScope)

    init {
        initializeAnalytics()
        onCardBrandDataChanged()
        onBinChanged()
    }

    private fun initializeAnalytics() {
        analyticsManager.initialize(this, coroutineScope)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            initializeCardScanner(context)
        }

        val scannerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            onCardScannerResult(result.resultCode, result.data)
            // Re-initialize to get a fresh PendingIntent, as Google's PaymentCardRecognitionPendingIntent is single-use
            initializeCardScanner(context)
        }

        val viewState by viewState.collectAsStateWithLifecycle()
        CardComponent(
            viewState = viewState,
            onIntent = ::handleIntent,
            onSubmitClick = ::submit,
            onScanButtonClick = {
                onScanButtonClick(scannerLauncher)
            },
            onInstallmentPickerClick = ::onInstallmentPickerClick,
            modifier = modifier,
        )
    }

    @Composable
    override fun SecondaryContent(identifier: String, modifier: Modifier) {
        val viewState by viewState.collectAsStateWithLifecycle()

        when (identifier) {
            INSTALLMENTS_IDENTIFIER -> {
                InstallmentPicker(
                    installmentOptions = viewState.installmentViewState?.installmentOptions ?: emptyList(),
                    selectedInstallment = viewState.installmentViewState?.selectedInstallment,
                    onItemClick = { installment ->
                        onIntent(CardIntent.UpdateInstallment(installment))
                        eventChannel.trySend(PaymentComponentEvent.CloseSecondaryScreen)
                    },
                    modifier = modifier,
                )
            }
        }
    }

    override fun submit() {
        if (componentStateValidator.isValid(componentState.value)) {
            val paymentComponentState = componentState.value.toPaymentComponentState(
                publicKey = publicKey,
                componentParams = componentParams,
                cardEncryptor = cardEncryptor,
                genericEncryptor = genericEncryptor,
                sdkDataProvider = sdkDataProvider,
                paymentMethodType = paymentMethodType,
                onEncryptionFailed = ::onEncryptionError,
                onPublicKeyNotFound = ::onPublicKeyNotFound,
            )
            val event = PaymentComponentEvent.Submit(paymentComponentState)
            eventChannel.trySend(event)
        } else {
            onIntent(CardIntent.HighlightValidationErrors)
        }
    }

    override fun requiresUserInteraction(): Boolean = true

    override fun setLoading(isLoading: Boolean) {
        onIntent(CardIntent.UpdateLoading(isLoading))
    }

    override fun onCleared() {
        analyticsManager.clear(this)
    }

    private fun onScanButtonClick(scannerLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        val intentSender = getCardScannerIntentSender() ?: run {
            onCardScannerPresented(didDisplay = false)
            return
        }
        val request = IntentSenderRequest.Builder(intentSender).build()
        scannerLauncher.launch(request)
        onCardScannerPresented(didDisplay = true)
    }

    fun getCardScannerIntentSender(): IntentSender? {
        return cardScannerWrapper.getIntentSender()
    }

    fun onCardScannerPresented(didDisplay: Boolean) {
        val event = if (didDisplay) {
            CardScannerEvents.cardScannerPresented(paymentMethodType)
        } else {
            CardScannerEvents.cardScannerFailure(paymentMethodType)
        }
        analyticsManager.trackEvent(event)
    }

    fun onCardScannerResult(resultCode: Int, intent: Intent?) {
        when (resultCode) {
            Activity.RESULT_CANCELED -> {
                analyticsManager.trackEvent(CardScannerEvents.cardScannerCancelled(paymentMethodType))
            }

            Activity.RESULT_OK -> {
                val scanResult = cardScannerWrapper.parseResult(intent)
                if (scanResult == null) {
                    analyticsManager.trackEvent(CardScannerEvents.cardScannerFailure(paymentMethodType))
                    return
                }

                analyticsManager.trackEvent(CardScannerEvents.cardScannerSuccess(paymentMethodType))
                onIntent(
                    CardIntent.UpdateCardScanResult(
                        pan = scanResult.pan,
                        expiryMonth = scanResult.expiryMonth,
                        expiryYear = scanResult.expiryYear,
                    ),
                )
            }

            else -> {
                analyticsManager.trackEvent(CardScannerEvents.cardScannerFailure(paymentMethodType))
            }
        }
    }

    fun initializeCardScanner(context: Context) {
        if (!componentParams.showCardScanner) return
        coroutineScope.launch {
            val isAvailable = cardScannerWrapper.initialize(
                context = context,
                environment = environment,
            )
            onIntent(CardIntent.UpdateCardScanningAvailability(isAvailable))
            val event = if (isAvailable) {
                CardScannerEvents.cardScannerAvailable(paymentMethodType)
            } else {
                CardScannerEvents.cardScannerUnavailable(paymentMethodType)
            }
            analyticsManager.trackEvent(event)
        }
    }

    private fun onIntent(intent: CardIntent) {
        componentState.handleIntent(intent)
    }

    private fun handleIntent(intent: CardIntent) {
        when (intent) {
            is CardIntent.UpdateCardNumber -> onCardNumberChanged(intent.number)
            else -> onIntent(intent)
        }
    }

    private fun onCardNumberChanged(newCardNumber: String) {
        onIntent(CardIntent.UpdateCardNumber(newCardNumber))
        detectCardTypes(newCardNumber)
    }

    private fun detectCardTypes(cardNumber: String) {
        detectCardTypeRepository.detectCardTypes(cardNumber).onEach { result ->
            onIntent(CardIntent.UpdateDetectedCardTypes(result))
        }.launchIn(coroutineScope)
    }

    private fun onBinChanged() {
        val callback = onBinChangeCallback ?: return
        componentState
            .map { it.binValue }
            .distinctUntilChanged()
            .drop(1)
            .onEach { newBinValue -> callback.onBinChange(newBinValue) }
            .launchIn(coroutineScope)
    }

    private fun onCardBrandDataChanged() {
        val callback = onBinLookupCallback ?: return
        componentState
            .map { it.networkBinLookupState }
            .distinctUntilChanged()
            .mapNotNull { it?.toBinLookupData() }
            .onEach { binLookupData -> callback.onBinLookup(binLookupData) }
            .launchIn(coroutineScope)
    }

    private fun onInstallmentPickerClick() {
        eventChannel.trySend(
            PaymentComponentEvent.SecondaryScreen(
                identifier = INSTALLMENTS_IDENTIFIER,
            ),
        )
    }

    private fun onEncryptionError(e: EncryptionException) {
        val event = GenericEvents.error(paymentMethodType, ErrorEvent.ENCRYPTION)
        analyticsManager.trackEvent(event)

        // TODO - Error propagation. Change after EncryptionException extends from CheckoutError
        emitError(GenericError("Encryption error", e))
    }

    private fun emitError(error: InternalCheckoutError) {
        eventChannel.trySend(
            PaymentComponentEvent.Error(error),
        )
    }

    // TODO - Error propagation. Change after implementation of specific error for this case
    private fun onPublicKeyNotFound(e: InternalCheckoutError) {
        val event = GenericEvents.error(paymentMethodType, ErrorEvent.API_PUBLIC_KEY)
        analyticsManager.trackEvent(event)
        emitError(e)
    }

    companion object {
        private const val INSTALLMENTS_IDENTIFIER = "installments"
    }
}

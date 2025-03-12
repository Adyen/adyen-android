/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/8/2022.
 */

package com.adyen.checkout.adyen3ds2.internal.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.adyen3ds2.Authentication3DS2Exception
import com.adyen.checkout.adyen3ds2.internal.analytics.ThreeDS2Events
import com.adyen.checkout.adyen3ds2.internal.data.api.SubmitFingerprintRepository
import com.adyen.checkout.adyen3ds2.internal.data.model.Adyen3DS2Serializer
import com.adyen.checkout.adyen3ds2.internal.data.model.ChallengeToken
import com.adyen.checkout.adyen3ds2.internal.data.model.FingerprintToken
import com.adyen.checkout.adyen3ds2.internal.data.model.SubmitFingerprintResult
import com.adyen.checkout.adyen3ds2.internal.ui.model.Adyen3DS2ComponentParams
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.BaseThreeds2Action
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.action.Threeds2Action
import com.adyen.checkout.components.core.action.Threeds2ChallengeAction
import com.adyen.checkout.components.core.action.Threeds2FingerprintAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.RedirectHandler
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.threeds2.AuthenticationRequestParameters
import com.adyen.threeds2.ChallengeResult
import com.adyen.threeds2.ChallengeStatusHandler
import com.adyen.threeds2.InitializeResult
import com.adyen.threeds2.ThreeDS2Service
import com.adyen.threeds2.Transaction
import com.adyen.threeds2.TransactionResult
import com.adyen.threeds2.exception.InvalidInputException
import com.adyen.threeds2.exception.SDKNotInitializedException
import com.adyen.threeds2.exception.SDKRuntimeException
import com.adyen.threeds2.parameters.ChallengeParameters
import com.adyen.threeds2.parameters.ConfigParameters
import com.adyen.threeds2.util.AdyenConfigParameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Suppress("TooManyFunctions", "LongParameterList")
internal class DefaultAdyen3DS2Delegate(
    private val observerRepository: ActionObserverRepository,
    override val savedStateHandle: SavedStateHandle,
    override val componentParams: Adyen3DS2ComponentParams,
    private val submitFingerprintRepository: SubmitFingerprintRepository,
    private val paymentDataRepository: PaymentDataRepository,
    private val adyen3DS2Serializer: Adyen3DS2Serializer,
    private val redirectHandler: RedirectHandler,
    private val threeDS2Service: ThreeDS2Service,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val application: Application,
    private val analyticsManager: AnalyticsManager?,
) : Adyen3DS2Delegate, ChallengeStatusHandler, SavedStateHandleContainer {

    private val detailsChannel: Channel<ActionComponentData> = bufferedChannel()
    override val detailsFlow: Flow<ActionComponentData> = detailsChannel.receiveAsFlow()

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(Adyen3DS2ComponentViewType)

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var currentTransaction: Transaction? = null

    private var action: BaseThreeds2Action? by SavedStateHandleProperty(ACTION_KEY)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        SharedChallengeStatusHandler.onCompletionListener = this
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) {
        observerRepository.addObservers(
            detailsFlow = detailsFlow,
            exceptionFlow = exceptionFlow,
            permissionFlow = null,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun handleAction(action: Action, activity: Activity) {
        if (action !is BaseThreeds2Action) {
            emitError(ComponentException("Unsupported action"))
            return
        }

        this.action = action

        val paymentData = action.paymentData
        paymentDataRepository.paymentData = paymentData
        when (action) {
            is Threeds2FingerprintAction -> handleThreeds2FingerprintAction(action, activity)
            is Threeds2ChallengeAction -> handleThreeds2ChallengeAction(action, activity)
            is Threeds2Action -> handleThreeds2Action(action, activity)
        }
    }

    private fun handleThreeds2FingerprintAction(
        action: Threeds2FingerprintAction,
        activity: Activity,
    ) {
        if (action.token.isNullOrEmpty()) {
            trackFingerprintErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_TOKEN_MISSING,
                message = "Token is missing for Threeds2FingerprintAction",
            )
            emitError(ComponentException("Fingerprint token not found."))
            return
        }

        trackFingerprintActionEvent(action)

        identifyShopper(
            activity = activity,
            encodedFingerprintToken = action.token.orEmpty(),
            submitFingerprintAutomatically = false,
        )
    }

    private fun handleThreeds2ChallengeAction(
        action: Threeds2ChallengeAction,
        activity: Activity,
    ) {
        if (action.token.isNullOrEmpty()) {
            trackChallengeErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_TOKEN_MISSING,
                message = "Token is missing for Threeds2ChallengeAction",
            )
            emitError(ComponentException("Challenge token not found."))
            return
        }

        trackChallengeActionEvent(action)

        challengeShopper(activity, action.token.orEmpty())
    }

    private fun handleThreeds2Action(
        action: Threeds2Action,
        activity: Activity,
    ) {
        if (action.subtype == null) {
            emitError(ComponentException("3DS2 Action subtype not found."))
            return
        }
        val subtype = Threeds2Action.SubType.parse(action.subtype.orEmpty())
        handleThreeds2ActionSubtype(action, activity, subtype)
    }

    private fun handleThreeds2ActionSubtype(
        action: Threeds2Action,
        activity: Activity,
        subtype: Threeds2Action.SubType,
    ) {
        val token = action.token
        if (token.isNullOrEmpty()) {
            val errorMessage = "Token is missing for Threeds2Action"
            when (subtype) {
                Threeds2Action.SubType.FINGERPRINT -> trackFingerprintErrorEvent(
                    errorEvent = ErrorEvent.THREEDS2_TOKEN_MISSING,
                    message = errorMessage,
                )

                Threeds2Action.SubType.CHALLENGE -> trackChallengeErrorEvent(
                    errorEvent = ErrorEvent.THREEDS2_TOKEN_MISSING,
                    message = errorMessage,
                )
            }

            emitError(ComponentException("3DS2 token not found."))
            return
        }

        when (subtype) {
            Threeds2Action.SubType.FINGERPRINT -> {
                trackFingerprintActionEvent(action)

                identifyShopper(
                    activity = activity,
                    encodedFingerprintToken = token,
                    submitFingerprintAutomatically = true,
                )
            }

            Threeds2Action.SubType.CHALLENGE -> {
                trackChallengeActionEvent(action)

                challengeShopper(activity, token)
            }
        }
    }

    @Suppress("LongMethod")
    @VisibleForTesting
    internal fun identifyShopper(
        activity: Activity,
        encodedFingerprintToken: String,
        submitFingerprintAutomatically: Boolean,
    ) {
        adyenLog(AdyenLogLevel.DEBUG) {
            "identifyShopper - submitFingerprintAutomatically: $submitFingerprintAutomatically"
        }

        val fingerprintToken = try {
            decodeFingerprintToken(encodedFingerprintToken)
        } catch (e: CheckoutException) {
            trackFingerprintErrorEvent(ErrorEvent.THREEDS2_TOKEN_DECODING)
            emitError(ComponentException("Failed to decode fingerprint token", e))
            return
        }

        val configParameters = createAdyenConfigParameters(fingerprintToken) ?: run {
            trackFingerprintErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_FINGERPRINT_CREATION,
                message = "Fingerprint creation failed because the token is partial",
            )
            emitError(ComponentException("Failed to create ConfigParameters."))
            return
        }

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            adyenLog(AdyenLogLevel.ERROR, throwable) { "Unexpected uncaught 3DS2 Exception" }
            trackFingerprintErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_FINGERPRINT_HANDLING,
                message = "Fingerprint handling failed because of uncaught exception",
            )
            emitError(CheckoutException("Unexpected 3DS2 exception.", throwable))
        }

        coroutineScope.launch(coroutineDispatcher + coroutineExceptionHandler) {
            // This makes sure the 3DS2 SDK doesn't re-use any state from previous transactions
            closeTransaction()

            adyenLog(AdyenLogLevel.DEBUG) { "initialize 3DS2 SDK" }
            val initializeResult =
                threeDS2Service.initialize(
                    activity,
                    configParameters,
                    null,
                    componentParams.uiCustomization,
                )

            if (initializeResult is InitializeResult.Failure) {
                val details = makeDetails(
                    initializeResult.transactionStatus,
                    initializeResult.additionalDetails,
                )
                emitDetails(details)
                return@launch
            }

            currentTransaction = createTransaction(fingerprintToken) ?: return@launch

            val authenticationRequestParameters =
                currentTransaction?.authenticationRequestParameters
            if (authenticationRequestParameters == null) {
                trackFingerprintErrorEvent(
                    errorEvent = ErrorEvent.THREEDS2_FINGERPRINT_CREATION,
                    message = "Fingerprint creation failed because authentication parameters do not exist",
                )
                emitError(ComponentException("Failed to retrieve 3DS2 authentication parameters"))
                return@launch
            }
            val encodedFingerprint = createEncodedFingerprint(authenticationRequestParameters)

            if (submitFingerprintAutomatically) {
                submitFingerprintAutomatically(activity, encodedFingerprint)
            } else {
                emitDetails(adyen3DS2Serializer.createFingerprintDetails(encodedFingerprint))
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Throws(ComponentException::class, ModelSerializationException::class)
    private fun decodeFingerprintToken(encoded: String): FingerprintToken {
        val decodedFingerprintToken = Base64.decode(encoded).toString(Charsets.UTF_8)

        val fingerprintJson: JSONObject = try {
            JSONObject(decodedFingerprintToken)
        } catch (e: JSONException) {
            throw ComponentException("JSON parsing of FingerprintToken failed", e)
        }

        return FingerprintToken.SERIALIZER.deserialize(fingerprintJson)
    }

    @Suppress("DestructuringDeclarationWithTooManyEntries")
    private fun createAdyenConfigParameters(
        fingerprintToken: FingerprintToken
    ): ConfigParameters? {
        val (directoryServerId, directoryServerPublicKey, directoryServerRootCertificates, _, _) = fingerprintToken

        if (directoryServerId == null || directoryServerPublicKey == null || directoryServerRootCertificates == null) {
            adyenLog(AdyenLogLevel.DEBUG) {
                "directoryServerId, directoryServerPublicKey or directoryServerRootCertificates is null."
            }
            return null
        }

        return AdyenConfigParameters.Builder(
            directoryServerId,
            directoryServerPublicKey,
            directoryServerRootCertificates,
        )
            .deviceParameterBlockList(componentParams.deviceParameterBlockList)
            .build()
    }

    private fun createTransaction(fingerprintToken: FingerprintToken): Transaction? {
        if (fingerprintToken.threeDSMessageVersion == null) {
            trackFingerprintErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_TRANSACTION_CREATION,
                message = "Transaction creation failed because threeDSMessageVersion is missing",
            )

            val error = "Failed to create 3DS2 Transaction. Missing threeDSMessageVersion inside fingerprintToken."
            emitError(ComponentException(error))

            return null
        }

        val event = ThreeDS2Events.threeDS2Fingerprint(
            subType = ThreeDS2Events.SubType.FINGERPRINT_DATA_SENT,
        )
        analyticsManager?.trackEvent(event)

        return try {
            adyenLog(AdyenLogLevel.DEBUG) { "create transaction" }
            val result =
                threeDS2Service.createTransaction(null, fingerprintToken.threeDSMessageVersion)
            when (result) {
                is TransactionResult.Failure -> {
                    val details = makeDetails(result.transactionStatus, result.additionalDetails)
                    emitDetails(details)
                    null
                }

                is TransactionResult.Success -> result.transaction
            }
        } catch (e: SDKNotInitializedException) {
            trackFingerprintErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_TRANSACTION_CREATION,
                message = "Transaction creation failed because the SDK is not initialized",
            )
            emitError(ComponentException("Failed to create 3DS2 Transaction", e))
            null
        } catch (e: SDKRuntimeException) {
            trackFingerprintErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_TRANSACTION_CREATION,
                message = "Transaction creation failed because SDK threw runtime exception",
            )
            emitError(ComponentException("Failed to create 3DS2 Transaction", e))
            null
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Throws(ComponentException::class)
    private fun createEncodedFingerprint(authenticationRequestParameters: AuthenticationRequestParameters): String {
        return try {
            val fingerprintJson = JSONObject().apply {
                with(authenticationRequestParameters) {
                    put("sdkAppID", sdkAppID)
                    put("sdkEncData", deviceData)
                    put("sdkEphemPubKey", JSONObject(sdkEphemeralPublicKey))
                    put("sdkReferenceNumber", sdkReferenceNumber)
                    put("sdkTransID", sdkTransactionID)
                    put("messageVersion", messageVersion)
                }
            }

            Base64.encode(fingerprintJson.toString().toByteArray())
        } catch (e: JSONException) {
            throw ComponentException("Failed to create encoded fingerprint", e)
        }
    }

    private suspend fun submitFingerprintAutomatically(
        activity: Activity,
        encodedFingerprint: String,
    ) {
        submitFingerprintRepository.submitFingerprint(
            encodedFingerprint,
            componentParams.clientKey,
            paymentDataRepository.paymentData,
        )
            .fold(
                onSuccess = { result -> onSubmitFingerprintResult(result, activity) },
                onFailure = { e ->
                    trackFingerprintErrorEvent(ErrorEvent.API_THREEDS2)
                    emitError(ComponentException("Unable to submit fingerprint", e))
                },
            )
    }

    private fun onSubmitFingerprintResult(result: SubmitFingerprintResult, activity: Activity) {
        // This flow (calling the internal submitFingerprint endpoint) requires that we do not send paymentData
        // back to the merchant. Setting it to null ensures that when the flow ends and notifyDetails is called,
        // paymentData will not be included in the response.
        paymentDataRepository.paymentData = null

        when (result) {
            is SubmitFingerprintResult.Completed -> {
                trackFingerprintCompletedEvent(ThreeDS2Events.Result.COMPLETED)
                emitDetails(result.details)
            }

            is SubmitFingerprintResult.Redirect -> {
                trackFingerprintCompletedEvent(ThreeDS2Events.Result.REDIRECT)
                makeRedirect(activity, result.action)
            }

            is SubmitFingerprintResult.Threeds2 -> {
                trackFingerprintCompletedEvent(ThreeDS2Events.Result.THREEDS2)
                handleAction(result.action, activity)
            }
        }
    }

    private fun trackFingerprintCompletedEvent(result: ThreeDS2Events.Result) {
        val event = ThreeDS2Events.threeDS2Fingerprint(
            subType = ThreeDS2Events.SubType.FINGERPRINT_COMPLETED,
            result = result,
        )
        analyticsManager?.trackEvent(event)
    }

    private fun makeRedirect(activity: Activity, action: RedirectAction) {
        val url = action.url
        try {
            adyenLog(AdyenLogLevel.DEBUG) { "makeRedirect - $url" }
            redirectHandler.launchUriRedirect(activity, url)
        } catch (e: CheckoutException) {
            emitError(e)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    @VisibleForTesting
    internal fun challengeShopper(activity: Activity, encodedChallengeToken: String) {
        adyenLog(AdyenLogLevel.DEBUG) { "challengeShopper" }

        if (currentTransaction == null) {
            trackChallengeErrorEvent(ErrorEvent.THREEDS2_TRANSACTION_MISSING)
            emitError(
                Authentication3DS2Exception("Failed to make challenge, missing reference to initial transaction."),
            )
            return
        }

        val decodedChallengeToken = Base64.decode(encodedChallengeToken).toString(Charsets.UTF_8)
        val challengeTokenJson: JSONObject = try {
            JSONObject(decodedChallengeToken)
        } catch (e: JSONException) {
            trackChallengeErrorEvent(ErrorEvent.THREEDS2_TOKEN_DECODING)
            emitError(ComponentException("JSON parsing of challenge token failed", e))
            return
        }

        val challengeSentEvent = ThreeDS2Events.threeDS2Challenge(
            subType = ThreeDS2Events.SubType.CHALLENGE_DATA_SENT,
        )
        analyticsManager?.trackEvent(challengeSentEvent)

        val challengeToken = ChallengeToken.SERIALIZER.deserialize(challengeTokenJson)
        val challengeParameters = createChallengeParameters(challengeToken)
        try {
            currentTransaction?.doChallenge(
                activity,
                challengeParameters,
                SharedChallengeStatusHandler,
                DEFAULT_CHALLENGE_TIME_OUT,
            )

            val challengeDisplayedEvent = ThreeDS2Events.threeDS2Challenge(
                subType = ThreeDS2Events.SubType.CHALLENGE_DISPLAYED,
            )
            analyticsManager?.trackEvent(challengeDisplayedEvent)
        } catch (e: InvalidInputException) {
            trackChallengeErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
                message = "Challenge failed because input is invalid",
            )
            emitError(CheckoutException("Error starting challenge", e))
        }
    }

    private fun createChallengeParameters(challenge: ChallengeToken): ChallengeParameters {
        return ChallengeParameters().apply {
            set3DSServerTransactionID(challenge.threeDSServerTransID)
            acsTransactionID = challenge.acsTransID
            acsRefNumber = challenge.acsReferenceNumber
            acsSignedContent = challenge.acsSignedContent
            // This field was introduced in version 2.2.0 so older protocols don't expect it to be present and might
            // throw an error.
            if (challenge.messageVersion != PROTOCOL_VERSION_2_1_0) {
                threeDSRequestorAppURL = componentParams.threeDSRequestorAppURL
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        try {
            val parsedResult = redirectHandler.parseRedirectResult(intent.data)
            emitDetails(parsedResult)
        } catch (e: CheckoutException) {
            emitError(e)
        }
    }

    private fun onCompleted(transactionStatus: String) {
        adyenLog(AdyenLogLevel.DEBUG) { "challenge completed" }
        try {
            val details = makeDetails(transactionStatus)
            emitDetails(details)
        } catch (e: CheckoutException) {
            trackChallengeErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
                message = "Challenge completed and details cannot be created",
            )
            emitError(e)
        } finally {
            closeTransaction()
        }
    }

    private fun onCancelled(result: ChallengeResult.Cancelled) {
        adyenLog(AdyenLogLevel.DEBUG) { "challenge cancelled" }
        try {
            val details = makeDetails(result.transactionStatus, result.additionalDetails)
            emitDetails(details)
        } catch (e: CheckoutException) {
            trackChallengeErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
                message = "Challenge is cancelled and details cannot be created",
            )
            emitError(e)
        } finally {
            closeTransaction()
        }
    }

    private fun onTimeout(result: ChallengeResult.Timeout) {
        adyenLog(AdyenLogLevel.DEBUG) { "challenge timed out" }
        try {
            val details = makeDetails(result.transactionStatus, result.additionalDetails)
            emitDetails(details)
        } catch (e: CheckoutException) {
            trackChallengeErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
                message = "Challenge timed out and details cannot be created",
            )
            emitError(e)
        } finally {
            closeTransaction()
        }
    }

    private fun onError(result: ChallengeResult.Error) {
        adyenLog(AdyenLogLevel.DEBUG) { "challenge error" }
        try {
            val details = makeDetails(result.transactionStatus, result.additionalDetails)
            emitDetails(details)
        } catch (e: CheckoutException) {
            trackChallengeErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
                message = "Challenge failed and details cannot be created",
            )
            emitError(e)
        } finally {
            closeTransaction()
        }
    }

    override fun onCompletion(result: ChallengeResult) {
        when (result) {
            is ChallengeResult.Cancelled -> {
                trackChallengeCompletedEvent(ThreeDS2Events.Result.CANCELLED)
                onCancelled(result)
            }

            is ChallengeResult.Completed -> {
                trackChallengeCompletedEvent(ThreeDS2Events.Result.COMPLETED)
                onCompleted(result.transactionStatus)
            }

            is ChallengeResult.Error -> {
                trackChallengeCompletedEvent(ThreeDS2Events.Result.ERROR)
                onError(result)
            }

            is ChallengeResult.Timeout -> {
                trackChallengeCompletedEvent(ThreeDS2Events.Result.TIMEOUT)
                onTimeout(result)
            }
        }
    }

    private fun trackChallengeCompletedEvent(result: ThreeDS2Events.Result) {
        val event = ThreeDS2Events.threeDS2Challenge(
            subType = ThreeDS2Events.SubType.CHALLENGE_COMPLETED,
            result = result,
        )
        analyticsManager?.trackEvent(event)
    }

    private fun trackFingerprintActionEvent(action: Action) =
        trackActionEvent(action, "Fingerprint action was handled by the SDK")

    private fun trackChallengeActionEvent(action: Action) =
        trackActionEvent(action, "Challenge action was handled by the SDK")

    private fun trackActionEvent(action: Action, message: String) {
        val event = GenericEvents.action(
            component = action.paymentMethodType.orEmpty(),
            subType = action.type.orEmpty(),
            message = message,
        )
        analyticsManager?.trackEvent(event)
    }

    private fun trackFingerprintErrorEvent(errorEvent: ErrorEvent, message: String? = null) {
        val event = ThreeDS2Events.threeDS2FingerprintError(errorEvent, message)
        analyticsManager?.trackEvent(event)
    }

    private fun trackChallengeErrorEvent(errorEvent: ErrorEvent, message: String? = null) {
        val event = ThreeDS2Events.threeDS2ChallengeError(errorEvent, message)
        analyticsManager?.trackEvent(event)
    }

    private fun closeTransaction() {
        currentTransaction?.close()
        currentTransaction = null
        cleanUp3DS2()
    }

    private fun cleanUp3DS2() {
        @Suppress("SwallowedException")
        try {
            threeDS2Service.cleanup(application)
        } catch (e: SDKNotInitializedException) {
            // Safe to ignore
        }
    }

    override fun onError(e: CheckoutException) {
        emitError(e)
    }

    override fun setOnRedirectListener(listener: () -> Unit) {
        redirectHandler.setOnRedirectListener(listener)
    }

    private fun emitError(e: CheckoutException) {
        exceptionChannel.trySend(e)
        clearState()
    }

    private fun emitDetails(details: JSONObject) {
        val actionComponentData = ActionComponentData(
            details = details,
            paymentData = paymentDataRepository.paymentData,
        )
        detailsChannel.trySend(actionComponentData)
        clearState()
    }

    private fun makeDetails(transactionStatus: String, errorDetails: String? = null): JSONObject {
        // Check whether authorizationToken was set and create the corresponding details object
        val token = (action as? Threeds2Action)?.authorisationToken
        return if (token == null) {
            adyen3DS2Serializer.createChallengeDetails(
                transactionStatus = transactionStatus,
                errorDetails = errorDetails,
            )
        } else {
            adyen3DS2Serializer.createThreeDsResultDetails(
                transactionStatus = transactionStatus,
                errorDetails = errorDetails,
                authorisationToken = token,
            )
        }
    }

    private fun clearState() {
        action = null
        SharedChallengeStatusHandler.reset()
    }

    override fun onCleared() {
        removeObserver()
        SharedChallengeStatusHandler.onCompletionListener = null
        _coroutineScope = null
        redirectHandler.removeOnRedirectListener()
    }

    companion object {
        private const val DEFAULT_CHALLENGE_TIME_OUT = 10
        private const val PROTOCOL_VERSION_2_1_0 = "2.1.0"

        @VisibleForTesting
        internal const val ACTION_KEY = "ACTION_KEY"
    }
}

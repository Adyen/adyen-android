/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/12/2025.
 */

package com.adyen.checkout.authentication.internal.ui

import android.app.Activity
import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.authentication.internal.analytics.AuthenticationEvents
import com.adyen.checkout.authentication.internal.data.api.SubmitFingerprintRepository
import com.adyen.checkout.authentication.internal.data.model.AuthenticationSerializer
import com.adyen.checkout.authentication.internal.data.model.ChallengeToken
import com.adyen.checkout.authentication.internal.data.model.FingerprintToken
import com.adyen.checkout.authentication.internal.data.model.SubmitFingerprintResult
import com.adyen.checkout.authentication.internal.ui.model.AuthenticationComponentParams
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.data.Threeds2Action
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.SavedStateHandleContainer
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.core.redirect.internal.RedirectHandler
import com.adyen.threeds2.AuthenticationRequestParameters
import com.adyen.threeds2.ChallengeResult
import com.adyen.threeds2.ChallengeStatusHandler
import com.adyen.threeds2.InitializeResult
import com.adyen.threeds2.ThreeDS2Service
import com.adyen.threeds2.Transaction
import com.adyen.threeds2.TransactionResult
import com.adyen.threeds2.customization.UiCustomization
import com.adyen.threeds2.exception.InvalidInputException
import com.adyen.threeds2.exception.SDKNotInitializedException
import com.adyen.threeds2.exception.SDKRuntimeException
import com.adyen.threeds2.parameters.ChallengeParameters
import com.adyen.threeds2.parameters.ConfigParameters
import com.adyen.threeds2.util.AdyenConfigParameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Suppress("TooManyFunctions", "LargeClass")
internal class AuthenticationComponent
@Suppress("LongParameterList")
constructor(
    private val action: Action,
    private val componentParams: AuthenticationComponentParams,
    override val savedStateHandle: SavedStateHandle,
    private val analyticsManager: AnalyticsManager,
    private val redirectHandler: RedirectHandler,
    private val authenticationSerializer: AuthenticationSerializer,
    private val threeDS2Service: ThreeDS2Service,
    private val submitFingerprintRepository: SubmitFingerprintRepository,
    private val paymentDataRepository: PaymentDataRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val application: Application,
    private val clientKey: String,
) : ActionComponent, ChallengeStatusHandler, SavedStateHandleContainer {

    private val eventChannel = bufferedChannel<ActionComponentEvent>()
    override val eventFlow: Flow<ActionComponentEvent> = eventChannel.receiveAsFlow()

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var currentTransaction: Transaction? = null

    private val authenticationEventChannel = bufferedChannel<AuthenticationEvent>()
    private val authenticationEventFlow: Flow<AuthenticationEvent> = authenticationEventChannel.receiveAsFlow()

    @Composable
    override fun Content(modifier: Modifier) {
        AuthenticationEventEffect(
            handleAction = ::handleAction,
            viewEventFlow = authenticationEventFlow,
            onError = ::emitError,
        )
    }

    fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        SharedChallengeStatusHandler.onCompletionListener = this
    }

    override fun handleAction() {
        authenticationEventChannel.trySend(AuthenticationEvent.HandleAction)
    }

    private fun handleAction(activity: Activity, uiCustomization: UiCustomization) {
        handleAction(action, activity, uiCustomization)
    }

    private fun handleAction(action: Action, activity: Activity, uiCustomization: UiCustomization) {
        if (action !is Threeds2Action) {
            emitError(
                GenericError("Unsupported action"),
            )
            return
        }

        val paymentData = action.paymentData
        paymentDataRepository.paymentData = paymentData
        handleThreeds2Action(action, activity, uiCustomization)
    }

    private fun handleThreeds2ActionSubtype(
        action: Threeds2Action,
        activity: Activity,
        subtype: Threeds2Action.SubType,
        uiCustomization: UiCustomization,
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

            emitError(
                GenericError("3DS2 token not found."),
            )
            return
        }

        when (subtype) {
            Threeds2Action.SubType.FINGERPRINT -> {
                trackFingerprintActionEvent(action)

                identifyShopper(
                    activity = activity,
                    encodedFingerprintToken = token,
                    submitFingerprintAutomatically = true,
                    uiCustomization = uiCustomization,
                )
            }

            Threeds2Action.SubType.CHALLENGE -> {
                trackChallengeActionEvent(action)

                challengeShopper(activity, token)
            }
        }
    }

    private fun handleThreeds2Action(
        action: Threeds2Action,
        activity: Activity,
        uiCustomization: UiCustomization,
    ) {
        if (action.subtype == null) {
            emitError(
                GenericError("3DS2 Action subtype not found."),
            )
            return
        }
        val subtype = Threeds2Action.SubType.parse(action.subtype.orEmpty())
        handleThreeds2ActionSubtype(action, activity, subtype, uiCustomization)
    }

    @Suppress("LongMethod", "TooGenericExceptionCaught")
    @VisibleForTesting
    internal fun identifyShopper(
        activity: Activity,
        encodedFingerprintToken: String,
        submitFingerprintAutomatically: Boolean,
        uiCustomization: UiCustomization,
    ) {
        adyenLog(AdyenLogLevel.DEBUG) {
            "identifyShopper - submitFingerprintAutomatically: $submitFingerprintAutomatically"
        }

        val fingerprintToken = try {
            decodeFingerprintToken(encodedFingerprintToken)
        } catch (e: RuntimeException) {
            trackFingerprintErrorEvent(ErrorEvent.THREEDS2_TOKEN_DECODING)
            emitError(
                GenericError(
                    message = "Failed to decode fingerprint token",
                    cause = e,
                ),
            )
            return
        }

        val configParameters = createAdyenConfigParameters(fingerprintToken) ?: run {
            trackFingerprintErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_FINGERPRINT_CREATION,
                message = "Fingerprint creation failed because the token is partial",
            )
            emitError(
                GenericError("Failed to create ConfigParameters."),
            )
            return
        }

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            adyenLog(AdyenLogLevel.ERROR, throwable) { "Unexpected uncaught 3DS2 Exception" }
            trackFingerprintErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_FINGERPRINT_HANDLING,
                message = "Fingerprint handling failed because of uncaught exception",
            )
            emitError(
                GenericError(
                    message = "Unexpected 3DS2 exception.",
                    cause = throwable,
                ),
            )
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
                    uiCustomization,
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
                emitError(
                    GenericError("Failed to retrieve 3DS2 authentication parameters"),
                )
                return@launch
            }
            val encodedFingerprint = createEncodedFingerprint(authenticationRequestParameters)

            if (submitFingerprintAutomatically) {
                submitFingerprintAutomatically(activity, encodedFingerprint, uiCustomization)
            } else {
                emitDetails(
                    authenticationSerializer.createFingerprintDetails(encodedFingerprint),
                    shouldClearState = false,
                )
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Throws(RuntimeException::class, JSONException::class)
    private fun decodeFingerprintToken(encoded: String): FingerprintToken {
        val decodedFingerprintToken = Base64.decode(encoded).toString(Charsets.UTF_8)

        @Suppress("TooGenericExceptionThrown")
        val fingerprintJson: JSONObject = try {
            JSONObject(decodedFingerprintToken)
        } catch (e: JSONException) {
            throw RuntimeException("JSON parsing of FingerprintToken failed", e)
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
            emitError(
                GenericError(
                    message = "Failed to create 3DS2 Transaction. " +
                        "Missing threeDSMessageVersion inside fingerprintToken.",
                ),
            )
            return null
        }

        val event = AuthenticationEvents.threeDS2Fingerprint(
            subType = AuthenticationEvents.SubType.FINGERPRINT_DATA_SENT,
        )
        analyticsManager.trackEvent(event)

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
            emitError(
                GenericError(
                    message = "Failed to create 3DS2 Transaction",
                    cause = e,
                ),
            )
            null
        } catch (e: SDKRuntimeException) {
            trackFingerprintErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_TRANSACTION_CREATION,
                message = "Transaction creation failed because SDK threw runtime exception",
            )
            emitError(
                GenericError(
                    message = "Failed to create 3DS2 Transaction",
                    cause = e,
                ),
            )
            null
        }
    }

    @Suppress("TooGenericExceptionThrown")
    @OptIn(ExperimentalEncodingApi::class)
    @Throws(RuntimeException::class)
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
            throw RuntimeException("Failed to create encoded fingerprint", e)
        }
    }

    private suspend fun submitFingerprintAutomatically(
        activity: Activity,
        encodedFingerprint: String,
        uiCustomization: UiCustomization,
    ) {
        submitFingerprintRepository.submitFingerprint(
            encodedFingerprint = encodedFingerprint,
            clientKey = clientKey,
            paymentData = paymentDataRepository.paymentData,
        )
            .fold(
                onSuccess = { result -> onSubmitFingerprintResult(result, activity, uiCustomization) },
                onFailure = { e ->
                    trackFingerprintErrorEvent(ErrorEvent.API_THREEDS2)
                    emitError(
                        GenericError(
                            message = "Unable to submit fingerprint",
                            cause = e,
                        ),
                    )
                },
            )
    }

    private fun onSubmitFingerprintResult(
        result: SubmitFingerprintResult,
        activity: Activity,
        uiCustomization: UiCustomization,
    ) {
        // This flow (calling the internal submitFingerprint endpoint) requires that we do not send paymentData
        // back to the merchant. Setting it to null ensures that when the flow ends and notifyDetails is called,
        // paymentData will not be included in the response.
        paymentDataRepository.paymentData = null

        when (result) {
            is SubmitFingerprintResult.Completed -> {
                trackFingerprintCompletedEvent(AuthenticationEvents.Result.COMPLETED)
                emitDetails(result.details)
            }

            is SubmitFingerprintResult.Redirect -> {
                trackFingerprintCompletedEvent(AuthenticationEvents.Result.REDIRECT)
                makeRedirect(activity, result.action)
            }

            is SubmitFingerprintResult.Threeds2 -> {
                trackFingerprintCompletedEvent(AuthenticationEvents.Result.THREEDS2)
                handleAction(result.action, activity, uiCustomization)
            }
        }
    }

    private fun trackFingerprintCompletedEvent(result: AuthenticationEvents.Result) {
        val event = AuthenticationEvents.threeDS2Fingerprint(
            subType = AuthenticationEvents.SubType.FINGERPRINT_COMPLETED,
            result = result,
        )
        analyticsManager.trackEvent(event)
    }

    @OptIn(ExperimentalEncodingApi::class)
    @VisibleForTesting
    internal fun challengeShopper(activity: Activity, encodedChallengeToken: String) {
        adyenLog(AdyenLogLevel.DEBUG) { "challengeShopper" }

        if (currentTransaction == null) {
            trackChallengeErrorEvent(ErrorEvent.THREEDS2_TRANSACTION_MISSING)
            emitError(
                GenericError("Failed to make challenge, missing reference to initial transaction."),
            )
            return
        }

        val decodedChallengeToken = Base64.decode(encodedChallengeToken).toString(Charsets.UTF_8)
        val challengeTokenJson: JSONObject = try {
            JSONObject(decodedChallengeToken)
        } catch (e: JSONException) {
            trackChallengeErrorEvent(ErrorEvent.THREEDS2_TOKEN_DECODING)
            emitError(
                GenericError(
                    message = "JSON parsing of challenge token failed",
                    cause = e,
                ),
            )
            return
        }

        val challengeSentEvent = AuthenticationEvents.threeDS2Challenge(
            subType = AuthenticationEvents.SubType.CHALLENGE_DATA_SENT,
        )
        analyticsManager.trackEvent(challengeSentEvent)

        val challengeToken = ChallengeToken.SERIALIZER.deserialize(challengeTokenJson)
        val challengeParameters = createChallengeParameters(challengeToken)
        try {
            currentTransaction?.doChallenge(
                activity,
                challengeParameters,
                SharedChallengeStatusHandler,
                DEFAULT_CHALLENGE_TIME_OUT,
            )

            val challengeDisplayedEvent = AuthenticationEvents.threeDS2Challenge(
                subType = AuthenticationEvents.SubType.CHALLENGE_DISPLAYED,
            )
            analyticsManager.trackEvent(challengeDisplayedEvent)
        } catch (e: InvalidInputException) {
            trackChallengeErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
                message = "Challenge failed because input is invalid",
            )
            emitError(
                GenericError(
                    message = "Error starting challenge",
                    cause = e,
                ),
            )
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

    private fun makeRedirect(activity: Activity, action: RedirectAction) {
        val url = action.url
        try {
            adyenLog(AdyenLogLevel.DEBUG) { "makeRedirect - $url" }
            redirectHandler.launchUriRedirect(activity, url.orEmpty())
        } catch (e: InternalCheckoutError) {
            emitError(
                GenericError(
                    message = e.message ?: "Redirect failed",
                    cause = e,
                ),
            )
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun onCompleted(transactionStatus: String) {
        adyenLog(AdyenLogLevel.DEBUG) { "challenge completed" }
        try {
            val details = makeDetails(transactionStatus)
            emitDetails(details)
        } catch (e: RuntimeException) {
            trackChallengeErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
                message = "Challenge completed and details cannot be created",
            )
            emitError(
                GenericError(
                    message = "Challenge completed and details cannot be created",
                    cause = e,
                ),
            )
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun onCancelled(result: ChallengeResult.Cancelled) {
        adyenLog(AdyenLogLevel.DEBUG) { "challenge cancelled" }
        try {
            val details = makeDetails(result.transactionStatus, result.additionalDetails)
            emitDetails(details)
        } catch (e: RuntimeException) {
            trackChallengeErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
                message = "Challenge is cancelled and details cannot be created",
            )
            emitError(
                GenericError(
                    message = "Challenge is cancelled and details cannot be created",
                    cause = e,
                ),
            )
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun onTimeout(result: ChallengeResult.Timeout) {
        adyenLog(AdyenLogLevel.DEBUG) { "challenge timed out" }
        try {
            val details = makeDetails(result.transactionStatus, result.additionalDetails)
            emitDetails(details)
        } catch (e: RuntimeException) {
            trackChallengeErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
                message = "Challenge timed out and details cannot be created",
            )
            emitError(
                GenericError(
                    message = "Challenge timed out and details cannot be created",
                    cause = e,
                ),
            )
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun onError(result: ChallengeResult.Error) {
        adyenLog(AdyenLogLevel.DEBUG) { "challenge error" }
        try {
            val details = makeDetails(result.transactionStatus, result.additionalDetails)
            emitDetails(details)
        } catch (e: RuntimeException) {
            trackChallengeErrorEvent(
                errorEvent = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
                message = "Challenge failed and details cannot be created",
            )
            emitError(
                GenericError(
                    message = "Challenge failed and details cannot be created",
                    cause = e,
                ),
            )
        }
    }

    override fun onCompletion(result: ChallengeResult) {
        when (result) {
            is ChallengeResult.Cancelled -> {
                trackChallengeCompletedEvent(AuthenticationEvents.Result.CANCELLED)
                onCancelled(result)
            }

            is ChallengeResult.Completed -> {
                trackChallengeCompletedEvent(AuthenticationEvents.Result.COMPLETED)
                onCompleted(result.transactionStatus)
            }

            is ChallengeResult.Error -> {
                trackChallengeCompletedEvent(AuthenticationEvents.Result.ERROR)
                onError(result)
            }

            is ChallengeResult.Timeout -> {
                trackChallengeCompletedEvent(AuthenticationEvents.Result.TIMEOUT)
                onTimeout(result)
            }
        }
    }

    private fun trackChallengeCompletedEvent(result: AuthenticationEvents.Result) {
        val event = AuthenticationEvents.threeDS2Challenge(
            subType = AuthenticationEvents.SubType.CHALLENGE_COMPLETED,
            result = result,
        )
        analyticsManager.trackEvent(event)
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
        analyticsManager.trackEvent(event)
    }

    private fun trackFingerprintErrorEvent(errorEvent: ErrorEvent, message: String? = null) {
        val event = AuthenticationEvents.threeDS2FingerprintError(errorEvent, message)
        analyticsManager.trackEvent(event)
    }

    private fun trackChallengeErrorEvent(errorEvent: ErrorEvent, message: String? = null) {
        val event = AuthenticationEvents.threeDS2ChallengeError(errorEvent, message)
        analyticsManager.trackEvent(event)
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
        } catch (_: SDKNotInitializedException) {
            // Safe to ignore
        }
    }

    private fun makeDetails(transactionStatus: String, errorDetails: String? = null): JSONObject {
        // Check whether authorizationToken was set and create the corresponding details object
        val token = (action as? Threeds2Action)?.authorisationToken
        return if (token == null) {
            authenticationSerializer.createChallengeDetails(
                transactionStatus = transactionStatus,
                errorDetails = errorDetails,
            )
        } else {
            authenticationSerializer.createThreeDsResultDetails(
                transactionStatus = transactionStatus,
                errorDetails = errorDetails,
                authorisationToken = token,
            )
        }
    }

    private fun emitDetails(details: JSONObject, shouldClearState: Boolean = true) {
        val actionComponentData = ActionComponentData(
            details = details,
            paymentData = paymentDataRepository.paymentData,
        )
        eventChannel.trySend(ActionComponentEvent.ActionDetails(actionComponentData))

        if (shouldClearState) {
            clearState()
        }
    }

    internal fun emitError(error: InternalCheckoutError) {
        eventChannel.trySend(
            ActionComponentEvent.Error(error),
        )
    }

    private fun clearState() {
        SharedChallengeStatusHandler.reset()
        closeTransaction()
    }

    companion object {
        private const val DEFAULT_CHALLENGE_TIME_OUT = 10
        private const val PROTOCOL_VERSION_2_1_0 = "2.1.0"
    }
}

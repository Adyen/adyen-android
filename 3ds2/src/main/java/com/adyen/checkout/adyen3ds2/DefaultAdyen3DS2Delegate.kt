/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/8/2022.
 */

package com.adyen.checkout.adyen3ds2

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.adyen3ds2.exception.Authentication3DS2Exception
import com.adyen.checkout.adyen3ds2.exception.Cancelled3DS2Exception
import com.adyen.checkout.adyen3ds2.model.ChallengeToken
import com.adyen.checkout.adyen3ds2.model.FingerprintToken
import com.adyen.checkout.adyen3ds2.repository.SubmitFingerprintRepository
import com.adyen.checkout.adyen3ds2.repository.SubmitFingerprintResult
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.encoding.Base64Encoder
import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.components.model.payments.response.BaseThreeds2Action
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.model.payments.response.Threeds2FingerprintAction
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.redirect.handler.RedirectHandler
import com.adyen.threeds2.AuthenticationRequestParameters
import com.adyen.threeds2.ChallengeStatusReceiver
import com.adyen.threeds2.CompletionEvent
import com.adyen.threeds2.ProtocolErrorEvent
import com.adyen.threeds2.RuntimeErrorEvent
import com.adyen.threeds2.ThreeDS2Service
import com.adyen.threeds2.Transaction
import com.adyen.threeds2.customization.UiCustomization
import com.adyen.threeds2.exception.InvalidInputException
import com.adyen.threeds2.exception.SDKAlreadyInitializedException
import com.adyen.threeds2.exception.SDKNotInitializedException
import com.adyen.threeds2.exception.SDKRuntimeException
import com.adyen.threeds2.parameters.ChallengeParameters
import com.adyen.threeds2.util.AdyenConfigParameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

@Suppress("TooManyFunctions", "LongParameterList")
internal class DefaultAdyen3DS2Delegate(
    private val savedStateHandle: SavedStateHandle,
    private val configuration: Adyen3DS2Configuration,
    private val submitFingerprintRepository: SubmitFingerprintRepository,
    private val paymentDataRepository: PaymentDataRepository,
    private val adyen3DS2Serializer: Adyen3DS2Serializer,
    private val redirectHandler: RedirectHandler,
    private val threeDS2Service: ThreeDS2Service,
    private val defaultDispatcher: CoroutineDispatcher,
    private val embeddedRequestorAppUrl: String,
    private val base64Encoder: Base64Encoder,
    private val application: Application,
) : Adyen3DS2Delegate, ChallengeStatusReceiver {

    private val _detailsFlow = MutableSingleEventSharedFlow<ActionComponentData>()
    override val detailsFlow: Flow<ActionComponentData> = _detailsFlow

    private val _exceptionFlow = MutableSingleEventSharedFlow<CheckoutException>()
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var uiCustomization: UiCustomization? = null

    private var currentTransaction: Transaction? = null

    private var authorizationToken: String?
        get() = savedStateHandle[AUTHORIZATION_TOKEN_KEY]
        set(value) {
            savedStateHandle[AUTHORIZATION_TOKEN_KEY] = value
        }

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
    }

    @Suppress("ReturnCount")
    override fun handleAction(action: BaseThreeds2Action, activity: Activity) {
        val paymentData = action.paymentData
        paymentDataRepository.paymentData = paymentData
        when (action) {
            is Threeds2FingerprintAction -> {
                if (action.token.isNullOrEmpty()) {
                    _exceptionFlow.tryEmit(ComponentException("Fingerprint token not found."))
                    return
                }
                identifyShopper(
                    activity = activity,
                    encodedFingerprintToken = action.token.orEmpty(),
                    submitFingerprintAutomatically = false,
                )
            }
            is Threeds2ChallengeAction -> {
                if (action.token.isNullOrEmpty()) {
                    _exceptionFlow.tryEmit(ComponentException("Challenge token not found."))
                    return
                }
                challengeShopper(activity, action.token.orEmpty())
            }
            is Threeds2Action -> {
                if (action.token.isNullOrEmpty()) {
                    _exceptionFlow.tryEmit(ComponentException("3DS2 token not found."))
                    return
                }
                if (action.subtype == null) {
                    _exceptionFlow.tryEmit(ComponentException("3DS2 Action subtype not found."))
                    return
                }
                val subtype = Threeds2Action.SubType.parse(action.subtype.orEmpty())
                // We need to keep authorizationToken in memory to access it later when the 3DS2 challenge is done
                authorizationToken = action.authorisationToken
                handleActionSubtype(activity, subtype, action.token.orEmpty())
            }
        }
    }

    private fun handleActionSubtype(
        activity: Activity,
        subtype: Threeds2Action.SubType,
        token: String,
    ) {
        when (subtype) {
            Threeds2Action.SubType.FINGERPRINT -> identifyShopper(
                activity = activity,
                encodedFingerprintToken = token,
                submitFingerprintAutomatically = true,
            )
            Threeds2Action.SubType.CHALLENGE -> challengeShopper(activity, token)
        }
    }

    @Suppress("LongMethod")
    @VisibleForTesting
    @Throws(ComponentException::class)
    internal fun identifyShopper(
        activity: Activity,
        encodedFingerprintToken: String,
        submitFingerprintAutomatically: Boolean,
    ) {
        Logger.d(TAG, "identifyShopper - submitFingerprintAutomatically: $submitFingerprintAutomatically")
        val decodedFingerprintToken = base64Encoder.decode(encodedFingerprintToken)

        val fingerprintJson: JSONObject = try {
            JSONObject(decodedFingerprintToken)
        } catch (e: JSONException) {
            throw ComponentException("JSON parsing of FingerprintToken failed", e)
        }

        val fingerprintToken = FingerprintToken.SERIALIZER.deserialize(fingerprintJson)
        val configParameters = AdyenConfigParameters.Builder(
            fingerprintToken.directoryServerId,
            fingerprintToken.directoryServerPublicKey
        ).build()

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Logger.e(TAG, "Unexpected uncaught 3DS2 Exception", throwable)
            _exceptionFlow.tryEmit(CheckoutException("Unexpected 3DS2 exception.", throwable))
        }

        coroutineScope.launch(defaultDispatcher + coroutineExceptionHandler) {
            try {
                Logger.d(TAG, "initialize 3DS2 SDK")
                threeDS2Service.initialize(activity, configParameters, null, uiCustomization)
            } catch (e: SDKRuntimeException) {
                _exceptionFlow.tryEmit(ComponentException("Failed to initialize 3DS2 SDK", e))
                return@launch
            } catch (e: SDKAlreadyInitializedException) {
                // This shouldn't cause any side effect.
                Logger.w(TAG, "3DS2 Service already initialized.")
            }

            currentTransaction = try {
                Logger.d(TAG, "create transaction")
                if (fingerprintToken.threeDSMessageVersion != null) {
                    threeDS2Service.createTransaction(null, fingerprintToken.threeDSMessageVersion)
                } else {
                    _exceptionFlow.tryEmit(
                        ComponentException(
                            "Failed to create 3DS2 Transaction. Missing " +
                                "threeDSMessageVersion inside fingerprintToken."
                        )
                    )
                    return@launch
                }
            } catch (e: SDKNotInitializedException) {
                _exceptionFlow.tryEmit(ComponentException("Failed to create 3DS2 Transaction", e))
                return@launch
            } catch (e: SDKRuntimeException) {
                _exceptionFlow.tryEmit(ComponentException("Failed to create 3DS2 Transaction", e))
                return@launch
            }

            val authenticationRequestParameters = currentTransaction?.authenticationRequestParameters
            if (authenticationRequestParameters == null) {
                _exceptionFlow.tryEmit(ComponentException("Failed to retrieve 3DS2 authentication parameters"))
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

            base64Encoder.encode(fingerprintJson.toString())
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
            configuration.clientKey,
            paymentDataRepository.paymentData
        )
            .fold(
                onSuccess = { result -> onSubmitFingerprintResult(result, activity) },
                onFailure = { e -> _exceptionFlow.tryEmit(ComponentException("Unable to submit fingerprint", e)) }
            )
    }

    private fun onSubmitFingerprintResult(result: SubmitFingerprintResult, activity: Activity) {
        // This flow (calling the internal submitFingerprint endpoint) requires that we do not send paymentData
        // back to the merchant. Setting it to null ensures that when the flow ends and notifyDetails is called,
        // paymentData will not be included in the response.
        paymentDataRepository.paymentData = null

        when (result) {
            is SubmitFingerprintResult.Completed -> {
                emitDetails(result.details)
            }
            is SubmitFingerprintResult.Redirect -> {
                makeRedirect(activity, result.action)
            }
            is SubmitFingerprintResult.Threeds2 -> {
                handleAction(result.action, activity)
            }
        }
    }

    private fun emitDetails(details: JSONObject) {
        val actionComponentData = ActionComponentData(
            details = details,
            paymentData = paymentDataRepository.paymentData,
        )
        _detailsFlow.tryEmit(actionComponentData)
    }

    private fun makeRedirect(activity: Activity, action: RedirectAction) {
        val url = action.url
        try {
            Logger.d(TAG, "makeRedirect - $url")
            redirectHandler.launchUriRedirect(activity, url)
        } catch (e: CheckoutException) {
            _exceptionFlow.tryEmit(e)
        }
    }

    @VisibleForTesting
    @Throws(ComponentException::class)
    internal fun challengeShopper(activity: Activity, encodedChallengeToken: String) {
        Logger.d(TAG, "challengeShopper")

        if (currentTransaction == null) {
            _exceptionFlow.tryEmit(
                Authentication3DS2Exception("Failed to make challenge, missing reference to initial transaction.")
            )
            return
        }

        val decodedChallengeToken = base64Encoder.decode(encodedChallengeToken)
        val challengeTokenJson: JSONObject = try {
            JSONObject(decodedChallengeToken)
        } catch (e: JSONException) {
            _exceptionFlow.tryEmit(ComponentException("JSON parsing of FingerprintToken failed", e))
            return
        }

        val challengeToken = ChallengeToken.SERIALIZER.deserialize(challengeTokenJson)
        val challengeParameters = createChallengeParameters(challengeToken)
        try {
            currentTransaction?.doChallenge(activity, challengeParameters, this, DEFAULT_CHALLENGE_TIME_OUT)
        } catch (e: InvalidInputException) {
            _exceptionFlow.tryEmit(CheckoutException("Error starting challenge", e))
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
                threeDSRequestorAppURL = embeddedRequestorAppUrl
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        try {
            val parsedResult = redirectHandler.parseRedirectResult(intent.data)
            emitDetails(parsedResult)
        } catch (e: CheckoutException) {
            _exceptionFlow.tryEmit(e)
        }
    }

    override fun set3DS2UICustomization(uiCustomization: UiCustomization?) {
        this.uiCustomization = uiCustomization
    }

    override fun completed(completionEvent: CompletionEvent) {
        Logger.d(TAG, "challenge completed")
        try {
            // Check whether authorizationToken was set and create the corresponding details object
            val token = authorizationToken
            val details =
                if (token == null) adyen3DS2Serializer.createChallengeDetails(completionEvent)
                else adyen3DS2Serializer.createThreeDsResultDetails(completionEvent, token)
            emitDetails(details)
        } catch (e: CheckoutException) {
            _exceptionFlow.tryEmit(e)
        } finally {
            closeTransaction()
        }
    }

    override fun cancelled() {
        Logger.d(TAG, "challenge cancelled")
        _exceptionFlow.tryEmit(Cancelled3DS2Exception("Challenge canceled."))
        closeTransaction()
    }

    override fun timedout() {
        Logger.d(TAG, "challenge timed out")
        _exceptionFlow.tryEmit(Authentication3DS2Exception("Challenge timed out."))
        closeTransaction()
    }

    override fun protocolError(protocolErrorEvent: ProtocolErrorEvent) {
        with(protocolErrorEvent.errorMessage) {
            Logger.e(TAG, "protocolError - $errorCode - $errorDescription - $errorDetails")
            _exceptionFlow.tryEmit(Authentication3DS2Exception("Protocol Error - $this"))
        }
        closeTransaction()
    }

    override fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent) {
        Logger.d(TAG, "runtimeError")
        _exceptionFlow.tryEmit(Authentication3DS2Exception("Runtime Error - " + runtimeErrorEvent.errorMessage))
        closeTransaction()
    }

    private fun closeTransaction() {
        currentTransaction?.close()
        currentTransaction = null
        cleanUp3DS2()
    }

    private fun cleanUp3DS2() {
        try {
            ThreeDS2Service.INSTANCE.cleanup(application)
        } catch (e: SDKNotInitializedException) {
            // Safe to ignore
        }
    }

    override fun onCleared() {
        _coroutineScope = null
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val AUTHORIZATION_TOKEN_KEY = "authorization_token"
        private const val DEFAULT_CHALLENGE_TIME_OUT = 10
        private const val PROTOCOL_VERSION_2_1_0 = "2.1.0"
    }
}

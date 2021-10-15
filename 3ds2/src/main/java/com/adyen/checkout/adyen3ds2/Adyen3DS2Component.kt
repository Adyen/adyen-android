/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 7/5/2019.
 */
package com.adyen.checkout.adyen3ds2

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.adyen3ds2.exception.Authentication3DS2Exception
import com.adyen.checkout.adyen3ds2.exception.Cancelled3DS2Exception
import com.adyen.checkout.adyen3ds2.model.ChallengeToken
import com.adyen.checkout.adyen3ds2.model.FingerprintToken
import com.adyen.checkout.adyen3ds2.repository.SubmitFingerprintRepository
import com.adyen.checkout.adyen3ds2.repository.SubmitFingerprintResult
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.encoding.Base64Encoder
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.model.payments.response.Threeds2Action.SubType
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.model.payments.response.Threeds2FingerprintAction
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.redirect.RedirectDelegate
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

@Suppress("TooManyFunctions")
class Adyen3DS2Component(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: Adyen3DS2Configuration,
    private val submitFingerprintRepository: SubmitFingerprintRepository,
    private val adyen3DS2Serializer: Adyen3DS2Serializer,
    private val redirectDelegate: RedirectDelegate
) : BaseActionComponent<Adyen3DS2Configuration>(savedStateHandle, application, configuration), ChallengeStatusReceiver, IntentHandlingComponent {

    private var mTransaction: Transaction? = null
    private var mUiCustomization: UiCustomization? = null
    private var authorizationToken: String?
        get() {
            return savedStateHandle[AUTHORIZATION_TOKEN_KEY]
        }
        set(value) {
            savedStateHandle[AUTHORIZATION_TOKEN_KEY] = value
        }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        if (mTransaction != null) {
            // We don't save the mTransaction reference if the ViewModel gets cleared.
            sGotDestroyedWhileChallenging = true
        }
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ActionComponentData>) {
        super.observe(lifecycleOwner, observer)
        if (sGotDestroyedWhileChallenging) {
            // This is OK if the user goes back and starts a new payment.
            // TODO notify error to activity?
            Logger.e(TAG, "Lost challenge result reference.")
        }
    }

    /**
     * Set a [UiCustomization] object to be passed to the 3DS2 SDK for customizing the challenge screen.
     * Needs to be set before handling any action.
     *
     * @param uiCustomization The customization object.
     */
    fun setUiCustomization(uiCustomization: UiCustomization?) {
        mUiCustomization = uiCustomization
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    @Throws(ComponentException::class)
    override fun handleActionInternal(activity: Activity, action: Action) {
        when (action) {
            is Threeds2FingerprintAction -> {
                if (action.token.isNullOrEmpty()) {
                    throw ComponentException("Fingerprint token not found.")
                }
                identifyShopper(activity, action.token.orEmpty(), submitFingerprintAutomatically = false)
            }
            is Threeds2ChallengeAction -> {
                if (action.token.isNullOrEmpty()) {
                    throw ComponentException("Challenge token not found.")
                }
                challengeShopper(activity, action.token.orEmpty())
            }
            is Threeds2Action -> {
                if (action.token.isNullOrEmpty()) {
                    throw ComponentException("3DS2 token not found.")
                }
                if (action.subtype == null) {
                    throw ComponentException("3DS2 Action subtype not found.")
                }
                val subtype = SubType.parse(action.subtype.orEmpty())
                // We need to keep authorizationToken in memory to access it later when the 3DS2 challenge is done
                authorizationToken = action.authorisationToken
                handleActionSubtype(activity, subtype, action.token.orEmpty())
            }
        }
    }

    private fun handleActionSubtype(activity: Activity, subtype: SubType, token: String) {
        when (subtype) {
            SubType.FINGERPRINT -> identifyShopper(activity, token, submitFingerprintAutomatically = true)
            SubType.CHALLENGE -> challengeShopper(activity, token)
        }
    }

    override fun completed(completionEvent: CompletionEvent) {
        Logger.d(TAG, "challenge completed")
        try {
            // Check whether authorizationToken was set and create the corresponding details object
            val token = authorizationToken
            val details =
                if (token == null) adyen3DS2Serializer.createChallengeDetails(completionEvent)
                else adyen3DS2Serializer.createThreeDsResultDetails(completionEvent, token)
            notifyDetails(details)
        } catch (e: CheckoutException) {
            notifyException(e)
        } finally {
            closeTransaction(getApplication())
        }
    }

    override fun cancelled() {
        Logger.d(TAG, "challenge cancelled")
        notifyException(Cancelled3DS2Exception("Challenge canceled."))
        closeTransaction(getApplication())
    }

    override fun timedout() {
        Logger.d(TAG, "challenge timed out")
        notifyException(Authentication3DS2Exception("Challenge timed out."))
        closeTransaction(getApplication())
    }

    override fun protocolError(protocolErrorEvent: ProtocolErrorEvent) {
        with(protocolErrorEvent.errorMessage) {
            Logger.e(TAG, "protocolError - $errorCode - $errorDescription - $errorDetails")
            notifyException(Authentication3DS2Exception("Protocol Error - $this"))
        }
        closeTransaction(getApplication())
    }

    override fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent) {
        Logger.d(TAG, "runtimeError")
        notifyException(Authentication3DS2Exception("Runtime Error - " + runtimeErrorEvent.errorMessage))
        closeTransaction(getApplication())
    }

    @Throws(ComponentException::class)
    private fun identifyShopper(activity: Activity, encodedFingerprintToken: String, submitFingerprintAutomatically: Boolean) {
        Logger.d(TAG, "identifyShopper - submitFingerprintAutomatically: $submitFingerprintAutomatically")
        val decodedFingerprintToken = Base64Encoder.decode(encodedFingerprintToken)

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
            notifyException(CheckoutException("Unexpected 3DS2 exception.", throwable))
        }
        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            try {
                Logger.d(TAG, "initialize 3DS2 SDK")
                ThreeDS2Service.INSTANCE.initialize(activity, configParameters, null, mUiCustomization)
            } catch (e: SDKRuntimeException) {
                notifyException(ComponentException("Failed to initialize 3DS2 SDK", e))
                return@launch
            } catch (e: SDKAlreadyInitializedException) {
                // This shouldn't cause any side effect.
                Logger.w(TAG, "3DS2 Service already initialized.")
            }

            mTransaction = try {
                Logger.d(TAG, "create transaction")
                if (fingerprintToken.threeDSMessageVersion != null) {
                    ThreeDS2Service.INSTANCE.createTransaction(null, fingerprintToken.threeDSMessageVersion)
                } else {
                    notifyException(ComponentException("Failed to create 3DS2 Transaction. Missing threeDSMessageVersion inside fingerprintToken."))
                    return@launch
                }
            } catch (e: SDKNotInitializedException) {
                notifyException(ComponentException("Failed to create 3DS2 Transaction", e))
                return@launch
            } catch (e: SDKRuntimeException) {
                notifyException(ComponentException("Failed to create 3DS2 Transaction", e))
                return@launch
            }

            val authenticationRequestParameters = mTransaction?.authenticationRequestParameters
            if (authenticationRequestParameters == null) {
                notifyException(ComponentException("Failed to retrieve 3DS2 authentication parameters"))
                return@launch
            }
            val encodedFingerprint = createEncodedFingerprint(authenticationRequestParameters)
            if (submitFingerprintAutomatically) {
                submitFingerprintAutomatically(activity, encodedFingerprint)
            } else {
                launch(Dispatchers.Main) {
                    notifyDetails(adyen3DS2Serializer.createFingerprintDetails(encodedFingerprint))
                }
            }
        }
    }

    private suspend fun submitFingerprintAutomatically(activity: Activity, encodedFingerprint: String) {
        try {
            val result = submitFingerprintRepository.submitFingerprint(encodedFingerprint, configuration, paymentData)
            // This flow (calling the internal submitFingerprint endpoint) requires that we do not send paymentData back to the merchant.
            // Setting it to null ensures that when the flow ends and notifyDetails is called, paymentData will not be included in the response.
            paymentData = null
            when (result) {
                is SubmitFingerprintResult.Completed -> {
                    viewModelScope.launch(Dispatchers.Main) {
                        notifyDetails(result.details)
                    }
                }
                is SubmitFingerprintResult.Redirect -> {
                    redirectDelegate.makeRedirect(activity, result.action)
                }
                is SubmitFingerprintResult.Threeds2 -> {
                    handleAction(activity, result.action)
                }
            }
        } catch (e: ComponentException) {
            notifyException(e)
        }
    }

    @Throws(ComponentException::class)
    private fun challengeShopper(activity: Activity, encodedChallengeToken: String) {
        Logger.d(TAG, "challengeShopper")
        if (mTransaction == null) {
            notifyException(Authentication3DS2Exception("Failed to make challenge, missing reference to initial transaction."))
            return
        }
        val decodedChallengeToken = Base64Encoder.decode(encodedChallengeToken)
        val challengeTokenJson: JSONObject = try {
            JSONObject(decodedChallengeToken)
        } catch (e: JSONException) {
            throw ComponentException("JSON parsing of FingerprintToken failed", e)
        }
        val challengeToken = ChallengeToken.SERIALIZER.deserialize(challengeTokenJson)
        val challengeParameters = createChallengeParameters(challengeToken)
        try {
            mTransaction?.doChallenge(activity, challengeParameters, this, DEFAULT_CHALLENGE_TIME_OUT)
        } catch (e: InvalidInputException) {
            notifyException(CheckoutException("Error starting challenge", e))
        }
    }

    @Throws(ComponentException::class)
    fun createEncodedFingerprint(authenticationRequestParameters: AuthenticationRequestParameters): String {
        val fingerprintJson = JSONObject()
        try {
            with(authenticationRequestParameters) {
                fingerprintJson.put("sdkAppID", sdkAppID)
                fingerprintJson.put("sdkEncData", deviceData)
                fingerprintJson.put("sdkEphemPubKey", JSONObject(sdkEphemeralPublicKey))
                fingerprintJson.put("sdkReferenceNumber", sdkReferenceNumber)
                fingerprintJson.put("sdkTransID", sdkTransactionID)
                fingerprintJson.put("messageVersion", messageVersion)
            }
        } catch (e: JSONException) {
            throw ComponentException("Failed to create encoded fingerprint", e)
        }
        return Base64Encoder.encode(fingerprintJson.toString())
    }

    private fun createChallengeParameters(challenge: ChallengeToken): ChallengeParameters {
        return ChallengeParameters().apply {
            set3DSServerTransactionID(challenge.threeDSServerTransID)
            acsTransactionID = challenge.acsTransID
            acsRefNumber = challenge.acsReferenceNumber
            acsSignedContent = challenge.acsSignedContent
            // This field was introduced in version 2.2.0 so older protocols don't expect it to be present and might throw an error.
            if (challenge.messageVersion != PROTOCOL_VERSION_2_1_0) {
                threeDSRequestorAppURL = ChallengeParameters.getEmbeddedRequestorAppURL(getApplication())
            }
        }
    }

    private fun closeTransaction(context: Context) {
        mTransaction?.close()
        mTransaction = null
        try {
            ThreeDS2Service.INSTANCE.cleanup(context)
        } catch (e: SDKNotInitializedException) {
            // no problem
        }
    }

    /**
     * Call this method when receiving the return URL from the 3DS redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    override fun handleIntent(intent: Intent) {
        try {
            val parsedResult = redirectDelegate.handleRedirectResponse(intent.data)
            notifyDetails(parsedResult)
        } catch (e: CheckoutException) {
            notifyException(e)
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val AUTHORIZATION_TOKEN_KEY = "authorization_token"

        @JvmField
        val PROVIDER: ActionComponentProvider<Adyen3DS2Component, Adyen3DS2Configuration> = Adyen3DS2ComponentProvider()

        private const val DEFAULT_CHALLENGE_TIME_OUT = 10
        private const val PROTOCOL_VERSION_2_1_0 = "2.1.0"

        private var sGotDestroyedWhileChallenging = false
    }
}

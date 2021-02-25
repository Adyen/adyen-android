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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.adyen3ds2.exception.Authentication3DS2Exception
import com.adyen.checkout.adyen3ds2.exception.Cancelled3DS2Exception
import com.adyen.checkout.adyen3ds2.model.ChallengeResult
import com.adyen.checkout.adyen3ds2.model.ChallengeToken
import com.adyen.checkout.adyen3ds2.model.FingerprintToken
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.ActionComponentProviderImpl
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.encoding.Base64Encoder
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.model.payments.response.Threeds2Action.SubType
import com.adyen.checkout.components.model.payments.response.Threeds2Action.SubType.Companion.parse
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.model.payments.response.Threeds2FingerprintAction
import com.adyen.checkout.core.api.ThreadManager
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
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
import org.json.JSONException
import org.json.JSONObject
import java.util.Collections

class Adyen3DS2Component(application: Application, configuration: Adyen3DS2Configuration) :
    BaseActionComponent<Adyen3DS2Configuration>(application, configuration), ChallengeStatusReceiver {

    private var mTransaction: Transaction? = null
    private var mUiCustomization: UiCustomization? = null

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
        synchronized(this) { mUiCustomization = uiCustomization }
    }

    override fun getSupportedActionTypes(): List<String> {
        val supportedCodes = arrayOf(Threeds2FingerprintAction.ACTION_TYPE, Threeds2ChallengeAction.ACTION_TYPE, Threeds2Action.ACTION_TYPE)
        return Collections.unmodifiableList(listOf(*supportedCodes))
    }

    @Throws(ComponentException::class)
    override fun handleActionInternal(activity: Activity, action: Action) {
        when (action.type) {
            Threeds2FingerprintAction.ACTION_TYPE -> {
                val fingerprintAction = action as Threeds2FingerprintAction
                if (fingerprintAction.token.isNullOrEmpty()) {
                    throw ComponentException("Fingerprint token not found.")
                }
                identifyShopper(activity, fingerprintAction.token.orEmpty())
            }
            Threeds2ChallengeAction.ACTION_TYPE -> {
                val challengeAction = action as Threeds2ChallengeAction
                if (challengeAction.token.isNullOrEmpty()) {
                    throw ComponentException("Challenge token not found.")
                }
                challengeShopper(activity, challengeAction.token.orEmpty())
            }
            Threeds2Action.ACTION_TYPE -> {
                val threeds2Action = action as Threeds2Action
                if (threeds2Action.token.isNullOrEmpty()) {
                    throw ComponentException("3DS2 token not found.")
                }
                if (threeds2Action.subtype == null) {
                    throw ComponentException("3DS2 Action subtype not found.")
                }
                val subtype = parse(threeds2Action.subtype.orEmpty())
                handleActionSubtype(activity, subtype, threeds2Action.token.orEmpty())
            }
        }
    }

    private fun handleActionSubtype(activity: Activity, subtype: SubType, token: String) {
        when (subtype) {
            SubType.FINGERPRINT -> identifyShopper(activity, token)
            SubType.CHALLENGE -> challengeShopper(activity, token)
            else -> {
            }
        }
    }

    override fun completed(completionEvent: CompletionEvent) {
        Logger.d(TAG, "challenge completed")
        try {
            notifyDetails(createChallengeDetails(completionEvent))
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
        Logger.e(
            TAG, "protocolError - "
                + protocolErrorEvent.errorMessage.errorCode + " - "
                + protocolErrorEvent.errorMessage.errorDescription + " - "
                + protocolErrorEvent.errorMessage.errorDetails
        )
        notifyException(Authentication3DS2Exception("Protocol Error - " + protocolErrorEvent.errorMessage))
        closeTransaction(getApplication())
    }

    override fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent) {
        Logger.d(TAG, "runtimeError")
        notifyException(Authentication3DS2Exception("Runtime Error - " + runtimeErrorEvent.errorMessage))
        closeTransaction(getApplication())
    }

    @Throws(ComponentException::class)
    private fun identifyShopper(context: Context, encodedFingerprintToken: String) {
        Logger.d(TAG, "identifyShopper")
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

        ThreadManager.EXECUTOR.submit {
            try {
                Logger.d(TAG, "initialize 3DS2 SDK")
                synchronized(this) { ThreeDS2Service.INSTANCE.initialize(context, configParameters, null, mUiCustomization) }
            } catch (e: SDKRuntimeException) {
                notifyException(ComponentException("Failed to initialize 3DS2 SDK", e))
                return@submit
            } catch (e: SDKAlreadyInitializedException) {
                // This shouldn't cause any side effect.
                Logger.w(TAG, "3DS2 Service already initialized.")
            }

            mTransaction = try {
                Logger.d(TAG, "create transaction")
                ThreeDS2Service.INSTANCE.createTransaction(null, fingerprintToken.threeDSMessageVersion)
            } catch (e: SDKNotInitializedException) {
                notifyException(ComponentException("Failed to create 3DS2 Transaction", e))
                return@submit
            } catch (e: SDKRuntimeException) {
                notifyException(ComponentException("Failed to create 3DS2 Transaction", e))
                return@submit
            }

            val authenticationRequestParameters = mTransaction?.authenticationRequestParameters
            if (authenticationRequestParameters != null) {
                val encodedFingerprint = createEncodedFingerprint(authenticationRequestParameters)
                ThreadManager.MAIN_HANDLER.post { notifyDetails(createFingerprintDetails(encodedFingerprint)) }
            }
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
            // TODO getMessageVersion is not used?
            fingerprintJson.put("sdkAppID", authenticationRequestParameters.sdkAppID)
            fingerprintJson.put("sdkEncData", authenticationRequestParameters.deviceData)
            fingerprintJson.put("sdkEphemPubKey", JSONObject(authenticationRequestParameters.sdkEphemeralPublicKey))
            fingerprintJson.put("sdkReferenceNumber", authenticationRequestParameters.sdkReferenceNumber)
            fingerprintJson.put("sdkTransID", authenticationRequestParameters.sdkTransactionID)
        } catch (e: JSONException) {
            throw ComponentException("Failed to create encoded fingerprint", e)
        }
        return Base64Encoder.encode(fingerprintJson.toString())
    }

    private fun createChallengeParameters(challenge: ChallengeToken): ChallengeParameters {
        // TODO referenceNumber, URL and version are not used?
        val challengeParameters = ChallengeParameters()
        challengeParameters.set3DSServerTransactionID(challenge.threeDSServerTransID)
        challengeParameters.acsTransactionID = challenge.acsTransID
        challengeParameters.acsRefNumber = challenge.acsReferenceNumber
        challengeParameters.acsSignedContent = challenge.acsSignedContent
        return challengeParameters
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

    @Throws(ComponentException::class)
    fun createFingerprintDetails(encodedFingerprint: String?): JSONObject {
        val fingerprintDetails = JSONObject()
        try {
            fingerprintDetails.put(FINGERPRINT_DETAILS_KEY, encodedFingerprint)
        } catch (e: JSONException) {
            throw ComponentException("Failed to create fingerprint details", e)
        }
        return fingerprintDetails
    }

    @Throws(ComponentException::class)
    private fun createChallengeDetails(completionEvent: CompletionEvent): JSONObject {
        val challengeDetails = JSONObject()
        try {
            val challengeResult = ChallengeResult.from(completionEvent)
            challengeDetails.put(CHALLENGE_DETAILS_KEY, challengeResult.payload)
        } catch (e: JSONException) {
            throw ComponentException("Failed to create challenge details", e)
        }
        return challengeDetails
    }

    companion object {
        val TAG = LogUtil.getTag()

        val PROVIDER: ActionComponentProvider<Adyen3DS2Component> = ActionComponentProviderImpl(
            Adyen3DS2Component::class.java, Adyen3DS2Configuration::class.java
        )

        private const val FINGERPRINT_DETAILS_KEY = "threeds2.fingerprint"
        private const val CHALLENGE_DETAILS_KEY = "threeds2.challengeResult"
        private const val DEFAULT_CHALLENGE_TIME_OUT = 10

        private var sGotDestroyedWhileChallenging = false
    }
}
/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/5/2021.
 */

package com.adyen.checkout.adyen3ds2.model

import com.adyen.checkout.core.exception.ComponentException
import com.adyen.threeds2.CompletionEvent
import org.json.JSONException
import org.json.JSONObject

class Adyen3DS2Serializer {
    companion object {
        private const val FINGERPRINT_DETAILS_KEY = "threeds2.fingerprint"
        private const val CHALLENGE_DETAILS_KEY = "threeds2.challengeResult"
        private const val THREEDS_RESULT_KEY = "threeDSResult"
    }

    @Throws(ComponentException::class)
    fun createDetailsJson(encodedFingerprint: String): JSONObject {
        return createFingerprintDetails(encodedFingerprint)
    }

    @Throws(ComponentException::class)
    fun createDetailsJson(completionEvent: CompletionEvent, authorisationToken: String?): JSONObject {
        return if (authorisationToken == null) createChallengeDetails(completionEvent)
        else createThreeDsResultDetails(completionEvent, authorisationToken)
    }

    private fun createFingerprintDetails(encodedFingerprint: String): JSONObject {
        val fingerprintDetails = JSONObject()
        try {
            fingerprintDetails.put(FINGERPRINT_DETAILS_KEY, encodedFingerprint)
        } catch (e: JSONException) {
            throw ComponentException("Failed to create fingerprint details", e)
        }
        return fingerprintDetails
    }

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

    private fun createThreeDsResultDetails(completionEvent: CompletionEvent, authorisationToken: String): JSONObject {
        val threeDsDetails = JSONObject()
        try {
            val challengeResult = ChallengeResult.from(completionEvent, authorisationToken)
            threeDsDetails.put(THREEDS_RESULT_KEY, challengeResult.payload)
        } catch (e: JSONException) {
            throw ComponentException("Failed to create ThreeDS Result details", e)
        }
        return threeDsDetails
    }
}
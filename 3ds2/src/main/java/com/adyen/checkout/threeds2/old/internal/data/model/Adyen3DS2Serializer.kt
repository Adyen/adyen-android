/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/5/2021.
 */

package com.adyen.checkout.threeds2.old.internal.data.model

import com.adyen.checkout.core.old.exception.ComponentException
import org.json.JSONException
import org.json.JSONObject

internal class Adyen3DS2Serializer {

    @Throws(ComponentException::class)
    fun createFingerprintDetails(encodedFingerprint: String): JSONObject {
        val fingerprintDetails = JSONObject()
        try {
            fingerprintDetails.put(FINGERPRINT_DETAILS_KEY, encodedFingerprint)
        } catch (e: JSONException) {
            throw ComponentException("Failed to create fingerprint details", e)
        }
        return fingerprintDetails
    }

    @Throws(ComponentException::class)
    fun createChallengeDetails(transactionStatus: String, errorDetails: String? = null): JSONObject {
        val challengeDetails = JSONObject()
        try {
            val challengeResult = ChallengeResult.from(transactionStatus, errorDetails)
            challengeDetails.put(CHALLENGE_DETAILS_KEY, challengeResult.payload)
        } catch (e: JSONException) {
            throw ComponentException("Failed to create challenge details", e)
        }
        return challengeDetails
    }

    @Throws(ComponentException::class)
    fun createThreeDsResultDetails(
        transactionStatus: String,
        authorisationToken: String,
        errorDetails: String? = null,
    ): JSONObject {
        val threeDsDetails = JSONObject()
        try {
            val challengeResult = ChallengeResult.from(transactionStatus, errorDetails, authorisationToken)
            threeDsDetails.put(THREEDS_RESULT_KEY, challengeResult.payload)
        } catch (e: JSONException) {
            throw ComponentException("Failed to create ThreeDS Result details", e)
        }
        return threeDsDetails
    }

    companion object {
        private const val FINGERPRINT_DETAILS_KEY = "threeds2.fingerprint"
        private const val CHALLENGE_DETAILS_KEY = "threeds2.challengeResult"
        private const val THREEDS_RESULT_KEY = "threeDSResult"
    }
}

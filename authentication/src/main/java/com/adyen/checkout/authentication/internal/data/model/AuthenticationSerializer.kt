/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/5/2021.
 */

package com.adyen.checkout.authentication.internal.data.model

import org.json.JSONException
import org.json.JSONObject

// TODO - Improve error handling
internal class AuthenticationSerializer {

    @Throws(JSONException::class)
    fun createFingerprintDetails(encodedFingerprint: String): JSONObject {
        val fingerprintDetails = JSONObject()
        fingerprintDetails.put(FINGERPRINT_DETAILS_KEY, encodedFingerprint)
        return fingerprintDetails
    }

    @Throws(JSONException::class)
    fun createChallengeDetails(transactionStatus: String, errorDetails: String? = null): JSONObject {
        val challengeDetails = JSONObject()
        val challengeResult = ChallengeResult.from(transactionStatus, errorDetails)
        challengeDetails.put(CHALLENGE_DETAILS_KEY, challengeResult.payload)
        return challengeDetails
    }

    @Throws(JSONException::class)
    fun createThreeDsResultDetails(
        transactionStatus: String,
        authorisationToken: String,
        errorDetails: String? = null,
    ): JSONObject {
        val threeDsDetails = JSONObject()
        val challengeResult = ChallengeResult.from(transactionStatus, errorDetails, authorisationToken)
        threeDsDetails.put(THREE_DS_RESULT_KEY, challengeResult.payload)
        return threeDsDetails
    }

    companion object {
        private const val FINGERPRINT_DETAILS_KEY = "threeds2.fingerprint"
        private const val CHALLENGE_DETAILS_KEY = "threeds2.challengeResult"
        private const val THREE_DS_RESULT_KEY = "threeDSResult"
    }
}

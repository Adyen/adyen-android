/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/6/2026.
 */

package com.adyen.checkout.authentication.internal.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
internal class AuthenticationSerializerTest {

    private val serializer = AuthenticationSerializer()

    @Test
    fun `when createFingerprintDetails is called then result contains encoded fingerprint`() {
        // GIVEN
        val encodedFingerprint = "test-fingerprint-value"

        // WHEN
        val result = serializer.createFingerprintDetails(encodedFingerprint)

        // THEN
        assertEquals(encodedFingerprint, result.getString("threeds2.fingerprint"))
    }

    @Test
    fun `when createChallengeDetails is called then result contains base64 encoded challenge result`() {
        // GIVEN
        val transactionStatus = "Y"

        // WHEN
        val result = serializer.createChallengeDetails(transactionStatus)

        // THEN
        val payload = result.getString("threeds2.challengeResult")
        val decoded = JSONObject(String(Base64.decode(payload)))
        assertEquals("Y", decoded.getString("transStatus"))
    }

    @Test
    fun `when createChallengeDetails is called with error details then result contains error details`() {
        // GIVEN
        val transactionStatus = "N"
        val errorDetails = "some error"

        // WHEN
        val result = serializer.createChallengeDetails(transactionStatus, errorDetails)

        // THEN
        val payload = result.getString("threeds2.challengeResult")
        val decoded = JSONObject(String(Base64.decode(payload)))
        assertEquals("N", decoded.getString("transStatus"))
        assertEquals("some error", decoded.getString("threeDS2SDKError"))
    }

    @Test
    fun `when createChallengeDetails is called without error details then result does not contain error key`() {
        // GIVEN
        val transactionStatus = "Y"

        // WHEN
        val result = serializer.createChallengeDetails(transactionStatus)

        // THEN
        val payload = result.getString("threeds2.challengeResult")
        val decoded = JSONObject(String(Base64.decode(payload)))
        assertEquals(false, decoded.has("threeDS2SDKError"))
    }

    @Test
    fun `when createThreeDsResultDetails is called then result contains base64 encoded payload`() {
        // GIVEN
        val transactionStatus = "Y"
        val authorisationToken = "test-auth-token"

        // WHEN
        val result = serializer.createThreeDsResultDetails(transactionStatus, authorisationToken)

        // THEN
        val payload = result.getString("threeDSResult")
        val decoded = JSONObject(String(Base64.decode(payload)))
        assertEquals("Y", decoded.getString("transStatus"))
        assertEquals("test-auth-token", decoded.getString("authorisationToken"))
    }

    @Test
    fun `when createThreeDsResultDetails is called with error details then result contains error details`() {
        // GIVEN
        val transactionStatus = "N"
        val authorisationToken = "test-auth-token"
        val errorDetails = "challenge error"

        // WHEN
        val result = serializer.createThreeDsResultDetails(transactionStatus, authorisationToken, errorDetails)

        // THEN
        val payload = result.getString("threeDSResult")
        val decoded = JSONObject(String(Base64.decode(payload)))
        assertEquals("N", decoded.getString("transStatus"))
        assertEquals("test-auth-token", decoded.getString("authorisationToken"))
        assertEquals("challenge error", decoded.getString("threeDS2SDKError"))
    }

    @Test
    fun `when createThreeDsResultDetails is called without error details then result does not contain error key`() {
        // GIVEN
        val transactionStatus = "Y"
        val authorisationToken = "test-auth-token"

        // WHEN
        val result = serializer.createThreeDsResultDetails(transactionStatus, authorisationToken)

        // THEN
        val payload = result.getString("threeDSResult")
        val decoded = JSONObject(String(Base64.decode(payload)))
        assertEquals(false, decoded.has("threeDS2SDKError"))
    }
}

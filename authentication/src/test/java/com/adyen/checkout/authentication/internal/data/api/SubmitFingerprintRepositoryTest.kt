/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/6/2026.
 */

package com.adyen.checkout.authentication.internal.data.api

import com.adyen.checkout.authentication.internal.data.model.SubmitFingerprintResponse
import com.adyen.checkout.authentication.internal.data.model.SubmitFingerprintResult
import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.data.Threeds2Action
import com.adyen.checkout.core.common.LoggingExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class SubmitFingerprintRepositoryTest(
    @param:Mock private val submitFingerprintService: SubmitFingerprintService,
) {

    @Test
    fun `when response type is completed with details then result is Completed`() = runTest {
        // GIVEN
        val response = SubmitFingerprintResponse(
            action = null,
            type = "completed",
            details = """{"resultCode":"authorised"}""",
        )
        whenever(submitFingerprintService.submitFingerprint(any(), any())) doReturn response
        val repository = SubmitFingerprintRepository(submitFingerprintService)

        // WHEN
        val result = repository.submitFingerprint("fingerprint", "clientKey", "paymentData")

        // THEN
        assertTrue(result.isSuccess)
        assertInstanceOf<SubmitFingerprintResult.Completed>(result.getOrThrow())
    }

    @Test
    fun `when response type is completed with details then Completed contains parsed details`() = runTest {
        // GIVEN
        val detailsJson = """{"resultCode":"authorised"}"""
        val response = SubmitFingerprintResponse(
            action = null,
            type = "completed",
            details = detailsJson,
        )
        whenever(submitFingerprintService.submitFingerprint(any(), any())) doReturn response
        val repository = SubmitFingerprintRepository(submitFingerprintService)

        // WHEN
        val result = repository.submitFingerprint("fingerprint", "clientKey", "paymentData")

        // THEN
        val completed = result.getOrThrow() as SubmitFingerprintResult.Completed
        assertEquals("authorised", completed.details.getString("resultCode"))
    }

    @Test
    fun `when response type is action with RedirectAction then result is Redirect`() = runTest {
        // GIVEN
        val redirectAction = RedirectAction(
            type = RedirectAction.ACTION_TYPE,
            paymentData = null,
            paymentMethodType = null,
            method = null,
            url = "https://redirect.url",
            nativeRedirectData = null,
        )
        val response = SubmitFingerprintResponse(
            action = redirectAction,
            type = "action",
            details = null,
        )
        whenever(submitFingerprintService.submitFingerprint(any(), any())) doReturn response
        val repository = SubmitFingerprintRepository(submitFingerprintService)

        // WHEN
        val result = repository.submitFingerprint("fingerprint", "clientKey", "paymentData")

        // THEN
        assertTrue(result.isSuccess)
        val redirect = assertInstanceOf<SubmitFingerprintResult.Redirect>(result.getOrThrow())
        assertEquals(redirectAction, redirect.action)
    }

    @Test
    fun `when response type is action with Threeds2Action then result is Threeds2`() = runTest {
        // GIVEN
        val threeds2Action = Threeds2Action(
            type = Threeds2Action.ACTION_TYPE,
            paymentData = null,
            paymentMethodType = null,
            token = "test-token",
            subtype = null,
            authorisationToken = null,
        )
        val response = SubmitFingerprintResponse(
            action = threeds2Action,
            type = "action",
            details = null,
        )
        whenever(submitFingerprintService.submitFingerprint(any(), any())) doReturn response
        val repository = SubmitFingerprintRepository(submitFingerprintService)

        // WHEN
        val result = repository.submitFingerprint("fingerprint", "clientKey", "paymentData")

        // THEN
        assertTrue(result.isSuccess)
        val unwrappedResult = assertInstanceOf<SubmitFingerprintResult.Threeds2>(result.getOrThrow())
        assertEquals(threeds2Action, unwrappedResult.action)
    }

    @Test
    fun `when response type is completed with null details then result is failure`() = runTest {
        // GIVEN
        val response = SubmitFingerprintResponse(
            action = null,
            type = "completed",
            details = null,
        )
        whenever(submitFingerprintService.submitFingerprint(any(), any())) doReturn response
        val repository = SubmitFingerprintRepository(submitFingerprintService)

        // WHEN
        val result = repository.submitFingerprint("fingerprint", "clientKey", "paymentData")

        // THEN
        assertTrue(result.isFailure)
    }

    @Test
    fun `when response type is unknown then result is failure`() = runTest {
        // GIVEN
        val response = SubmitFingerprintResponse(
            action = null,
            type = "unknown",
            details = null,
        )
        whenever(submitFingerprintService.submitFingerprint(any(), any())) doReturn response
        val repository = SubmitFingerprintRepository(submitFingerprintService)

        // WHEN
        val result = repository.submitFingerprint("fingerprint", "clientKey", "paymentData")

        // THEN
        assertTrue(result.isFailure)
    }

    @Test
    fun `when response type is action with null action then result is failure`() = runTest {
        // GIVEN
        val response = SubmitFingerprintResponse(
            action = null,
            type = "action",
            details = null,
        )
        whenever(submitFingerprintService.submitFingerprint(any(), any())) doReturn response
        val repository = SubmitFingerprintRepository(submitFingerprintService)

        // WHEN
        val result = repository.submitFingerprint("fingerprint", "clientKey", "paymentData")

        // THEN
        assertTrue(result.isFailure)
    }

    @Test
    fun `when service throws exception then result is failure`() = runTest {
        // GIVEN
        whenever(submitFingerprintService.submitFingerprint(any(), any())) doThrow RuntimeException("Network error")
        val repository = SubmitFingerprintRepository(submitFingerprintService)

        // WHEN
        val result = repository.submitFingerprint("fingerprint", "clientKey", "paymentData")

        // THEN
        assertTrue(result.isFailure)
    }
}

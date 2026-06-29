/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/6/2026.
 */

@file:Suppress("DEPRECATION")

package com.adyen.checkout.authentication.internal.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.authentication.internal.analytics.AuthenticationEvents
import com.adyen.checkout.authentication.internal.data.api.SubmitFingerprintRepository
import com.adyen.checkout.authentication.internal.data.model.AuthenticationSerializer
import com.adyen.checkout.authentication.internal.data.model.SubmitFingerprintResult
import com.adyen.checkout.authentication.internal.ui.model.AuthenticationComponentParams
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.data.Threeds2Action
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.LoggingExtension
import com.adyen.checkout.core.common.test
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.redirect.internal.RedirectHandler
import com.adyen.threeds2.AuthenticationRequestParameters
import com.adyen.threeds2.ChallengeResult
import com.adyen.threeds2.ChallengeStatusHandler
import com.adyen.threeds2.ChallengeStatusReceiver
import com.adyen.threeds2.InitializeResult
import com.adyen.threeds2.ProgressDialog
import com.adyen.threeds2.ThreeDS2Service
import com.adyen.threeds2.Transaction
import com.adyen.threeds2.TransactionResult
import com.adyen.threeds2.Warning
import com.adyen.threeds2.customization.UiCustomization
import com.adyen.threeds2.exception.InvalidInputException
import com.adyen.threeds2.exception.SDKNotInitializedException
import com.adyen.threeds2.exception.SDKRuntimeException
import com.adyen.threeds2.parameters.ChallengeParameters
import com.adyen.threeds2.parameters.ConfigParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import kotlin.io.encoding.Base64

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class, MockitoExtension::class)
internal class AuthenticationComponentTest(
    @param:Mock private val submitFingerprintRepository: SubmitFingerprintRepository,
    @param:Mock private val redirectHandler: RedirectHandler,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var component: AuthenticationComponent
    private lateinit var paymentDataRepository: PaymentDataRepository
    private lateinit var threeDS2Service: TestThreeDS2Service

    @BeforeEach
    fun setup() {
        // Needs to reset before the component is created
        SharedChallengeStatusHandler.reset()
        analyticsManager = TestAnalyticsManager()
        threeDS2Service = TestThreeDS2Service()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        component = createComponent()
    }

    private fun createComponent(
        action: Action = Threeds2Action(
            subtype = Threeds2Action.SubType.FINGERPRINT.value,
            token = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray()),
        ),
        authenticationSerializer: AuthenticationSerializer = AuthenticationSerializer(),
    ): AuthenticationComponent {
        return AuthenticationComponent(
            action = action,
            componentParams = AuthenticationComponentParams(
                threeDSRequestorAppURL = null,
                deviceParameterBlockList = null,
            ),
            savedStateHandle = SavedStateHandle(),
            analyticsManager = analyticsManager,
            redirectHandler = redirectHandler,
            authenticationSerializer = authenticationSerializer,
            threeDS2Service = threeDS2Service,
            submitFingerprintRepository = submitFingerprintRepository,
            paymentDataRepository = paymentDataRepository,
            coroutineDispatcher = UnconfinedTestDispatcher(),
            application = Application(),
            clientKey = TEST_CLIENT_KEY,
        )
    }

    @Nested
    @DisplayName("when handling action and")
    inner class HandleActionTest {

        @Test
        fun `action is not Threeds2Action, then an error is emitted`() = runTest {
            // GIVEN
            val redirectAction = RedirectAction(url = "https://redirect.url")
            component = createComponent(action = redirectAction)
            component.initialize(this)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.handleAction(Activity(), mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals("Unsupported action", event.error.message)
        }

        @Test
        fun `subtype is null, then an error is emitted`() = runTest {
            // GIVEN
            val action = Threeds2Action(subtype = null, token = "token")
            component = createComponent(action = action)
            component.initialize(this)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.handleAction(Activity(), mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals("3DS2 Action subtype not found.", event.error.message)
        }

        @Test
        fun `token is null for fingerprint subtype, then an error is emitted`() = runTest {
            // GIVEN
            val action = Threeds2Action(
                subtype = Threeds2Action.SubType.FINGERPRINT.value,
                token = null,
            )
            component = createComponent(action = action)
            component.initialize(this)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.handleAction(Activity(), mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals("3DS2 token not found.", event.error.message)
        }

        @Test
        fun `token is null for challenge subtype, then an error is emitted`() = runTest {
            // GIVEN
            val action = Threeds2Action(
                subtype = Threeds2Action.SubType.CHALLENGE.value,
                token = null,
            )
            component = createComponent(action = action)
            component.initialize(this)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.handleAction(Activity(), mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals("3DS2 token not found.", event.error.message)
        }

        @Test
        fun `action has paymentData, then paymentData is stored`() = runTest {
            // GIVEN
            val action = Threeds2Action(
                subtype = Threeds2Action.SubType.FINGERPRINT.value,
                token = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray()),
                paymentData = "test_payment_data",
            )
            component = createComponent(action = action)
            component.initialize(this)

            // WHEN
            component.handleAction(Activity(), mock())

            // THEN
            assertEquals("test_payment_data", paymentDataRepository.paymentData)
        }
    }

    @Nested
    @DisplayName("when identifying shopper and")
    inner class IdentifyShopperTest {

        @Test
        fun `fingerprint token is malformed, then an error is emitted`() = runTest {
            // GIVEN
            component.initialize(this)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            val encodedJson = Base64.encode("{incorrectJson}".toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals("Failed to decode fingerprint token", event.error.message)
        }

        @Test
        fun `fingerprint token is partial, then an error is emitted`() = runTest {
            // GIVEN
            val partialFingerprintToken = """
                {
                    "directoryServerId":"id"
                }
            """.trimIndent()
            component.initialize(this)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            val encodedJson = Base64.encode(partialFingerprintToken.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals("Failed to create ConfigParameters.", event.error.message)
        }

        @Test
        fun `3ds2 sdk returns an initialization failure, then details are emitted`() = runTest {
            // GIVEN
            threeDS2Service.initializeResult = InitializeResult.Failure("X", "mockAdditionalDetails")
            component.initialize(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.ActionDetails>(eventFlow.latestValue)
            assertNotNull(event.data.details)
        }

        @Test
        fun `3ds2 sdk throws an exception while initializing, then an error is emitted`() = runTest {
            // GIVEN
            val error = InvalidInputException("test", null)
            threeDS2Service.initializeError = error
            component.initialize(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals(error, event.error.cause)
        }

        @Test
        fun `creating transaction fails with SDKNotInitializedException, then an error is emitted`() = runTest {
            // GIVEN
            val error = SDKNotInitializedException()
            threeDS2Service.createTransactionError = error
            component.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals(error, event.error.cause)
        }

        @Test
        fun `creating transaction fails with SDKRuntimeException, then an error is emitted`() = runTest {
            // GIVEN
            val error = SDKRuntimeException("test", "test", null)
            threeDS2Service.createTransactionError = error
            component.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals(error, event.error.cause)
        }

        @Test
        fun `creating transaction returns failure, then details are emitted`() = runTest {
            // GIVEN
            threeDS2Service.transactionResult = TransactionResult.Failure("X", "mockDetails")
            component.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.ActionDetails>(eventFlow.latestValue)
            assertNotNull(event.data.details)
        }

        @Test
        fun `transaction parameters are null, then an error is emitted`() = runTest {
            // GIVEN
            component.initialize(this)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals("Failed to retrieve 3DS2 authentication parameters", event.error.message)
        }

        @Test
        fun `fingerprint is submitted automatically and result is completed, then details are emitted`() = runTest {
            // GIVEN
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            val submitResult = SubmitFingerprintResult.Completed(JSONObject())
            whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                Result.success(submitResult)
            val eventFlow = component.eventFlow.test(testScheduler)
            component.initialize(this)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, true, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.ActionDetails>(eventFlow.latestValue)
            val expected = ActionComponentData(
                paymentData = null,
                details = submitResult.details,
            )
            assertEquals(expected, event.data)
        }

        @Test
        fun `fingerprint is submitted automatically and result is redirect, then redirect is launched`() = runTest {
            // GIVEN
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            val redirectAction = RedirectAction(url = "https://redirect.url")
            val submitResult = SubmitFingerprintResult.Redirect(redirectAction)
            whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                Result.success(submitResult)
            component.initialize(this)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, true, mock())

            // THEN
            verify(redirectHandler).launchUriRedirect(any(), any())
        }

        @Test
        fun `fingerprint is submitted automatically and it fails, then an error is emitted`() = runTest {
            // GIVEN
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            val error = IOException("test")
            whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                Result.failure(error)
            val eventFlow = component.eventFlow.test(testScheduler)
            component.initialize(this)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, true, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals(error, event.error.cause)
        }

        @Test
        fun `fingerprint is submitted automatically and result is threeds2, then new action is handled`() = runTest {
            // GIVEN
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            val threeds2Action = Threeds2Action(subtype = null)
            val submitResult = SubmitFingerprintResult.Threeds2(threeds2Action)
            whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                Result.success(submitResult)
            val eventFlow = component.eventFlow.test(testScheduler)
            component.initialize(this)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, true, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals("3DS2 Action subtype not found.", event.error.message)
        }

        @Test
        fun `fingerprint is submitted automatically and redirect fails, then an error is emitted`() = runTest {
            // GIVEN
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            val redirectAction = RedirectAction(url = "https://redirect.url")
            val submitResult = SubmitFingerprintResult.Redirect(redirectAction)
            whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                Result.success(submitResult)
            whenever(redirectHandler.launchUriRedirect(any(), any())) doAnswer {
                throw GenericError("test redirect error")
            }
            val eventFlow = component.eventFlow.test(testScheduler)
            component.initialize(this)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, true, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals("test redirect error", event.error.message)
        }

        @Test
        fun `fingerprint is not submitted automatically, then details are emitted`() = runTest {
            // GIVEN
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            val eventFlow = component.eventFlow.test(testScheduler)
            component.initialize(this)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.ActionDetails>(eventFlow.latestValue)
            assertNotNull(event.data.details)
        }

        @Test
        fun `threeDSMessageVersion is null, then an error is emitted`() = runTest {
            // GIVEN
            val fingerprintTokenWithoutVersion = """
                {
                    "directoryServerId":"id",
                    "directoryServerPublicKey":"key",
                    "directoryServerRootCertificates":"certs"
                }
            """.trimIndent()
            component.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            val encodedJson = Base64.encode(fingerprintTokenWithoutVersion.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertTrue(event.error.message!!.contains("Missing threeDSMessageVersion"))
        }
    }

    @Nested
    @DisplayName("when challenging shopper and")
    inner class ChallengeShopperTest {

        @Test
        fun `transaction is null, then an error is emitted`() = runTest {
            // GIVEN
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.challengeShopper(mock(), "token")

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertTrue(event.error.message!!.contains("missing reference to initial transaction"))
        }

        @Test
        fun `token can't be decoded, then an error is emitted`() = runTest {
            // GIVEN
            initializeChallengeTransaction(this)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.challengeShopper(Activity(), Base64.encode("token".toByteArray()))

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertEquals("JSON parsing of challenge token failed", event.error.message)
        }

        @Test
        fun `everything is good, then challenge should be executed`() = runTest {
            // GIVEN
            val transaction = initializeChallengeTransaction(this)

            // WHEN
            component.challengeShopper(
                Activity(),
                Base64.encode("""{"messageVersion":"2.1.0"}""".toByteArray()),
            )

            // THEN
            transaction.assertDoChallengeCalled()
        }

        @Test
        fun `challenge fails, then an error is emitted`() = runTest {
            // GIVEN
            initializeChallengeTransaction(this).apply {
                shouldThrowError = true
            }
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.challengeShopper(
                Activity(),
                Base64.encode("""{"messageVersion":"2.1.0"}""".toByteArray()),
            )

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertTrue(event.error.cause is InvalidInputException)
        }
    }

    private fun initializeChallengeTransaction(scope: CoroutineScope): TestTransaction {
        val transaction = TestTransaction(getAuthenticationRequestParams())
        threeDS2Service.transactionResult = TransactionResult.Success(transaction)

        component.initialize(scope)

        val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
        component.identifyShopper(Activity(), encodedJson, false, mock())

        return transaction
    }

    @Nested
    @DisplayName("when transaction is")
    inner class TransactionTest {

        @Test
        fun `completed, then details are emitted`() = runTest {
            // GIVEN
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.onCompletion(ChallengeResult.Completed(transactionStatus = "transactionStatus"))

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.ActionDetails>(eventFlow.latestValue)
            assertNotNull(event.data.details)
        }

        @Test
        fun `completed and creating details fails, then an error is emitted`() = runTest {
            // GIVEN
            val authSerializer: AuthenticationSerializer = mock()
            whenever(authSerializer.createChallengeDetails(any(), anyOrNull())) doAnswer {
                throw JSONException("test")
            }
            component = createComponent(authenticationSerializer = authSerializer)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.onCompletion(ChallengeResult.Completed(transactionStatus = "transactionStatus"))

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.Error>(eventFlow.latestValue)
            assertNotNull(event.error)
        }

        @Test
        fun `cancelled, then details are emitted`() = runTest {
            // GIVEN
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.onCompletion(
                ChallengeResult.Cancelled(
                    transactionStatus = "transactionStatus",
                    additionalDetails = "additionalDetails",
                ),
            )

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.ActionDetails>(eventFlow.latestValue)
            assertNotNull(event.data.details)
        }

        @Test
        fun `timed out, then details are emitted`() = runTest {
            // GIVEN
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.onCompletion(
                ChallengeResult.Timeout(
                    transactionStatus = "transactionStatus",
                    additionalDetails = "additionalDetails",
                ),
            )

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.ActionDetails>(eventFlow.latestValue)
            assertNotNull(event.data.details)
        }

        @Test
        fun `error, then details are emitted`() = runTest {
            // GIVEN
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.onCompletion(
                ChallengeResult.Error(
                    transactionStatus = "transactionStatus",
                    additionalDetails = "additionalDetails",
                ),
            )

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.ActionDetails>(eventFlow.latestValue)
            assertNotNull(event.data.details)
        }

        @Test
        fun `completed with authorisationToken, then threeDsResult details are used`() = runTest {
            // GIVEN
            val action = Threeds2Action(
                subtype = Threeds2Action.SubType.FINGERPRINT.value,
                token = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray()),
                authorisationToken = "test-auth-token",
            )
            component = createComponent(action = action)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.onCompletion(ChallengeResult.Completed(transactionStatus = "Y"))

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.ActionDetails>(eventFlow.latestValue)
            assertTrue(event.data.details!!.has("threeDSResult"))
        }

        @Test
        fun `completed without authorisationToken, then challengeResult details are used`() = runTest {
            // GIVEN
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.onCompletion(ChallengeResult.Completed(transactionStatus = "Y"))

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.ActionDetails>(eventFlow.latestValue)
            assertTrue(event.data.details!!.has("threeds2.challengeResult"))
        }
    }

    @Nested
    @DisplayName("when tracking analytics and")
    inner class AnalyticsTest {

        @Test
        fun `handleAction is called with fingerprint subtype, then action event is tracked`() {
            // GIVEN
            val action = Threeds2Action(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                type = TEST_ACTION_TYPE,
                subtype = Threeds2Action.SubType.FINGERPRINT.value,
                token = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray()),
            )
            component = createComponent(action = action)
            component.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            // WHEN
            component.handleAction(Activity(), mock())

            // THEN
            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
                message = "Fingerprint action was handled by the SDK",
            )
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @Test
        fun `handleAction is called with challenge subtype, then action event is tracked`() {
            // GIVEN
            val action = Threeds2Action(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                type = TEST_ACTION_TYPE,
                subtype = Threeds2Action.SubType.CHALLENGE.value,
                token = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray()),
            )
            component = createComponent(action = action)
            component.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            // WHEN
            component.handleAction(Activity(), mock())

            // THEN
            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
                message = "Challenge action was handled by the SDK",
            )
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @Test
        fun `identifyShopper is called, then fingerprint data sent event is tracked`() = runTest {
            // GIVEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.initialize(this)

            // WHEN
            component.identifyShopper(Activity(), encodedJson, true, mock())

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2Fingerprint(
                subType = AuthenticationEvents.SubType.FINGERPRINT_DATA_SENT,
            )
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @ParameterizedTest
        @MethodSource(
            "com.adyen.checkout.authentication.internal.ui.AuthenticationComponentTest#fingerprintResult",
        )
        fun `fingerprint result is returned, then event is tracked`(
            fingerprintResult: SubmitFingerprintResult,
            analyticsResult: AuthenticationEvents.Result,
        ) = runTest {
            // GIVEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                Result.success(fingerprintResult)
            component.initialize(this)

            // WHEN
            component.identifyShopper(Activity(), encodedJson, true, mock())

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2Fingerprint(
                subType = AuthenticationEvents.SubType.FINGERPRINT_COMPLETED,
                result = analyticsResult,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `challengeShopper is called, then challenge data sent event is tracked`() = runTest {
            // GIVEN
            initializeChallengeTransaction(this)

            // WHEN
            component.challengeShopper(
                Activity(),
                Base64.encode("""{"messageVersion":"2.1.0"}""".toByteArray()),
            )

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2Challenge(
                subType = AuthenticationEvents.SubType.CHALLENGE_DATA_SENT,
            )
            analyticsManager.assertHasEventEquals(expectedEvent)

            val expectedDisplayedEvent = AuthenticationEvents.threeDS2Challenge(
                subType = AuthenticationEvents.SubType.CHALLENGE_DISPLAYED,
            )
            analyticsManager.assertLastEventEquals(expectedDisplayedEvent)
        }

        @ParameterizedTest
        @MethodSource(
            "com.adyen.checkout.authentication.internal.ui.AuthenticationComponentTest#challengeResult",
        )
        fun `challenge result is returned, then event is tracked`(
            challengeResult: ChallengeResult,
            analyticsResult: AuthenticationEvents.Result,
        ) = runTest {
            // WHEN
            component.onCompletion(result = challengeResult)

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2Challenge(
                subType = AuthenticationEvents.SubType.CHALLENGE_COMPLETED,
                result = analyticsResult,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `token is null for fingerprint, then fingerprint error event is tracked`() = runTest {
            // GIVEN
            val action = Threeds2Action(
                subtype = Threeds2Action.SubType.FINGERPRINT.value,
                token = null,
            )
            component = createComponent(action = action)
            component.initialize(this)

            // WHEN
            component.handleAction(Activity(), mock())

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_TOKEN_MISSING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `token is null for challenge, then challenge error event is tracked`() = runTest {
            // GIVEN
            val action = Threeds2Action(
                subtype = Threeds2Action.SubType.CHALLENGE.value,
                token = null,
            )
            component = createComponent(action = action)
            component.initialize(this)

            // WHEN
            component.handleAction(Activity(), mock())

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_TOKEN_MISSING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `fingerprint token is malformed, then error event is tracked`() = runTest {
            // GIVEN
            component.initialize(this)

            // WHEN
            val encodedJson = Base64.encode("{incorrectJson}".toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_TOKEN_DECODING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `fingerprint token is partial, then error event is tracked`() = runTest {
            // GIVEN
            val partialFingerprintToken = """
                {
                    "directoryServerId":"id"
                }
            """.trimIndent()
            component.initialize(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

            // WHEN
            val encodedJson = Base64.encode(partialFingerprintToken.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_FINGERPRINT_CREATION,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `3ds2 sdk throws while initializing, then error event is tracked`() = runTest {
            // GIVEN
            threeDS2Service.initializeError = InvalidInputException("test", null)
            component.initialize(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_FINGERPRINT_HANDLING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `transaction parameters are null, then error event is tracked`() = runTest {
            // GIVEN
            component.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_FINGERPRINT_CREATION,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `threeDSMessageVersion is null, then error event is tracked`() = runTest {
            // GIVEN
            val fingerprintTokenWithoutVersion = """
                {
                    "directoryServerId":"id",
                    "directoryServerPublicKey":"key",
                    "directoryServerRootCertificates":"certs"
                }
            """.trimIndent()
            component.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            // WHEN
            val encodedJson = Base64.encode(fingerprintTokenWithoutVersion.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_TRANSACTION_CREATION,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `creating transaction fails with SDKNotInitializedException, then error event is tracked`() =
            runTest {
                // GIVEN
                val error = SDKNotInitializedException()
                threeDS2Service.createTransactionError = error
                component.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                // WHEN
                val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
                component.identifyShopper(Activity(), encodedJson, false, mock())

                // THEN
                val expectedEvent = AuthenticationEvents.threeDS2FingerprintError(
                    event = ErrorEvent.THREEDS2_TRANSACTION_CREATION,
                )
                analyticsManager.assertLastEventEquals(expectedEvent)
            }

        @Test
        fun `creating transaction fails with SDKRuntimeException, then error event is tracked`() = runTest {
            // GIVEN
            val error = SDKRuntimeException("test", "test", null)
            threeDS2Service.createTransactionError = error
            component.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, false, mock())

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_TRANSACTION_CREATION,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `fingerprint is submitted automatically and it fails, then error event is tracked`() = runTest {
            // GIVEN
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                Result.failure(IOException("test"))
            component.initialize(this)

            // WHEN
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            component.identifyShopper(Activity(), encodedJson, true, mock())

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2FingerprintError(
                event = ErrorEvent.API_THREEDS2,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `transaction is null for challenge, then error event is tracked`() = runTest {
            // WHEN
            component.challengeShopper(mock(), "token")

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_TRANSACTION_MISSING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `challenge token can't be decoded, then error event is tracked`() = runTest {
            // GIVEN
            initializeChallengeTransaction(this)

            // WHEN
            component.challengeShopper(Activity(), Base64.encode("token".toByteArray()))

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_TOKEN_DECODING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `challenge fails, then error event is tracked`() = runTest {
            // GIVEN
            initializeChallengeTransaction(this).apply {
                shouldThrowError = true
            }

            // WHEN
            component.challengeShopper(
                Activity(),
                Base64.encode("""{"messageVersion":"2.1.0"}""".toByteArray()),
            )

            // THEN
            val expectedEvent = AuthenticationEvents.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }
    }

    private fun getAuthenticationRequestParams() = TestAuthenticationRequestParameters(
        deviceData = "deviceData",
        sdkTransactionID = "sdkTransactionID",
        sdkAppID = "sdkAppID",
        sdkReferenceNumber = "sdkReferenceNumber",
        sdkEphemeralPublicKey = "{}",
        messageVersion = "messageVersion",
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private const val TEST_ACTION_TYPE = "TEST_ACTION_TYPE"
        private val TEST_FINGERPRINT_TOKEN =
            """
            {
                "directoryServerId":"id",
                "directoryServerPublicKey":"key",
                "directoryServerRootCertificates":"certs",
                "threeDSMessageVersion":"2.1.0"
            }
            """.trimIndent()

        @JvmStatic
        fun fingerprintResult() = listOf(
            Arguments.arguments(
                SubmitFingerprintResult.Completed(JSONObject()),
                AuthenticationEvents.Result.COMPLETED,
            ),
            Arguments.arguments(
                SubmitFingerprintResult.Redirect(RedirectAction()),
                AuthenticationEvents.Result.REDIRECT,
            ),
            Arguments.arguments(
                SubmitFingerprintResult.Threeds2(Threeds2Action()),
                AuthenticationEvents.Result.THREEDS2,
            ),
        )

        @JvmStatic
        fun challengeResult() = listOf(
            Arguments.arguments(
                ChallengeResult.Completed("transactionStatus"),
                AuthenticationEvents.Result.COMPLETED,
            ),
            Arguments.arguments(
                ChallengeResult.Cancelled("transactionStatus", "additionalDetails"),
                AuthenticationEvents.Result.CANCELLED,
            ),
            Arguments.arguments(
                ChallengeResult.Error("transactionStatus", "additionalDetails"),
                AuthenticationEvents.Result.ERROR,
            ),
            Arguments.arguments(
                ChallengeResult.Timeout("transactionStatus", "additionalDetails"),
                AuthenticationEvents.Result.TIMEOUT,
            ),
        )
    }
}

private class TestThreeDS2Service : ThreeDS2Service {

    var initializeResult: InitializeResult = InitializeResult.Success
    var initializeError: Throwable? = null
    var transactionResult: TransactionResult = TransactionResult.Success(TestTransaction())
    var createTransactionError: Throwable? = null

    override fun initialize(
        p0: android.content.Context?,
        p1: ConfigParameters?,
        p2: String?,
        p3: UiCustomization?
    ): InitializeResult {
        initializeError?.let { throw it }
        return initializeResult
    }

    override fun createTransaction(p0: String?, p1: String): TransactionResult {
        createTransactionError?.let { throw it }
        return transactionResult
    }

    override fun cleanup(p0: android.content.Context?) = Unit

    override fun getSDKVersion(): String = "1.2.3-test"

    override fun getWarnings(): MutableList<Warning> {
        error("Should not be called.")
    }
}

private class TestTransaction(
    val authReqParameters: AuthenticationRequestParameters? = null,
) : Transaction {

    var shouldThrowError: Boolean = false
    private var timesDoChallengeCalled = 0

    override fun getAuthenticationRequestParameters(): AuthenticationRequestParameters? = authReqParameters

    @Suppress("OVERRIDE_DEPRECATION", "deprecation")
    override fun doChallenge(p0: Activity?, p1: ChallengeParameters?, p2: ChallengeStatusReceiver?, p3: Int) = Unit

    override fun doChallenge(
        currentActivity: Activity?,
        challengeParameters: ChallengeParameters?,
        challengeStatusHandler: ChallengeStatusHandler?,
        timeOut: Int,
    ) {
        timesDoChallengeCalled++
        if (shouldThrowError) {
            throw InvalidInputException("test", null)
        }
    }

    override fun getProgressView(p0: Activity?): ProgressDialog {
        error("This method should not be used")
    }

    override fun close() = Unit

    fun assertDoChallengeCalled() {
        assertTrue(timesDoChallengeCalled > 0) { "Expected doChallenge to be called at least once" }
    }
}

private class TestAuthenticationRequestParameters(
    private val deviceData: String? = null,
    private val sdkTransactionID: String? = null,
    private val sdkAppID: String? = null,
    private val sdkReferenceNumber: String? = null,
    private val sdkEphemeralPublicKey: String? = null,
    private val messageVersion: String? = null,
) : AuthenticationRequestParameters {
    override fun getDeviceData(): String? = deviceData
    override fun getSDKTransactionID(): String? = sdkTransactionID
    override fun getSDKAppID(): String? = sdkAppID
    override fun getSDKReferenceNumber(): String? = sdkReferenceNumber
    override fun getSDKEphemeralPublicKey(): String? = sdkEphemeralPublicKey
    override fun getMessageVersion(): String? = messageVersion
}

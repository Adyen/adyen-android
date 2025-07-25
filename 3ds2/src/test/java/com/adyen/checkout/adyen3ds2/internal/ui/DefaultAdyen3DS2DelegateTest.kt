/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2022.
 */

@file:OptIn(ExperimentalEncodingApi::class)

package com.adyen.checkout.adyen3ds2.internal.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.adyen3ds2.Authentication3DS2Exception
import com.adyen.checkout.adyen3ds2.internal.analytics.ThreeDS2Events
import com.adyen.checkout.adyen3ds2.internal.data.api.SubmitFingerprintRepository
import com.adyen.checkout.adyen3ds2.internal.data.model.Adyen3DS2Serializer
import com.adyen.checkout.adyen3ds2.internal.data.model.SubmitFingerprintResult
import com.adyen.checkout.adyen3ds2.internal.ui.model.Adyen3DS2ComponentParamsMapper
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.action.Threeds2Action
import com.adyen.checkout.components.core.action.Threeds2ChallengeAction
import com.adyen.checkout.components.core.action.Threeds2FingerprintAction
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.core.old.exception.ComponentException
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.ui.core.old.internal.TestRedirectHandler
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class, MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultAdyen3DS2DelegateTest(
    @Mock private val submitFingerprintRepository: SubmitFingerprintRepository,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var redirectHandler: TestRedirectHandler
    private lateinit var delegate: DefaultAdyen3DS2Delegate
    private lateinit var paymentDataRepository: PaymentDataRepository

    private val threeDS2Service: TestThreeDS2Service = TestThreeDS2Service()

    @BeforeEach
    fun setup() {
        analyticsManager = TestAnalyticsManager()
        redirectHandler = TestRedirectHandler()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        delegate = createDelegate()
    }

    private fun createDelegate(
        adyen3DS2Serializer: Adyen3DS2Serializer = Adyen3DS2Serializer(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ): DefaultAdyen3DS2Delegate {
        val configuration = CheckoutConfiguration(Environment.TEST, TEST_CLIENT_KEY)
        return DefaultAdyen3DS2Delegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = savedStateHandle,
            componentParams = Adyen3DS2ComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null)
                // Set it to null to avoid a crash in 3DS2 library (they use Android APIs)
                .copy(deviceParameterBlockList = null),
            submitFingerprintRepository = submitFingerprintRepository,
            paymentDataRepository = paymentDataRepository,
            adyen3DS2Serializer = adyen3DS2Serializer,
            redirectHandler = redirectHandler,
            threeDS2Service = threeDS2Service,
            coroutineDispatcher = UnconfinedTestDispatcher(),
            application = Application(),
            analyticsManager = analyticsManager,
        )
    }

    @Nested
    @DisplayName("when handling")
    inner class HandleActionTest {

        @Test
        fun `Threeds2FingerprintAction and token is null, then an exception is thrown`() = runTest {
            delegate.initialize(this)
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleAction(Threeds2FingerprintAction(token = null), Activity())

            assertTrue(exceptionFlow.latestValue is ComponentException)
        }

        @Test
        fun `Threeds2ChallengeAction and token is null, then an exception is thrown`() = runTest {
            delegate.initialize(this)
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleAction(Threeds2ChallengeAction(token = null), Activity())

            assertTrue(exceptionFlow.latestValue is ComponentException)
        }

        @Test
        fun `Threeds2Action and token is null, then an exception is thrown`() = runTest {
            delegate.initialize(this)
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleAction(Threeds2Action(token = null), Activity())

            assertTrue(exceptionFlow.latestValue is ComponentException)
        }

        @Test
        fun `Threeds2Action and sub type is null, then an exception is thrown`() = runTest {
            delegate.initialize(this)
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleAction(Threeds2Action(token = "sometoken", subtype = null), Activity())

            assertTrue(exceptionFlow.latestValue is ComponentException)
        }
    }

    @Nested
    @DisplayName("when identifying shopper and")
    inner class IdentifyShopperTest {

        @Test
        fun `fingerprint is malformed, then an exception is thrown`() = runTest {
            delegate.initialize(this)
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            val encodedJson = Base64.encode("{incorrectJson}".toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            assertTrue(exceptionFlow.latestValue is ComponentException)
        }

        @Test
        fun `when fingerprintToken is partial, then an exception is emitted`() = runTest {
            val partialFingerprintToken =
                """
                {
                    "directoryServerId":"id",
                }
                """.trimIndent()
            delegate.initialize(this)
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            val encodedJson = Base64.encode(partialFingerprintToken.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            assertTrue(exceptionFlow.latestValue is ComponentException)
        }

        @Test
        fun `3ds2 sdk throws an exception while initializing, then an exception emitted`() = runTest {
            val error = InvalidInputException("test", null)
            threeDS2Service.initializeError = error
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            assertEquals(error, exceptionFlow.latestValue.cause)
        }

        @Test
        fun `3ds2 sdk returns an initialization error, then details are emitted`() = runTest {
            val transStatus = "X"
            val additionalDetails = "mockAdditionalDetails"
            threeDS2Service.initializeResult = InitializeResult.Failure(transStatus, additionalDetails)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

            val detailsFlow = delegate.detailsFlow.test(testScheduler)

            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            // We don't care about the encoded value in this test, we just want to know if details are there
            assertNotNull(detailsFlow.latestValue.details)
        }

        @Test
        fun `creating 3ds2 transaction fails and the error is SDKNotInitializedException, then an exception emitted`() =
            runTest {
                val error = SDKNotInitializedException()
                threeDS2Service.createTransactionError = error
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

                val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
                delegate.identifyShopper(Activity(), encodedJson, false)

                assertEquals(error, exceptionFlow.latestValue.cause)
            }

        @Test
        fun `creating 3ds2 transaction fails and the error is SDKRuntimeException, then an exception emitted`() =
            runTest {
                val error = SDKRuntimeException("test", "test", null)
                threeDS2Service.createTransactionError = error
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

                val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
                delegate.identifyShopper(Activity(), encodedJson, false)

                assertEquals(error, exceptionFlow.latestValue.cause)
            }

        @Test
        fun `creating 3ds2 transaction return transaction error, then details are emitted`() = runTest {
            threeDS2Service.transactionResult = TransactionResult.Failure("X", "mockDetails")
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val detailsFlow = delegate.detailsFlow.test(testScheduler)

            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            // We don't care about the encoded value in this test, we just want to know if details are there
            assertNotNull(detailsFlow.latestValue.details)
        }

        @Test
        fun `transaction parameters are null, then an exception emitted`() = runTest {
            delegate.initialize(this)
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            assertEquals("Failed to retrieve 3DS2 authentication parameters", exceptionFlow.latestValue.message)
        }

        @Test
        fun `fingerprint is submitted automatically and result is completed, then details are emitted`() = runTest {
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            val submitFingerprintResult = SubmitFingerprintResult.Completed(JSONObject())
            whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                Result.success(submitFingerprintResult)
            val detailsFlow = delegate.detailsFlow.test(testScheduler)

            delegate.initialize(this)

            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, true)

            val expected = ActionComponentData(
                paymentData = null,
                details = submitFingerprintResult.details,
            )
            assertEquals(expected, detailsFlow.latestValue)
        }

        @Test
        fun `fingerprint is submitted automatically and result is redirect, then redirect should be handled`() =
            runTest {
                threeDS2Service.transactionResult =
                    TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
                val submitFingerprintResult = SubmitFingerprintResult.Redirect(RedirectAction())
                whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                    Result.success(submitFingerprintResult)

                delegate.initialize(this)

                val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
                delegate.identifyShopper(Activity(), encodedJson, true)

                redirectHandler.assertLaunchRedirectCalled()
            }

        @Test
        fun `fingerprint is submitted automatically and it fails, then an exception is emitted`() = runTest {
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            val error = IOException("test")
            whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                Result.failure(error)
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.initialize(this)

            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, true)
            assertEquals(error, exceptionFlow.latestValue.cause)
        }

        @Test
        fun `fingerprint is not submitted automatically, then details are emitted`() = runTest {
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            val detailsFlow = delegate.detailsFlow.test(testScheduler)
            delegate.initialize(this)

            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            assertNotNull(detailsFlow.latestValue.details)
        }
    }

    @Nested
    @DisplayName("when challenging shopper and")
    inner class ChallengeShopperTest {

        @Test
        fun `transaction is null, then an exception is emitted`() = runTest {
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)
            delegate.challengeShopper(mock(), "token")

            assertTrue(exceptionFlow.latestValue is Authentication3DS2Exception)
        }

        @Test
        fun `token can't be decoded, then an exception is emitted`() = runTest {
            initializeChallengeTransaction(this)
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.challengeShopper(Activity(), Base64.encode("token".toByteArray()))

            assertTrue(exceptionFlow.latestValue.cause is JSONException)
        }

        @Test
        fun `everything is good, then challenge should be executed`() = runTest {
            val transaction = initializeChallengeTransaction(this)

            // We need to set the messageVersion to workaround an error in the 3DS2 SDK
            delegate.challengeShopper(Activity(), Base64.encode("{\"messageVersion\":\"2.1.0\"}".toByteArray()))

            transaction.assertDoChallengeCalled()
        }

        @Test
        fun `challenge fails, then an exception is emitted`() = runTest {
            initializeChallengeTransaction(this).apply {
                shouldThrowError = true
            }

            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            // We need to set the messageVersion to workaround an error in the 3DS2 SDK
            delegate.challengeShopper(Activity(), Base64.encode("{\"messageVersion\":\"2.1.0\"}".toByteArray()))

            assertTrue(exceptionFlow.latestValue.cause is InvalidInputException)
        }
    }

    private fun initializeChallengeTransaction(scope: CoroutineScope): TestTransaction {
        val transaction = TestTransaction(getAuthenticationRequestParams())
        threeDS2Service.transactionResult = TransactionResult.Success(transaction)

        delegate.initialize(scope)

        val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
        delegate.identifyShopper(Activity(), encodedJson, false)

        return transaction
    }

    @Nested
    @DisplayName("when handling intent and")
    inner class HandleIntentTest {

        @Test
        fun `result is parsed, then details are emitted`() = runTest {
            val detailsFlow = delegate.detailsFlow.test(testScheduler)

            delegate.handleIntent(Intent())

            val expected = ActionComponentData(
                paymentData = null,
                details = TestRedirectHandler.REDIRECT_RESULT,
            )
            assertEquals(expected, detailsFlow.latestValue)
        }

        @Test
        fun `parsing fails, then an exception is emitted`() = runTest {
            val error = ComponentException("yes")
            redirectHandler.exception = error
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleIntent(Intent())

            assertEquals(error, exceptionFlow.latestValue)
        }
    }

    @Nested
    @DisplayName("when transaction is")
    inner class TransactionTest {

        @Test
        fun `completed, then details are emitted`() = runTest {
            val details =
                JSONObject("{\"threeds2.challengeResult\":\"eyJ0cmFuc1N0YXR1cyI6InRyYW5zYWN0aW9uU3RhdHVzIn0=\"}")
            val detailsFlow = delegate.detailsFlow.test(testScheduler)

            delegate.onCompletion(
                result = ChallengeResult.Completed(
                    transactionStatus = "transactionStatus",
                ),
            )

            val expected = ActionComponentData(
                paymentData = null,
                details = details,
            )
            assertEquals(expected.details.toString(), detailsFlow.latestValue.details.toString())
        }

        @Test
        fun `completed and creating details fails, then an error is emitted`() = runTest {
            val error = ComponentException("test")
            // We have to mock the serializer in order to throw an exception
            val adyen3DS2Serializer: Adyen3DS2Serializer = mock()
            whenever(adyen3DS2Serializer.createChallengeDetails(transactionStatus = "transactionStatus")) doAnswer {
                throw error
            }
            delegate = createDelegate(adyen3DS2Serializer)
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.onCompletion(
                result = ChallengeResult.Completed(
                    transactionStatus = "transactionStatus",
                ),
            )

            assertEquals(error, exceptionFlow.latestValue)
        }

        @Test
        fun `cancelled, then details are emitted`() = runTest {
            val detailsFlow = delegate.detailsFlow.test(testScheduler)

            delegate.onCompletion(
                result = ChallengeResult.Cancelled(
                    transactionStatus = "transactionStatus",
                    additionalDetails = "additionalDetails",
                ),
            )

            assertNotNull(detailsFlow.latestValue.details)
        }

        @Test
        fun `timed out, then details are emitted`() = runTest {
            val detailsFlow = delegate.detailsFlow.test(testScheduler)

            delegate.onCompletion(
                result = ChallengeResult.Timeout(
                    transactionStatus = "transactionStatus",
                    additionalDetails = "additionalDetails",
                ),
            )

            assertNotNull(detailsFlow.latestValue.details)
        }

        @Test
        fun `error, then details are emitted`() = runTest {
            val detailsFlow = delegate.detailsFlow.test(testScheduler)

            delegate.onCompletion(
                result = ChallengeResult.Error(
                    transactionStatus = "transactionStatus",
                    additionalDetails = "additionalDetails",
                ),
            )

            assertNotNull(detailsFlow.latestValue.details)
        }
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when handleAction is called for Threeds2FingerprintAction, then action event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val action = Threeds2FingerprintAction(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                type = TEST_ACTION_TYPE,
                token = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray()),
            )

            delegate.handleAction(action, Activity())

            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @Test
        fun `when handleAction is called for Threeds2ChallengeAction, then action event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val action = Threeds2ChallengeAction(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                type = TEST_ACTION_TYPE,
                token = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray()),
            )

            delegate.handleAction(action, Activity())

            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @Test
        fun `when handleAction is called for Threeds2Action and subType is fingerprint, then action event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val action = Threeds2Action(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                type = TEST_ACTION_TYPE,
                subtype = Threeds2Action.SubType.FINGERPRINT.value,
                token = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray()),
            )

            delegate.handleAction(action, Activity())

            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @Test
        fun `when handleAction is called for Threeds2Action and subType is challenge, then action event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val action = Threeds2Action(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                type = TEST_ACTION_TYPE,
                subtype = Threeds2Action.SubType.CHALLENGE.value,
                token = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray()),
            )

            delegate.handleAction(action, Activity())

            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @Test
        fun `when identifyShopper is called, then event is tracked`() = runTest {
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.initialize(this)

            delegate.identifyShopper(Activity(), encodedJson, true)

            val expectedEvent = ThreeDS2Events.threeDS2Fingerprint(
                subType = ThreeDS2Events.SubType.FINGERPRINT_DATA_SENT,
            )
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.adyen3ds2.internal.ui.DefaultAdyen3DS2DelegateTest#fingerprintResult")
        fun `when fingerprint result is returned, then event is tracked`(
            fingerprintResult: SubmitFingerprintResult,
            analyticsResult: ThreeDS2Events.Result
        ) = runTest {
            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                Result.success(fingerprintResult)
            delegate.initialize(this)

            delegate.identifyShopper(Activity(), encodedJson, true)

            val expectedEvent = ThreeDS2Events.threeDS2Fingerprint(
                subType = ThreeDS2Events.SubType.FINGERPRINT_COMPLETED,
                result = analyticsResult,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when challengeShopper is called, then event is tracked`() = runTest {
            initializeChallengeTransaction(this)
            // We need to set the messageVersion to workaround an error in the 3DS2 SDK
            delegate.challengeShopper(Activity(), Base64.encode("{\"messageVersion\":\"2.1.0\"}".toByteArray()))

            val expectedDataSentEvent = ThreeDS2Events.threeDS2Challenge(
                subType = ThreeDS2Events.SubType.CHALLENGE_DATA_SENT,
            )
            analyticsManager.assertHasEventEquals(expectedDataSentEvent)

            val expectedDisplayedEvent = ThreeDS2Events.threeDS2Challenge(
                subType = ThreeDS2Events.SubType.CHALLENGE_DISPLAYED,
            )
            analyticsManager.assertLastEventEquals(expectedDisplayedEvent)
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.adyen3ds2.internal.ui.DefaultAdyen3DS2DelegateTest#challengeResult")
        fun `when challenge result is returned, then event is tracked`(
            challengeResult: ChallengeResult,
            analyticsResult: ThreeDS2Events.Result
        ) = runTest {
            delegate.onCompletion(
                result = challengeResult,
            )

            val expectedEvent = ThreeDS2Events.threeDS2Challenge(
                subType = ThreeDS2Events.SubType.CHALLENGE_COMPLETED,
                result = analyticsResult,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when action is Threeds2FingerprintAction and token is null, then error event is tracked`() = runTest {
            delegate.initialize(this)

            delegate.handleAction(Threeds2FingerprintAction(token = null), Activity())

            val expectedEvent = ThreeDS2Events.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_TOKEN_MISSING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when action is Threeds2ChallengeAction and token is null, then error event is tracked`() = runTest {
            delegate.initialize(this)

            delegate.handleAction(Threeds2ChallengeAction(token = null), Activity())

            val expectedEvent = ThreeDS2Events.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_TOKEN_MISSING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when action is Threeds2Action and token is null, then error event is tracked`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

            delegate.handleAction(
                Threeds2Action(token = null, subtype = Threeds2Action.SubType.FINGERPRINT.value),
                Activity(),
            )
            val expectedFingerprintEvent = ThreeDS2Events.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_TOKEN_MISSING,
            )
            analyticsManager.assertLastEventEquals(expectedFingerprintEvent)

            delegate.handleAction(
                Threeds2Action(token = null, subtype = Threeds2Action.SubType.CHALLENGE.value),
                Activity(),
            )
            val expectedChallengeEvent = ThreeDS2Events.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_TOKEN_MISSING,
            )
            analyticsManager.assertLastEventEquals(expectedChallengeEvent)
        }

        @Test
        fun `when fingerprintToken is partial, then error event is tracked`() = runTest {
            val partialFingerprintToken =
                """
                {
                    "directoryServerId":"id",
                }
                """.trimIndent()
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

            val encodedJson = Base64.encode(partialFingerprintToken.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            val expectedEvent = ThreeDS2Events.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_FINGERPRINT_CREATION,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when 3ds2 sdk throws an exception while initializing, then error event is tracked`() = runTest {
            threeDS2Service.initializeError = InvalidInputException("test", null)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            val expectedEvent = ThreeDS2Events.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_FINGERPRINT_HANDLING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when transaction parameters are null, then error event is tracked`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            val expectedEvent = ThreeDS2Events.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_FINGERPRINT_CREATION,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when creating 3ds2 transaction fails and the threeDSMessageVersion is null, then error event is tracked`() =
            runTest {
                val fingerprintTokenWithoutMessageVersion =
                    """
                    {
                        "directoryServerId":"id",
                        "directoryServerPublicKey":"key",
                        "directoryServerRootCertificates":"certs",
                    }
                    """.trimIndent()
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                val encodedJson = Base64.encode(fingerprintTokenWithoutMessageVersion.toByteArray())
                delegate.identifyShopper(Activity(), encodedJson, false)

                val expectedEvent = ThreeDS2Events.threeDS2FingerprintError(
                    event = ErrorEvent.THREEDS2_TRANSACTION_CREATION,
                )
                analyticsManager.assertLastEventEquals(expectedEvent)
            }

        @Test
        fun `when creating 3ds2 transaction fails and error is SDKNotInitializedException, then error event is tracked`() =
            runTest {
                val error = SDKNotInitializedException()
                threeDS2Service.createTransactionError = error
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
                delegate.identifyShopper(Activity(), encodedJson, false)

                val expectedEvent = ThreeDS2Events.threeDS2FingerprintError(
                    event = ErrorEvent.THREEDS2_TRANSACTION_CREATION,
                )
                analyticsManager.assertLastEventEquals(expectedEvent)
            }

        @Test
        fun `when creating 3ds2 transaction fails and error is SDKRuntimeException, then error event is tracked`() =
            runTest {
                val error = SDKRuntimeException("test", "test", null)
                threeDS2Service.createTransactionError = error
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
                delegate.identifyShopper(Activity(), encodedJson, false)

                val expectedEvent = ThreeDS2Events.threeDS2FingerprintError(
                    event = ErrorEvent.THREEDS2_TRANSACTION_CREATION,
                )
                analyticsManager.assertLastEventEquals(expectedEvent)
            }

        @Test
        fun `when fingerprint is submitted automatically and it fails, then error event is tracked`() = runTest {
            threeDS2Service.transactionResult =
                TransactionResult.Success(TestTransaction(getAuthenticationRequestParams()))
            whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                Result.failure(IOException("test"))

            delegate.initialize(this)

            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, true)

            val expectedEvent = ThreeDS2Events.threeDS2FingerprintError(
                event = ErrorEvent.API_THREEDS2,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when transaction is null, then error event is tracked`() = runTest {
            delegate.challengeShopper(mock(), "token")

            val expectedEvent = ThreeDS2Events.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_TRANSACTION_MISSING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when fingerprint token can't be decoded, then error event is tracked`() = runTest {
            delegate.initialize(this)

            val encodedJson = Base64.encode("{incorrectJson}".toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            val expectedEvent = ThreeDS2Events.threeDS2FingerprintError(
                event = ErrorEvent.THREEDS2_TOKEN_DECODING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when challenge token can't be decoded, then error event is tracked`() = runTest {
            initializeChallengeTransaction(this)

            delegate.challengeShopper(Activity(), Base64.encode("token".toByteArray()))

            val expectedEvent = ThreeDS2Events.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_TOKEN_DECODING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when challenge fails, then error event is tracked`() = runTest {
            initializeChallengeTransaction(this).apply {
                shouldThrowError = true
            }

            // We need to set the messageVersion to workaround an error in the 3DS2 SDK
            delegate.challengeShopper(Activity(), Base64.encode("{\"messageVersion\":\"2.1.0\"}".toByteArray()))

            val expectedEvent = ThreeDS2Events.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when completed and creating details fails, then error event is tracked`() = runTest {
            val adyen3DS2Serializer: Adyen3DS2Serializer = mock()
            whenever(adyen3DS2Serializer.createChallengeDetails(any(), anyOrNull())) doAnswer {
                throw ComponentException("test")
            }
            delegate = createDelegate(adyen3DS2Serializer)

            delegate.onCompletion(
                result = ChallengeResult.Completed("transactionStatus"),
            )

            val expectedEvent = ThreeDS2Events.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when timed out and creating details fails, then error event is tracked`() = runTest {
            val adyen3DS2Serializer: Adyen3DS2Serializer = mock()
            whenever(adyen3DS2Serializer.createChallengeDetails(any(), any())) doAnswer {
                throw ComponentException("test")
            }
            delegate = createDelegate(adyen3DS2Serializer)

            delegate.onCompletion(
                result = ChallengeResult.Timeout("transactionStatus", "additionalDetails"),
            )

            val expectedEvent = ThreeDS2Events.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when error and creating details fails, then error event is tracked`() = runTest {
            val adyen3DS2Serializer: Adyen3DS2Serializer = mock()
            whenever(adyen3DS2Serializer.createChallengeDetails(any(), any())) doAnswer {
                throw ComponentException("test")
            }
            delegate = createDelegate(adyen3DS2Serializer)

            delegate.onCompletion(
                result = ChallengeResult.Error("transactionStatus", "additionalDetails"),
            )

            val expectedEvent = ThreeDS2Events.threeDS2ChallengeError(
                event = ErrorEvent.THREEDS2_CHALLENGE_HANDLING,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }
    }

    @Test
    fun `when details are emitted, then state is cleared`() = runTest {
        val savedStateHandle = SavedStateHandle().apply {
            set(
                DefaultAdyen3DS2Delegate.ACTION_KEY,
                Threeds2Action(paymentMethodType = "test", paymentData = "paymentData"),
            )
        }
        delegate = createDelegate(savedStateHandle = savedStateHandle)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.onCompletion(ChallengeResult.Completed("test"))

        assertStateCleared(savedStateHandle)
    }

    @Test
    fun `when an error is emitted, then state is cleared`() = runTest {
        val savedStateHandle = SavedStateHandle()
        delegate = createDelegate(savedStateHandle = savedStateHandle)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.handleAction(Threeds2Action(paymentMethodType = "test", paymentData = "paymentData"), Activity())

        assertStateCleared(savedStateHandle)
    }

    private fun assertStateCleared(savedStateHandle: SavedStateHandle) {
        assertNull(savedStateHandle[DefaultAdyen3DS2Delegate.ACTION_KEY])
        SharedChallengeStatusHandler.onCompletionListener = null
    }

    private fun getAuthenticationRequestParams() = TestAuthenticationRequestParameters(
        deviceData = "deviceData",
        sdkTransactionID = "sdkTransactionID",
        sdkAppID = "sdkAppID",
        sdkReferenceNumber = "sdkReferenceNumber",
        sdkEphemeralPublicKey = "{}",
        messageVersion = "messageVersion",
    )

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
            // fingerprintResult, analyticsResult
            Arguments.arguments(SubmitFingerprintResult.Completed(JSONObject()), ThreeDS2Events.Result.COMPLETED),
            Arguments.arguments(SubmitFingerprintResult.Redirect(RedirectAction()), ThreeDS2Events.Result.REDIRECT),
            Arguments.arguments(SubmitFingerprintResult.Threeds2(Threeds2Action()), ThreeDS2Events.Result.THREEDS2),
        )

        @JvmStatic
        fun challengeResult() = listOf(
            // challengeResult, analyticsResult
            Arguments.arguments(
                ChallengeResult.Completed("transactionStatus"),
                ThreeDS2Events.Result.COMPLETED,
            ),
            Arguments.arguments(
                ChallengeResult.Cancelled("transactionStatus", "additionalDetails"),
                ThreeDS2Events.Result.CANCELLED,
            ),
            Arguments.arguments(
                ChallengeResult.Error("transactionStatus", "additionalDetails"),
                ThreeDS2Events.Result.ERROR,
            ),
            Arguments.arguments(
                ChallengeResult.Timeout("transactionStatus", "additionalDetails"),
                ThreeDS2Events.Result.TIMEOUT,
            ),
        )
    }
}

private class TestThreeDS2Service : ThreeDS2Service {

    var initializeResult: InitializeResult = InitializeResult.Success

    var initializeError: Throwable? = null

    var transactionResult: TransactionResult = TransactionResult.Success(TestTransaction())

    var createTransactionError: Throwable? = null

    private var didCallInitialize = false

    private var didCallCleanup = false

    override fun initialize(p0: Context?, p1: ConfigParameters?, p2: String?, p3: UiCustomization?): InitializeResult {
        didCallInitialize = true
        initializeError?.let { throw it }
        return initializeResult
    }

    override fun createTransaction(p0: String?, p1: String): TransactionResult {
        createTransactionError?.let { throw it }
        return transactionResult
    }

    override fun cleanup(p0: Context?) {
        didCallCleanup = true
    }

    override fun getSDKVersion(): String {
        return TEST_SDK_VERSION
    }

    override fun getWarnings(): MutableList<Warning> {
        error("Should not be called.")
    }

    companion object {
        private const val TEST_SDK_VERSION = "1.2.3-test"
    }
}

private class TestTransaction(
    val authReqParameters: AuthenticationRequestParameters? = null
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
        timeOut: Int
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
        assert(timesDoChallengeCalled > 0)
    }
}

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
import com.adyen.checkout.adyen3ds2.Cancelled3DS2Exception
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
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.ui.core.internal.test.TestRedirectHandler
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
import org.junit.jupiter.api.extension.ExtendWith
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
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
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
        adyen3DS2Serializer: Adyen3DS2Serializer = Adyen3DS2Serializer()
    ): DefaultAdyen3DS2Delegate {
        val configuration = CheckoutConfiguration(Environment.TEST, TEST_CLIENT_KEY)
        return DefaultAdyen3DS2Delegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = SavedStateHandle(),
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
        fun `creating 3ds2 transaction fails, then an exception emitted`() = runTest {
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
            val authReqParams = TestAuthenticationRequestParameters(
                deviceData = "deviceData",
                sdkTransactionID = "sdkTransactionID",
                sdkAppID = "sdkAppID",
                sdkReferenceNumber = "sdkReferenceNumber",
                sdkEphemeralPublicKey = "{}",
                messageVersion = "messageVersion",
            )
            threeDS2Service.transactionResult = TransactionResult.Success(TestTransaction(authReqParams))
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
                val authReqParams = TestAuthenticationRequestParameters(
                    deviceData = "deviceData",
                    sdkTransactionID = "sdkTransactionID",
                    sdkAppID = "sdkAppID",
                    sdkReferenceNumber = "sdkReferenceNumber",
                    sdkEphemeralPublicKey = "{}",
                    messageVersion = "messageVersion",
                )
                threeDS2Service.transactionResult = TransactionResult.Success(TestTransaction(authReqParams))
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
            val authReqParams = TestAuthenticationRequestParameters(
                deviceData = "deviceData",
                sdkTransactionID = "sdkTransactionID",
                sdkAppID = "sdkAppID",
                sdkReferenceNumber = "sdkReferenceNumber",
                sdkEphemeralPublicKey = "{}",
                messageVersion = "messageVersion",
            )
            threeDS2Service.transactionResult = TransactionResult.Success(TestTransaction(authReqParams))
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
            val authReqParams = TestAuthenticationRequestParameters(
                deviceData = "deviceData",
                sdkTransactionID = "sdkTransactionID",
                sdkAppID = "sdkAppID",
                sdkReferenceNumber = "sdkReferenceNumber",
                sdkEphemeralPublicKey = "{}",
                messageVersion = "messageVersion",
            )
            threeDS2Service.transactionResult = TransactionResult.Success(TestTransaction(authReqParams))
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
            initializeTransaction(this)
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.challengeShopper(Activity(), Base64.encode("token".toByteArray()))

            assertTrue(exceptionFlow.latestValue.cause is JSONException)
        }

        @Test
        fun `everything is good, then challenge should be executed`() = runTest {
            val transaction = initializeTransaction(this)

            // We need to set the messageVersion to workaround an error in the 3DS2 SDK
            delegate.challengeShopper(Activity(), Base64.encode("{\"messageVersion\":\"2.1.0\"}".toByteArray()))

            transaction.assertDoChallengeCalled()
        }

        @Test
        fun `challenge fails, then an exception is emitted`() = runTest {
            initializeTransaction(this).apply {
                shouldThrowError = true
            }

            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            // We need to set the messageVersion to workaround an error in the 3DS2 SDK
            delegate.challengeShopper(Activity(), Base64.encode("{\"messageVersion\":\"2.1.0\"}".toByteArray()))

            assertTrue(exceptionFlow.latestValue.cause is InvalidInputException)
        }

        private fun initializeTransaction(scope: CoroutineScope): TestTransaction {
            val authReqParams = TestAuthenticationRequestParameters(
                deviceData = "deviceData",
                sdkTransactionID = "sdkTransactionID",
                sdkAppID = "sdkAppID",
                sdkReferenceNumber = "sdkReferenceNumber",
                sdkEphemeralPublicKey = "{}",
                messageVersion = "2.1.0",
            )
            val transaction = TestTransaction(authReqParams)
            threeDS2Service.transactionResult = TransactionResult.Success(transaction)

            delegate.initialize(scope)

            val encodedJson = Base64.encode(TEST_FINGERPRINT_TOKEN.toByteArray())
            delegate.identifyShopper(Activity(), encodedJson, false)

            return transaction
        }
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
        fun `cancelled, then an error is emitted`() = runTest {
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.onCompletion(
                result = ChallengeResult.Cancelled(
                    transactionStatus = "transactionStatus",
                    additionalDetails = "additionalDetails",
                ),
            )

            assertTrue(exceptionFlow.latestValue is Cancelled3DS2Exception)
        }

        @Test
        fun `timedout, then details are emitted`() = runTest {
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
                message = DefaultAdyen3DS2Delegate.ANALYTICS_MESSAGE_FINGERPRINT,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
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
                message = DefaultAdyen3DS2Delegate.ANALYTICS_MESSAGE_CHALLENGE,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
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
                message = DefaultAdyen3DS2Delegate.ANALYTICS_MESSAGE_FINGERPRINT,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
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
                message = DefaultAdyen3DS2Delegate.ANALYTICS_MESSAGE_CHALLENGE,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
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

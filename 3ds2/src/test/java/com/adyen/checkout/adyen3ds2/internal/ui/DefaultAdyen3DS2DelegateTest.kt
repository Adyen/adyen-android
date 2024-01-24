/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2022.
 */

package com.adyen.checkout.adyen3ds2.internal.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
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
import com.adyen.checkout.components.core.internal.util.JavaBase64Encoder
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.ui.core.internal.test.TestRedirectHandler
import com.adyen.threeds2.AuthenticationRequestParameters
import com.adyen.threeds2.ChallengeResult
import com.adyen.threeds2.ChallengeStatusHandler
import com.adyen.threeds2.ChallengeStatusReceiver
import com.adyen.threeds2.ProgressDialog
import com.adyen.threeds2.ThreeDS2Service
import com.adyen.threeds2.Transaction
import com.adyen.threeds2.exception.InvalidInputException
import com.adyen.threeds2.exception.SDKRuntimeException
import com.adyen.threeds2.parameters.ChallengeParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.Locale

@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultAdyen3DS2DelegateTest(
    @Mock private val submitFingerprintRepository: SubmitFingerprintRepository,
    @Mock private val adyen3DS2Serializer: Adyen3DS2Serializer,
    @Mock private val threeDS2Service: ThreeDS2Service,
) {

    private lateinit var redirectHandler: TestRedirectHandler
    private lateinit var delegate: DefaultAdyen3DS2Delegate
    private lateinit var paymentDataRepository: PaymentDataRepository

    private val base64Encoder = JavaBase64Encoder()

    @BeforeEach
    fun beforeEach(dispatcher: TestDispatcher) {
        redirectHandler = TestRedirectHandler()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        val configuration = CheckoutConfiguration(Locale.US, Environment.TEST, TEST_CLIENT_KEY)
        delegate = DefaultAdyen3DS2Delegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = SavedStateHandle(),
            componentParams = Adyen3DS2ComponentParamsMapper(null, null)
                .mapToParams(configuration, null)
                // Set it to null to avoid a crash in 3DS2 library (they use Android APIs)
                .copy(deviceParameterBlockList = null),
            submitFingerprintRepository = submitFingerprintRepository,
            paymentDataRepository = paymentDataRepository,
            adyen3DS2Serializer = adyen3DS2Serializer,
            redirectHandler = redirectHandler,
            threeDS2Service = threeDS2Service,
            defaultDispatcher = dispatcher,
            base64Encoder = base64Encoder,
            application = Application(),
        )
    }

    @Nested
    @DisplayName("when handling")
    inner class HandleActionTest {

        @Test
        fun `Threeds2FingerprintAction and token is null, then an exception is thrown`(
            dispatcher: TestDispatcher
        ) = runTest {
            delegate.initialize(CoroutineScope(dispatcher))

            delegate.exceptionFlow.test {
                delegate.handleAction(Threeds2FingerprintAction(token = null), Activity())

                assertTrue(awaitItem() is ComponentException)
            }
        }

        @Test
        fun `Threeds2ChallengeAction and token is null, then an exception is thrown`(
            dispatcher: TestDispatcher
        ) = runTest {
            delegate.initialize(CoroutineScope(dispatcher))

            delegate.exceptionFlow.test {
                delegate.handleAction(Threeds2ChallengeAction(token = null), Activity())

                assertTrue(awaitItem() is ComponentException)
            }
        }

        @Test
        fun `Threeds2Action and token is null, then an exception is thrown`(
            dispatcher: TestDispatcher
        ) = runTest {
            delegate.initialize(CoroutineScope(dispatcher))

            delegate.exceptionFlow.test {
                delegate.handleAction(Threeds2Action(token = null), Activity())

                assertTrue(awaitItem() is ComponentException)
            }
        }

        @Test
        fun `Threeds2Action and sub type is null, then an exception is thrown`(
            dispatcher: TestDispatcher
        ) = runTest {
            delegate.initialize(CoroutineScope(dispatcher))

            delegate.exceptionFlow.test {
                delegate.handleAction(Threeds2Action(token = "sometoken", subtype = null), Activity())

                assertTrue(awaitItem() is ComponentException)
            }
        }
    }

    @Nested
    @DisplayName("when identifying shopper and")
    inner class IdentifyShopperTest {

        @Test
        fun `fingerprint is malformed, then an exception is thrown`(dispatcher: TestDispatcher) = runTest {
            delegate.initialize(CoroutineScope(dispatcher))

            assertThrows<ComponentException> {
                val encodedJson = base64Encoder.encode("{incorrectJson}")
                delegate.identifyShopper(Activity(), encodedJson, false)
            }
        }

        @Test
        fun `3ds2 sdk throws an exception while initializing, then an exception emitted`(dispatcher: TestDispatcher) =
            runTest {
                val error = SDKRuntimeException("test", "test", null)
                whenever(threeDS2Service.initialize(any(), any(), anyOrNull(), anyOrNull())) doAnswer {
                    throw error
                }
                delegate.initialize(CoroutineScope(dispatcher))

                delegate.exceptionFlow.test {
                    val encodedJson = base64Encoder.encode(
                        """
                            {
                            "directoryServerId":"id",
                            "directoryServerPublicKey":"key"
                            }
                        """.trimIndent(),
                    )
                    delegate.identifyShopper(Activity(), encodedJson, false)

                    assertEquals(error, awaitItem().cause)
                }
            }

        @Test
        fun `creating 3ds2 transaction fails, then an exception emitted`(dispatcher: TestDispatcher) = runTest {
            val error = SDKRuntimeException("test", "test", null)
            whenever(threeDS2Service.createTransaction(anyOrNull(), any())) doAnswer {
                throw error
            }
            delegate.initialize(CoroutineScope(dispatcher))

            delegate.exceptionFlow.test {
                val encodedJson = base64Encoder.encode(TEST_FINGERPRINT_TOKEN)
                delegate.identifyShopper(Activity(), encodedJson, false)

                assertEquals(error, awaitItem().cause)
            }
        }

        @Test
        fun `transaction parameters are null, then an exception emitted`(dispatcher: TestDispatcher) = runTest {
            whenever(threeDS2Service.createTransaction(anyOrNull(), any())) doReturn TestTransaction()
            delegate.initialize(CoroutineScope(dispatcher))

            delegate.exceptionFlow.test {
                val encodedJson = base64Encoder.encode(TEST_FINGERPRINT_TOKEN)
                delegate.identifyShopper(Activity(), encodedJson, false)

                assertTrue(awaitItem() is ComponentException)
            }
        }

        @Test
        fun `fingerprint is submitted automatically and result is completed, then details are emitted`(
            dispatcher: TestDispatcher
        ) =
            runTest {
                val authReqParams = TestAuthenticationRequestParameters(
                    deviceData = "deviceData",
                    sdkTransactionID = "sdkTransactionID",
                    sdkAppID = "sdkAppID",
                    sdkReferenceNumber = "sdkReferenceNumber",
                    sdkEphemeralPublicKey = "{}",
                    messageVersion = "messageVersion",
                )
                whenever(threeDS2Service.createTransaction(anyOrNull(), any())) doReturn TestTransaction(authReqParams)
                val submitFingerprintResult = SubmitFingerprintResult.Completed(JSONObject())
                whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                    Result.success(submitFingerprintResult)

                delegate.initialize(CoroutineScope(dispatcher))

                delegate.detailsFlow.test {
                    val encodedJson = base64Encoder.encode(TEST_FINGERPRINT_TOKEN)
                    delegate.identifyShopper(Activity(), encodedJson, true)

                    val expected = ActionComponentData(
                        paymentData = null,
                        details = submitFingerprintResult.details,
                    )
                    assertEquals(expected, awaitItem())
                }
            }

        @Test
        fun `fingerprint is submitted automatically and result is redirect, then redirect should be handled`(
            dispatcher: TestDispatcher
        ) =
            runTest {
                val authReqParams = TestAuthenticationRequestParameters(
                    deviceData = "deviceData",
                    sdkTransactionID = "sdkTransactionID",
                    sdkAppID = "sdkAppID",
                    sdkReferenceNumber = "sdkReferenceNumber",
                    sdkEphemeralPublicKey = "{}",
                    messageVersion = "messageVersion",
                )
                whenever(threeDS2Service.createTransaction(anyOrNull(), any())) doReturn TestTransaction(authReqParams)
                val submitFingerprintResult = SubmitFingerprintResult.Redirect(RedirectAction())
                whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                    Result.success(submitFingerprintResult)

                delegate.initialize(CoroutineScope(dispatcher))

                val encodedJson = base64Encoder.encode(TEST_FINGERPRINT_TOKEN)
                delegate.identifyShopper(Activity(), encodedJson, true)

                redirectHandler.assertLaunchRedirectCalled()
            }

        @Test
        fun `fingerprint is submitted automatically and it fails, then an exception is emitted`(
            dispatcher: TestDispatcher
        ) =
            runTest {
                val authReqParams = TestAuthenticationRequestParameters(
                    deviceData = "deviceData",
                    sdkTransactionID = "sdkTransactionID",
                    sdkAppID = "sdkAppID",
                    sdkReferenceNumber = "sdkReferenceNumber",
                    sdkEphemeralPublicKey = "{}",
                    messageVersion = "messageVersion",
                )
                whenever(threeDS2Service.createTransaction(anyOrNull(), any())) doReturn TestTransaction(authReqParams)
                val error = IOException("test")
                whenever(submitFingerprintRepository.submitFingerprint(any(), any(), anyOrNull())) doReturn
                    Result.failure(error)

                delegate.initialize(CoroutineScope(dispatcher))

                delegate.exceptionFlow.test {
                    val encodedJson = base64Encoder.encode(TEST_FINGERPRINT_TOKEN)
                    delegate.identifyShopper(Activity(), encodedJson, true)
                    assertEquals(error, awaitItem().cause)
                }
            }

        @Test
        fun `fingerprint is not submitted automatically, then details are emitted`(dispatcher: TestDispatcher) =
            runTest {
                val authReqParams = TestAuthenticationRequestParameters(
                    deviceData = "deviceData",
                    sdkTransactionID = "sdkTransactionID",
                    sdkAppID = "sdkAppID",
                    sdkReferenceNumber = "sdkReferenceNumber",
                    sdkEphemeralPublicKey = "{}",
                    messageVersion = "messageVersion",
                )
                whenever(threeDS2Service.createTransaction(anyOrNull(), any())) doReturn TestTransaction(authReqParams)
                val fingerprintDetails = JSONObject("{\"finger\":\"print\"}")
                whenever(adyen3DS2Serializer.createFingerprintDetails(any())) doReturn fingerprintDetails

                delegate.initialize(CoroutineScope(dispatcher))

                delegate.detailsFlow.test {
                    val encodedJson = base64Encoder.encode(TEST_FINGERPRINT_TOKEN)
                    delegate.identifyShopper(Activity(), encodedJson, false)

                    val expected = ActionComponentData(
                        paymentData = null,
                        details = fingerprintDetails,
                    )
                    assertEquals(expected, awaitItem())
                }
            }
    }

    @Nested
    @DisplayName("when challenging shopper and")
    inner class ChallengeShopperTest {

        @Test
        fun `transaction is null, then an exception is emitted`() = runTest {
            delegate.exceptionFlow.test {
                delegate.challengeShopper(Activity(), "token")

                assertTrue(awaitItem() is Authentication3DS2Exception)
            }
        }

        @Test
        fun `token can't be decoded, then an exception is emitted`(dispatcher: TestDispatcher) = runTest {
            initializeTransaction(dispatcher)

            delegate.exceptionFlow.test {
                delegate.challengeShopper(Activity(), base64Encoder.encode("token"))

                assertTrue(awaitItem().cause is JSONException)
            }
        }

        @Test
        fun `everything is good, then challenge should be executed`(dispatcher: TestDispatcher) = runTest {
            val transaction = initializeTransaction(dispatcher)

            delegate.challengeShopper(Activity(), base64Encoder.encode("{}"))

            transaction.assertDoChallengeCalled()
        }

        @Test
        fun `challenge fails, then an exception is emitted`(dispatcher: TestDispatcher) = runTest {
            initializeTransaction(dispatcher).apply {
                shouldThrowError = true
            }

            delegate.exceptionFlow.test {
                delegate.challengeShopper(Activity(), base64Encoder.encode("{}"))

                assertTrue(awaitItem().cause is InvalidInputException)
            }
        }

        private fun initializeTransaction(dispatcher: TestDispatcher): TestTransaction {
            val authReqParams = TestAuthenticationRequestParameters(
                deviceData = "deviceData",
                sdkTransactionID = "sdkTransactionID",
                sdkAppID = "sdkAppID",
                sdkReferenceNumber = "sdkReferenceNumber",
                sdkEphemeralPublicKey = "{}",
                messageVersion = "messageVersion",
            )
            val transaction = TestTransaction(authReqParams)
            whenever(threeDS2Service.createTransaction(anyOrNull(), any())) doReturn transaction

            delegate.initialize(CoroutineScope(dispatcher))

            val encodedJson = base64Encoder.encode(TEST_FINGERPRINT_TOKEN)
            delegate.identifyShopper(Activity(), encodedJson, false)

            return transaction
        }
    }

    @Nested
    @DisplayName("when handling intent and")
    inner class HandleIntentTest {

        @Test
        fun `result is parsed, then details are emitted`() = runTest {
            delegate.detailsFlow.test {
                delegate.handleIntent(Intent())

                val expected = ActionComponentData(
                    paymentData = null,
                    details = TestRedirectHandler.REDIRECT_RESULT,
                )
                assertEquals(expected, awaitItem())
            }
        }

        @Test
        fun `parsing fails, then an exception is emitted`() = runTest {
            val error = ComponentException("yes")
            redirectHandler.exception = error

            delegate.exceptionFlow.test {
                delegate.handleIntent(Intent())

                assertEquals(error, awaitItem())
            }
        }
    }

    @Nested
    @DisplayName("when transaction is")
    inner class TransactionTest {

        @Test
        fun `completed, then details are emitted`() = runTest {
            val details = JSONObject("{}")
            whenever(
                adyen3DS2Serializer.createChallengeDetails(
                    transactionStatus = "transactionStatus",
                ),
            ) doReturn details

            delegate.detailsFlow.test {
                delegate.onCompletion(
                    result = ChallengeResult.Completed(
                        transactionStatus = "transactionStatus",
                    ),
                )

                val expected = ActionComponentData(
                    paymentData = null,
                    details = details,
                )
                assertEquals(expected, awaitItem())
            }
        }

        @Test
        fun `completed and creating details fails, then an error is emitted`() = runTest {
            val error = ComponentException("test")
            whenever(
                adyen3DS2Serializer.createChallengeDetails(
                    transactionStatus = "transactionStatus",
                ),
            ) doAnswer { throw error }

            delegate.exceptionFlow.test {
                delegate.onCompletion(
                    result = ChallengeResult.Completed(
                        transactionStatus = "transactionStatus",
                    ),
                )

                assertEquals(error, awaitItem())
            }
        }

        @Test
        fun `cancelled, then an error is emitted`() = runTest {
            delegate.exceptionFlow.test {
                delegate.onCompletion(
                    result = ChallengeResult.Cancelled(
                        transactionStatus = "transactionStatus",
                        additionalDetails = "additionalDetails",
                    ),
                )

                assertTrue(awaitItem() is Cancelled3DS2Exception)
            }
        }

        @Test
        fun `timedout, then an error is emitted`() = runTest {
            val details = JSONObject("{}")
            whenever(
                adyen3DS2Serializer.createChallengeDetails(
                    transactionStatus = "transactionStatus",
                    errorDetails = "additionalDetails",
                ),
            ) doReturn details

            delegate.detailsFlow.test {
                delegate.onCompletion(
                    result = ChallengeResult.Timeout(
                        transactionStatus = "transactionStatus",
                        additionalDetails = "additionalDetails",
                    ),
                )

                val expected = ActionComponentData(
                    paymentData = null,
                    details = details,
                )
                assertEquals(expected, awaitItem())
            }
        }

        @Test
        fun `error, then an error is emitted`() = runTest {
            val details = JSONObject("{}")
            whenever(
                adyen3DS2Serializer.createChallengeDetails(
                    transactionStatus = "transactionStatus",
                    errorDetails = "additionalDetails",
                ),
            ) doReturn details

            delegate.detailsFlow.test {
                delegate.onCompletion(
                    result = ChallengeResult.Error(
                        transactionStatus = "transactionStatus",
                        additionalDetails = "additionalDetails",
                    ),
                )

                val expected = ActionComponentData(
                    paymentData = null,
                    details = details,
                )
                assertEquals(expected, awaitItem())
            }
        }
    }

    private class TestTransaction(
        val authReqParameters: AuthenticationRequestParameters? = null
    ) : Transaction {

        var shouldThrowError: Boolean = false

        private var timesDoChallengeCalled = 0

        override fun getAuthenticationRequestParameters(): AuthenticationRequestParameters? = authReqParameters

        @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
        override fun doChallenge(p0: Activity?, p1: ChallengeParameters?, p2: ChallengeStatusReceiver?, p3: Int) {
            timesDoChallengeCalled++
            if (shouldThrowError) {
                throw InvalidInputException("test", null)
            }
        }

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
        private val TEST_FINGERPRINT_TOKEN =
            """
            {
                "directoryServerId":"id",
                "directoryServerPublicKey":"key",
                "threeDSMessageVersion":"2.1.0"
            }
            """.trimIndent()
    }
}

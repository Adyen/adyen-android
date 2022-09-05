/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2022.
 */

package com.adyen.checkout.adyen3ds2

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.adyen3ds2.exception.Authentication3DS2Exception
import com.adyen.checkout.adyen3ds2.exception.Cancelled3DS2Exception
import com.adyen.checkout.adyen3ds2.repository.SubmitFingerprintRepository
import com.adyen.checkout.adyen3ds2.repository.SubmitFingerprintResult
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.encoding.JavaBase64Encoder
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.model.payments.response.Threeds2FingerprintAction
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.redirect.test.TestRedirectHandler
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.threeds2.AuthenticationRequestParameters
import com.adyen.threeds2.ChallengeStatusReceiver
import com.adyen.threeds2.CompletionEvent
import com.adyen.threeds2.ErrorMessage
import com.adyen.threeds2.ProgressDialog
import com.adyen.threeds2.ProtocolErrorEvent
import com.adyen.threeds2.RuntimeErrorEvent
import com.adyen.threeds2.ThreeDS2Service
import com.adyen.threeds2.Transaction
import com.adyen.threeds2.exception.InvalidInputException
import com.adyen.threeds2.exception.SDKRuntimeException
import com.adyen.threeds2.parameters.ChallengeParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
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
        delegate = DefaultAdyen3DS2Delegate(
            savedStateHandle = SavedStateHandle(),
            configuration = Adyen3DS2Configuration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build(),
            submitFingerprintRepository = submitFingerprintRepository,
            paymentDataRepository = paymentDataRepository,
            adyen3DS2Serializer = adyen3DS2Serializer,
            redirectHandler = redirectHandler,
            threeDS2Service = threeDS2Service,
            defaultDispatcher = dispatcher,
            embeddedRequestorAppUrl = "embeddedRequestorAppUrl",
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
                        "{\"directoryServerId\": \"id\", \"directoryServerPublicKey\": \"key\"}"
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
                val encodedJson = base64Encoder.encode(
                    "{\"directoryServerId\": \"id\", \"directoryServerPublicKey\": \"key\", \"threeDSMessageVersion\":\"2.1.0\"}"
                )
                delegate.identifyShopper(Activity(), encodedJson, false)

                assertEquals(error, awaitItem().cause)
            }
        }

        @Test
        fun `transaction parameters are null, then an exception emitted`(dispatcher: TestDispatcher) = runTest {
            whenever(threeDS2Service.createTransaction(anyOrNull(), any())) doReturn TestTransaction()
            delegate.initialize(CoroutineScope(dispatcher))

            delegate.exceptionFlow.test {
                val encodedJson = base64Encoder.encode(
                    "{\"directoryServerId\": \"id\", \"directoryServerPublicKey\": \"key\", \"threeDSMessageVersion\":\"2.1.0\"}"
                )
                delegate.identifyShopper(Activity(), encodedJson, false)

                assertTrue(awaitItem() is ComponentException)
            }
        }

        @Test
        fun `fingerprint is submitted automatically and result is completed, then details are emitted`(dispatcher: TestDispatcher) =
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
                    val encodedJson = base64Encoder.encode(
                        "{\"directoryServerId\": \"id\", \"directoryServerPublicKey\": \"key\", \"threeDSMessageVersion\":\"2.1.0\"}"
                    )
                    delegate.identifyShopper(Activity(), encodedJson, true)

                    val expected = ActionComponentData(
                        paymentData = null,
                        details = submitFingerprintResult.details,
                    )
                    assertEquals(expected, awaitItem())
                }
            }

        @Test
        fun `fingerprint is submitted automatically and result is redirect, then redirect should be handled`(dispatcher: TestDispatcher) =
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

                val encodedJson = base64Encoder.encode(
                    "{\"directoryServerId\": \"id\", \"directoryServerPublicKey\": \"key\", \"threeDSMessageVersion\":\"2.1.0\"}"
                )
                delegate.identifyShopper(Activity(), encodedJson, true)

                redirectHandler.assertLaunchRedirectCalled()
            }

        @Test
        fun `fingerprint is submitted automatically and it fails, then an exception is emitted`(dispatcher: TestDispatcher) =
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
                    val encodedJson = base64Encoder.encode(
                        "{\"directoryServerId\": \"id\", \"directoryServerPublicKey\": \"key\", \"threeDSMessageVersion\":\"2.1.0\"}"
                    )
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
                    val encodedJson = base64Encoder.encode(
                        "{\"directoryServerId\": \"id\", \"directoryServerPublicKey\": \"key\", \"threeDSMessageVersion\":\"2.1.0\"}"
                    )
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

            val encodedJson = base64Encoder.encode(
                "{\"directoryServerId\": \"id\", \"directoryServerPublicKey\": \"key\", \"threeDSMessageVersion\":\"2.1.0\"}"
            )
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
            whenever(adyen3DS2Serializer.createChallengeDetails(any())) doReturn details

            delegate.detailsFlow.test {
                delegate.completed(TestCompletionEvent())

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
            whenever(adyen3DS2Serializer.createChallengeDetails(any())) doAnswer { throw error }

            delegate.exceptionFlow.test {
                delegate.completed(TestCompletionEvent())

                assertEquals(error, awaitItem())
            }
        }

        @Test
        fun `cancelled, then an error is emitted`() = runTest {
            delegate.exceptionFlow.test {
                delegate.cancelled()

                assertTrue(awaitItem() is Cancelled3DS2Exception)
            }
        }

        @Test
        fun `timedout, then an error is emitted`() = runTest {
            delegate.exceptionFlow.test {
                delegate.timedout()

                assertTrue(awaitItem() is Authentication3DS2Exception)
            }
        }

        @Test
        fun `protocolError, then an error is emitted`() = runTest {
            delegate.exceptionFlow.test {
                delegate.protocolError(TestProtocolErrorEvent())

                assertTrue(awaitItem() is Authentication3DS2Exception)
            }
        }

        @Test
        fun `runtimeError, then an error is emitted`() = runTest {
            delegate.exceptionFlow.test {
                delegate.runtimeError(TestRuntimeErrorEvent())

                assertTrue(awaitItem() is Authentication3DS2Exception)
            }
        }
    }

    private class TestTransaction(
        val authReqParameters: AuthenticationRequestParameters? = null
    ) : Transaction {

        var shouldThrowError: Boolean = false

        private var timesDoChallengeCalled = 0

        override fun getAuthenticationRequestParameters(): AuthenticationRequestParameters? = authReqParameters

        override fun doChallenge(p0: Activity?, p1: ChallengeParameters?, p2: ChallengeStatusReceiver?, p3: Int) {
            timesDoChallengeCalled++
            if (shouldThrowError) {
                throw InvalidInputException("test", null)
            }
        }

        override fun getProgressView(p0: Activity?): ProgressDialog {
            throw IllegalStateException("This method should not be used")
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

    private class TestCompletionEvent : CompletionEvent {
        override fun getSDKTransactionID(): String = "transactionId"

        override fun getTransactionStatus(): String = "status"
    }

    private class TestProtocolErrorEvent : ProtocolErrorEvent {
        override fun getErrorMessage(): ErrorMessage = object : ErrorMessage {
            override fun getErrorCode(): String = ""

            override fun getErrorDescription(): String = ""

            override fun getErrorDetails(): String = ""

            override fun getTransactionID(): String = ""
        }

        override fun getSDKTransactionID(): String = "transactionId"
    }

    private class TestRuntimeErrorEvent : RuntimeErrorEvent {
        override fun getErrorCode(): String = ""

        override fun getErrorMessage(): String = ""
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}

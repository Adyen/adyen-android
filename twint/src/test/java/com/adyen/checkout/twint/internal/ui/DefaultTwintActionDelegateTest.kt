/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/11/2023.
 */

package com.adyen.checkout.twint.internal.ui

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import ch.twint.payment.sdk.TwintPayResult
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.AwaitAction
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.action.TwintSdkData
import com.adyen.checkout.components.core.action.WeChatPaySdkData
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import com.adyen.checkout.components.core.internal.test.TestStatusRepository
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.components.core.internal.util.StatusResponseUtils
import com.adyen.checkout.core.Environment
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.junit.jupiter.MockitoExtension
import java.io.IOException
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class DefaultTwintActionDelegateTest {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var statusRepository: TestStatusRepository
    private lateinit var delegate: DefaultTwintActionDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        statusRepository = TestStatusRepository()
        delegate = createDelegate()
    }

    @Test
    fun `when handling action successfully, then a pay event should be emitted`() = runTest {
        val payEventFlow = delegate.payEventFlow.test(testScheduler)
        val action = SdkAction(paymentData = TEST_PAYMENT_DATA, sdkData = TwintSdkData("token"))

        delegate.handleAction(action, Activity())

        assertEquals(action.sdkData?.token, payEventFlow.latestValue)
    }

    @ParameterizedTest
    @MethodSource("handleActionSource")
    fun `when handling action, then expect`(action: Action, expectedErrorMessage: String) = runTest {
        val testFlow = delegate.exceptionFlow.test(testScheduler)

        delegate.handleAction(action, Activity())

        assertEquals(expectedErrorMessage, testFlow.latestValue.message)
    }

    @ParameterizedTest
    @MethodSource("handleTwintResult")
    fun `when handling twint result, then expect`(result: TwintPayResult, testResult: TwintTestResult) = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = StatusResponseUtils.RESULT_AUTHORIZED, payload = TEST_PAYLOAD)),
        )
        val detailsFlow = delegate.detailsFlow.test(testScheduler)
        val exceptionFlow = delegate.exceptionFlow.test(testScheduler)
        delegate.handleAction(SdkAction(paymentData = TEST_PAYMENT_DATA, sdkData = TwintSdkData("token")), Activity())

        delegate.handleTwintResult(result)

        when (testResult) {
            is TwintTestResult.Error -> {
                assertEquals(testResult.expectedMessage, exceptionFlow.latestValue.message)
            }

            is TwintTestResult.Success -> {
                with(detailsFlow.latestValue) {
                    assertEquals(testResult.expectedActionComponentData.paymentData, paymentData)
                    assertEquals(testResult.expectedActionComponentData.details.toString(), details.toString())
                }
            }
        }
    }

    @Nested
    @DisplayName("when polling and")
    inner class PollingTest {

        @Test
        fun `paymentData is missing, then an error is propagated`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleTwintResult(TwintPayResult.TW_B_SUCCESS)

            val expectedErrorMessage = "PaymentData should not be null."
            assertEquals(expectedErrorMessage, exceptionFlow.latestValue.message)
        }

        @Test
        fun `polling fails, then an error is propagated`() = runTest {
            statusRepository.pollingResults = listOf(Result.failure(IOException("Test")))
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)
            delegate.handleAction(
                action = SdkAction(paymentData = TEST_PAYMENT_DATA, sdkData = TwintSdkData("token")),
                activity = Activity(),
            )

            delegate.handleTwintResult(TwintPayResult.TW_B_SUCCESS)

            val expectedErrorMessage = "Error while polling status."
            assertEquals(expectedErrorMessage, exceptionFlow.latestValue.message)
        }

        @Test
        fun `polling succeeds and payload is missing, then an error is propagated`() = runTest {
            statusRepository.pollingResults = listOf(
                Result.success(StatusResponse(resultCode = StatusResponseUtils.RESULT_AUTHORIZED, payload = null)),
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)
            delegate.handleAction(
                action = SdkAction(paymentData = TEST_PAYMENT_DATA, sdkData = TwintSdkData("token")),
                activity = Activity(),
            )

            delegate.handleTwintResult(TwintPayResult.TW_B_SUCCESS)

            val expectedErrorMessage = "Payload is missing from StatusResponse."
            assertEquals(expectedErrorMessage, exceptionFlow.latestValue.message)
        }

        @Test
        fun `polling succeeds and payload is available, then details are emitted`() = runTest {
            statusRepository.pollingResults = listOf(
                Result.success(
                    StatusResponse(
                        resultCode = StatusResponseUtils.RESULT_AUTHORIZED,
                        payload = TEST_PAYLOAD,
                    ),
                ),
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val detailsFlow = delegate.detailsFlow.test(testScheduler)
            delegate.handleAction(
                action = SdkAction(paymentData = TEST_PAYMENT_DATA, sdkData = TwintSdkData("token")),
                activity = Activity(),
            )

            delegate.handleTwintResult(TwintPayResult.TW_B_SUCCESS)

            val expected = ActionComponentData(
                paymentData = null,
                details = JSONObject().put(DefaultTwintActionDelegate.PAYLOAD_DETAILS_KEY, TEST_PAYLOAD),
            )
            with(detailsFlow.latestValue) {
                assertNull(paymentData)
                assertEquals(expected.details.toString(), details.toString())
            }
        }
    }

    @Test
    fun `when initializing and action is set, then state is restored`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "finished", payload = "testpayload")),
        )
        val savedStateHandle = SavedStateHandle().apply {
            set(
                DefaultTwintActionDelegate.ACTION_KEY,
                SdkAction(paymentData = TEST_PAYMENT_DATA, sdkData = TwintSdkData("token")),
            )
            set(DefaultTwintActionDelegate.IS_POLLING_KEY, true)
        }
        delegate = createDelegate(savedStateHandle)
        val detailsFlow = delegate.detailsFlow.test(testScheduler)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertTrue(detailsFlow.values.isNotEmpty())
    }

    @Test
    fun `when details are emitted, then state is cleared`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "finished", payload = "testpayload")),
        )
        val savedStateHandle = SavedStateHandle()
        delegate = createDelegate(savedStateHandle)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.handleTwintResult(TwintPayResult.TW_B_SUCCESS)

        assertNull(savedStateHandle[DefaultTwintActionDelegate.ACTION_KEY])
    }

    @Test
    fun `when an error is emitted, then state is cleared`() = runTest {
        val savedStateHandle = SavedStateHandle().apply {
            set(DefaultTwintActionDelegate.ACTION_KEY, SdkAction(paymentData = "test", sdkData = TwintSdkData("token")))
        }
        delegate = createDelegate(savedStateHandle)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.handleAction(
            action = RedirectAction(paymentMethodType = TEST_PAYMENT_METHOD_TYPE, paymentData = TEST_PAYMENT_DATA),
            activity = Activity(),
        )

        assertNull(savedStateHandle[DefaultTwintActionDelegate.ACTION_KEY])
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when handleAction is called, then action event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val action = SdkAction(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                type = TEST_ACTION_TYPE,
                paymentData = TEST_PAYMENT_DATA,
                sdkData = TwintSdkData("token"),
            )

            delegate.handleAction(action, Activity())

            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }
    }

    private fun createDelegate(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): DefaultTwintActionDelegate {
        val configuration = CheckoutConfiguration(Environment.TEST, TEST_CLIENT_KEY)

        return DefaultTwintActionDelegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = savedStateHandle,
            componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null),
            paymentDataRepository = PaymentDataRepository(SavedStateHandle()),
            statusRepository = statusRepository,
            analyticsManager = analyticsManager,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_PAYLOAD = "TEST_PAYLOAD"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private const val TEST_ACTION_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private const val TEST_PAYMENT_DATA = "TEST_PAYMENT_DATA"

        @JvmStatic
        fun handleActionSource() = listOf(
            arguments(AwaitAction(), "Unsupported action"),
            arguments(
                SdkAction(paymentData = "something", sdkData = WeChatPaySdkData()),
                "SDK Data is null or of wrong type",
            ),
            arguments(SdkAction<WeChatPaySdkData>(paymentData = "something"), "SDK Data is null or of wrong type"),
            arguments(SdkAction(paymentData = null, sdkData = TwintSdkData("")), "Payment data is null"),
            arguments(
                SdkAction<TwintSdkData>(paymentData = "something", sdkData = null),
                "SDK Data is null or of wrong type",
            ),
        )

        @JvmStatic
        fun handleTwintResult() = listOf(
            arguments(
                TwintPayResult.TW_B_SUCCESS,
                TwintTestResult.Success(
                    ActionComponentData(null, JSONObject().put(DefaultTwintActionDelegate.PAYLOAD_DETAILS_KEY, TEST_PAYLOAD)),
                ),
            ),
            arguments(
                TwintPayResult.TW_B_ERROR,
                TwintTestResult.Error("Twint encountered an error."),
            ),
            arguments(
                TwintPayResult.TW_B_APP_NOT_INSTALLED,
                TwintTestResult.Error("Twint app not installed."),
            ),
        )
    }

    sealed class TwintTestResult {
        data class Success(val expectedActionComponentData: ActionComponentData) : TwintTestResult()

        data class Error(val expectedMessage: String) : TwintTestResult()
    }
}

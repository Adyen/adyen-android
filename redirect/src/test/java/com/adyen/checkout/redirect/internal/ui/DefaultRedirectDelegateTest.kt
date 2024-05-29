/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/8/2022.
 */

package com.adyen.checkout.redirect.internal.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.ActionTypes
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.exception.HttpException
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.redirect.internal.data.api.NativeRedirectService
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectResponse
import com.adyen.checkout.redirect.redirect
import com.adyen.checkout.ui.core.internal.test.TestRedirectHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultRedirectDelegateTest(
    @Mock private val nativeRedirectService: NativeRedirectService,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var redirectHandler: TestRedirectHandler
    private lateinit var paymentDataRepository: PaymentDataRepository
    private lateinit var delegate: DefaultRedirectDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        redirectHandler = TestRedirectHandler()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        redirectHandler = TestRedirectHandler()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        delegate = createDelegate()
    }

    @Test
    fun `when handleAction called and RedirectHandler returns an error, then the error is propagated`() = runTest {
        val error = ComponentException("Failed to make redirect.")
        redirectHandler.exception = error

        delegate.exceptionFlow.test {
            delegate.handleAction(RedirectAction(paymentData = "paymentData"), Activity())

            assertEquals(error, awaitItem())
        }
    }

    @Test
    fun `when handleAction called with valid data, then no error is propagated`() = runTest {
        delegate.exceptionFlow.test {
            delegate.handleAction(RedirectAction(paymentData = "paymentData"), Activity())

            expectNoEvents()
        }
    }

    @Test
    fun `when handleAction called with native redirect type, then the native redirect data should be stored`() {
        val testData = "sometestdata"
        delegate.handleAction(
            action = RedirectAction(type = ActionTypes.NATIVE_REDIRECT, nativeRedirectData = testData),
            activity = Activity(),
        )

        assertEquals(testData, paymentDataRepository.nativeRedirectData)
    }

    @Test
    fun `when handleIntent called and RedirectHandler returns an error, then the error is propagated`() = runTest {
        val error = ComponentException("Failed to parse redirect result.")
        redirectHandler.exception = error

        delegate.exceptionFlow.test {
            delegate.handleIntent(Intent())

            assertEquals(error, awaitItem())
        }
    }

    @Test
    fun `when handleIntent called with valid data, then the details are emitted`() = runTest {
        delegate.detailsFlow.test {
            delegate.handleAction(RedirectAction(paymentData = "paymentData"), Activity())
            delegate.handleIntent(Intent())

            with(awaitItem()) {
                assertEquals(TestRedirectHandler.REDIRECT_RESULT, details)
                assertEquals("paymentData", paymentData)
            }
        }
    }

    @Test
    fun `when handleIntent called with valid data and it's a native redirect, then the details are emitted`() =
        runTest {
            delegate.detailsFlow.test {
                val response = NativeRedirectResponse("someRedirectResult")
                whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doReturn response
                delegate.initialize(this@runTest)
                delegate.handleAction(
                    action = RedirectAction(type = ActionTypes.NATIVE_REDIRECT, nativeRedirectData = "testData"),
                    activity = Activity(),
                )

                delegate.handleIntent(Intent())

                with(awaitItem()) {
                    assertEquals(NativeRedirectResponse.SERIALIZER.serialize(response).toString(), details.toString())
                    assertNull(paymentData)
                }
            }
        }

    @ParameterizedTest
    @MethodSource("errorSource")
    fun `when native redirect is handled and throws an error, then the error is propagated`(error: Exception) =
        runTest {
            delegate.exceptionFlow.test {
                whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doAnswer { throw error }
                delegate.initialize(this@runTest)
                delegate.handleAction(
                    action = RedirectAction(type = ActionTypes.NATIVE_REDIRECT, nativeRedirectData = "testData"),
                    activity = Activity(),
                )

                delegate.handleIntent(Intent())

                assertEquals(error, awaitItem())
            }
        }

    @Test
    fun `when onCleared is called, then the delegate gets clean up correctly`() {
        val mockObserverRepository = mock<ActionObserverRepository>()
        delegate = createDelegate(observerRepository = mockObserverRepository)

        delegate.onCleared()

        verify(mockObserverRepository).removeObservers()
        redirectHandler.assertRemoveOnRedirectListenerCalled()
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when handleAction is called, then action event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val action = RedirectAction(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                type = TEST_ACTION_TYPE,
            )

            delegate.handleAction(action, Activity())

            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }
    }

    @Test
    fun `when initializing and action is set, then state is restored`() = runTest {
        val savedStateHandle = SavedStateHandle().apply {
            set(
                DefaultRedirectDelegate.ACTION_KEY,
                RedirectAction(paymentMethodType = "test", paymentData = "paymentData"),
            )
        }
        delegate = createDelegate(savedStateHandle = savedStateHandle)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertEquals("paymentData", paymentDataRepository.paymentData)
    }

    @Test
    fun `when details are emitted, then state is cleared`() = runTest {
        val savedStateHandle = SavedStateHandle()
        delegate = createDelegate(savedStateHandle = savedStateHandle)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.handleAction(RedirectAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

        delegate.handleIntent(Intent())

        assertNull(savedStateHandle[DefaultRedirectDelegate.ACTION_KEY])
    }

    @Test
    fun `when an error is emitted, then state is cleared`() = runTest {
        val savedStateHandle = SavedStateHandle()
        delegate = createDelegate(savedStateHandle = savedStateHandle)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        redirectHandler.exception = ComponentException("Test")

        delegate.handleAction(RedirectAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

        assertNull(savedStateHandle[DefaultRedirectDelegate.ACTION_KEY])
    }

    private fun createDelegate(
        observerRepository: ActionObserverRepository = ActionObserverRepository(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ): DefaultRedirectDelegate {
        val configuration = CheckoutConfiguration(
            Environment.TEST,
            TEST_CLIENT_KEY,
        ) {
            redirect()
        }
        return DefaultRedirectDelegate(
            observerRepository = observerRepository,
            savedStateHandle = savedStateHandle,
            componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                configuration,
                Locale.US,
                null,
                null,
            ),
            redirectHandler = redirectHandler,
            paymentDataRepository = paymentDataRepository,
            nativeRedirectService = nativeRedirectService,
            analyticsManager = analyticsManager,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private const val TEST_ACTION_TYPE = "TEST_PAYMENT_METHOD_TYPE"

        @JvmStatic
        fun errorSource() = listOf(
            arguments(HttpException(401, "", null)),
            arguments(ModelSerializationException(NativeRedirectResponse::class.java, null)),
        )
    }
}

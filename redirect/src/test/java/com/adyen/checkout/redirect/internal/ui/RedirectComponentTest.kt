/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/6/2026.
 */

package com.adyen.checkout.redirect.internal.ui

import android.content.Intent
import com.adyen.checkout.core.action.data.ActionTypes
import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.HttpError
import com.adyen.checkout.core.redirect.internal.RedirectHandler
import com.adyen.checkout.core.redirect.internal.ui.RedirectViewEvent
import com.adyen.checkout.redirect.internal.data.api.NativeRedirectService
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectRequest
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectResponse
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class RedirectComponentTest(
    @param:Mock private val redirectHandler: RedirectHandler,
    @param:Mock private val nativeRedirectService: NativeRedirectService,
) {

    private lateinit var analyticsManager: TestAnalyticsManager

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
    }

    @Nested
    inner class HandleActionTest {

        @Test
        fun `when handleAction is called, then analytics action event is tracked`() {
            // GIVEN
            val component = createComponent(
                action = redirectAction(
                    paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                    type = TEST_ACTION_TYPE,
                    paymentData = "paymentData",
                ),
            )

            // WHEN
            component.handleAction()

            // THEN
            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when handleAction is called, then the redirect is launched with the action url`() = runTest {
            // GIVEN
            val component = createComponent(action = redirectAction(url = TEST_URL))
            val redirectEvents = component.redirectEventFlow.test(testScheduler)

            // WHEN
            component.handleAction()

            // THEN
            assertEquals(listOf(RedirectViewEvent.Redirect(TEST_URL)), redirectEvents.values)
        }
    }

    @Nested
    inner class RestoreActionTest {

        @Test
        fun `when restoreAction is called, then no redirect is launched`() = runTest {
            // GIVEN
            val component = createComponent(action = redirectAction(url = TEST_URL))
            val redirectEvents = component.redirectEventFlow.test(testScheduler)

            // WHEN
            component.restoreAction()

            // THEN
            assertEquals(emptyList<RedirectViewEvent>(), redirectEvents.values)
        }

        @Test
        fun `when restoreAction is called, then no action event is tracked`() {
            // GIVEN
            val component = createComponent(
                action = redirectAction(
                    paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                    type = TEST_ACTION_TYPE,
                ),
            )

            // WHEN
            component.restoreAction()

            // THEN
            val actionEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertEventCount(0, actionEvent)
        }

        @Test
        fun `when restored and handleReturn is called, then details contain the action's paymentData`() = runTest {
            // GIVEN
            val expectedDetails = JSONObject().apply { put("redirectResult", "testResult") }
            whenever(redirectHandler.parseRedirectResult(anyOrNull())) doReturn expectedDetails
            val component = createComponent(
                action = redirectAction(
                    type = ActionTypes.REDIRECT,
                    paymentData = "testPaymentData",
                ),
            )
            component.restoreAction()
            val events = component.eventFlow.test(testScheduler)

            // WHEN
            component.handleReturn(Intent())

            // THEN
            val event = assertInstanceOf<ActionComponentEvent.ActionDetails>(events.latestValue)
            assertEquals(expectedDetails.toString(), event.data.details.toString())
            assertEquals("testPaymentData", event.data.paymentData)
        }

        @Test
        fun `when restored with native redirect and handleReturn is called, then the action's nativeRedirectData is sent`() =
            runTest {
                // GIVEN
                whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doReturn
                    NativeRedirectResponse("someRedirectResult")
                val redirectResult = JSONObject().apply {
                    put("returnUrlQueryString", "gpid=ajfbasljbfaljfe")
                }
                whenever(redirectHandler.parseRedirectResult(anyOrNull())) doReturn redirectResult
                val component = createComponent(
                    action = redirectAction(
                        type = ActionTypes.NATIVE_REDIRECT,
                        nativeRedirectData = "testNativeData",
                    ),
                )
                component.restoreAction()

                // WHEN
                component.handleReturn(Intent())

                // THEN
                val expectedRequest = NativeRedirectRequest(
                    redirectData = "testNativeData",
                    returnQueryString = "gpid=ajfbasljbfaljfe",
                )
                verify(nativeRedirectService).makeNativeRedirect(expectedRequest, TEST_CLIENT_KEY)
            }
    }

    @Nested
    inner class HandleReturnTest {

        @Test
        fun `when handleReturn is called with valid data, then details are emitted`() = runTest {
            // GIVEN
            val expectedDetails = JSONObject().apply { put("redirectResult", "testResult") }
            whenever(redirectHandler.parseRedirectResult(anyOrNull())) doReturn expectedDetails
            val component = createComponent(
                action = redirectAction(
                    type = ActionTypes.REDIRECT,
                    paymentData = "testPaymentData",
                ),
            )
            component.handleAction()

            val events = component.eventFlow.test(testScheduler)

            // WHEN
            component.handleReturn(Intent())

            // THEN
            val event = events.latestValue
            assertTrue(event is ActionComponentEvent.ActionDetails)
            val details = (event as ActionComponentEvent.ActionDetails).data
            assertEquals(expectedDetails.toString(), details.details.toString())
            assertEquals("testPaymentData", details.paymentData)
        }

        @Test
        fun `when handleReturn is called and parsing fails, then error is emitted`() = runTest {
            // GIVEN
            val error = GenericError("Failed to parse redirect result.")
            whenever(redirectHandler.parseRedirectResult(anyOrNull())) doAnswer { throw error }
            val component = createComponent(
                action = redirectAction(
                    type = ActionTypes.REDIRECT,
                    paymentData = "paymentData",
                ),
            )

            val events = component.eventFlow.test(testScheduler)

            // WHEN
            component.handleReturn(Intent())

            // THEN
            val event = events.latestValue
            assertTrue(event is ActionComponentEvent.Error)
            assertEquals(error, (event as ActionComponentEvent.Error).error)
        }

        @Test
        fun `when handleReturn is called and parsing fails, then redirect parse failed event is tracked`() {
            // GIVEN
            val error = GenericError("Failed to parse redirect result.")
            whenever(redirectHandler.parseRedirectResult(anyOrNull())) doAnswer { throw error }
            val component = createComponent(
                action = redirectAction(
                    paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                    type = ActionTypes.REDIRECT,
                ),
            )

            // WHEN
            component.handleReturn(Intent())

            // THEN
            val expectedEvent = GenericEvents.error(
                component = TEST_PAYMENT_METHOD_TYPE,
                event = ErrorEvent.REDIRECT_PARSE_FAILED,
            )
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @Test
        fun `when handleReturn is called and error is emitted, then redirect failed event is tracked`() {
            // GIVEN
            val error = GenericError("Failed to parse redirect result.")
            whenever(redirectHandler.parseRedirectResult(anyOrNull())) doAnswer { throw error }
            val component = createComponent(
                action = redirectAction(
                    paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                    type = ActionTypes.REDIRECT,
                ),
            )

            // WHEN
            component.handleReturn(Intent())

            // THEN
            val expectedEvent = GenericEvents.error(
                component = TEST_PAYMENT_METHOD_TYPE,
                event = ErrorEvent.REDIRECT_FAILED,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }
    }

    @Nested
    inner class NativeRedirectTest {

        @Test
        fun `when handleReturn is called with native redirect and service succeeds, then details are emitted`() =
            runTest {
                // GIVEN
                val response = NativeRedirectResponse("someRedirectResult")
                whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doReturn response
                val redirectResult = JSONObject().apply {
                    put("returnUrlQueryString", "gpid=ajfbasljbfaljfe")
                }
                whenever(redirectHandler.parseRedirectResult(anyOrNull())) doReturn redirectResult
                val component = createComponent(
                    action = redirectAction(
                        type = ActionTypes.NATIVE_REDIRECT,
                        nativeRedirectData = "testNativeData",
                    ),
                )
                component.handleAction()

                val events = component.eventFlow.test(testScheduler)

                // WHEN
                component.handleReturn(Intent())

                // THEN
                val event = events.latestValue
                assertTrue(event is ActionComponentEvent.ActionDetails)
                val details = (event as ActionComponentEvent.ActionDetails).data
                val expectedDetails = NativeRedirectResponse.SERIALIZER.serialize(response)
                assertEquals(expectedDetails.toString(), details.details.toString())
                assertNull(details.paymentData)
            }

        @Test
        fun `when handleReturn is called with native redirect, then the action's nativeRedirectData is sent`() =
            runTest {
                // GIVEN
                whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doReturn
                    NativeRedirectResponse("someRedirectResult")
                val redirectResult = JSONObject().apply {
                    put("returnUrlQueryString", "gpid=ajfbasljbfaljfe")
                }
                whenever(redirectHandler.parseRedirectResult(anyOrNull())) doReturn redirectResult
                val component = createComponent(
                    action = redirectAction(
                        type = ActionTypes.NATIVE_REDIRECT,
                        nativeRedirectData = "testNativeData",
                    ),
                )
                component.handleAction()

                // WHEN
                component.handleReturn(Intent())

                // THEN
                val expectedRequest = NativeRedirectRequest(
                    redirectData = "testNativeData",
                    returnQueryString = "gpid=ajfbasljbfaljfe",
                )
                verify(nativeRedirectService).makeNativeRedirect(expectedRequest, TEST_CLIENT_KEY)
            }

        @Test
        fun `when handleReturn is called with native redirect and HttpError is thrown, then error is emitted`() =
            runTest {
                // GIVEN
                val error = HttpError(401, "Unauthorized", null)
                whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doAnswer { throw error }
                whenever(redirectHandler.parseRedirectResult(anyOrNull())) doReturn JSONObject()
                val component = createComponent(
                    action = redirectAction(
                        paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                        type = ActionTypes.NATIVE_REDIRECT,
                        nativeRedirectData = "testData",
                    ),
                )
                component.handleAction()

                val events = component.eventFlow.test(testScheduler)

                // WHEN
                component.handleReturn(Intent())

                // THEN
                val event = events.latestValue
                assertTrue(event is ActionComponentEvent.Error)
                assertEquals(error, (event as ActionComponentEvent.Error).error)
            }

        @Test
        fun `when handleReturn is called with native redirect and HttpError is thrown, then api native redirect error event is tracked`() =
            runTest {
                // GIVEN
                val error = HttpError(401, "Unauthorized", null)
                whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doAnswer { throw error }
                whenever(redirectHandler.parseRedirectResult(anyOrNull())) doReturn JSONObject()
                val component = createComponent(
                    action = redirectAction(
                        paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                        type = ActionTypes.NATIVE_REDIRECT,
                        nativeRedirectData = "testData",
                    ),
                )
                component.handleAction()

                // WHEN
                component.handleReturn(Intent())

                // THEN
                val expectedEvent = GenericEvents.error(
                    component = TEST_PAYMENT_METHOD_TYPE,
                    event = ErrorEvent.API_NATIVE_REDIRECT,
                )
                analyticsManager.assertHasEventEquals(expectedEvent)
            }

        @Test
        fun `when handleReturn is called with native redirect and JSONException is thrown, then error is emitted`() =
            runTest {
                // GIVEN
                val error = JSONException("Serialization error")
                whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doAnswer { throw error }
                whenever(redirectHandler.parseRedirectResult(anyOrNull())) doReturn JSONObject()
                val component = createComponent(
                    action = redirectAction(
                        paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                        type = ActionTypes.NATIVE_REDIRECT,
                        nativeRedirectData = "testData",
                    ),
                )
                component.handleAction()

                val events = component.eventFlow.test(testScheduler)

                // WHEN
                component.handleReturn(Intent())

                // THEN
                val event = events.latestValue
                assertTrue(event is ActionComponentEvent.Error)
            }

        @Test
        fun `when handleReturn is called with native redirect and JSONException is thrown, then api native redirect error event is tracked`() =
            runTest {
                // GIVEN
                val error = JSONException("Serialization error")
                whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doAnswer { throw error }
                whenever(redirectHandler.parseRedirectResult(anyOrNull())) doReturn JSONObject()
                val component = createComponent(
                    action = redirectAction(
                        paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                        type = ActionTypes.NATIVE_REDIRECT,
                        nativeRedirectData = "testData",
                    ),
                )
                component.handleAction()

                // WHEN
                component.handleReturn(Intent())

                // THEN
                val expectedEvent = GenericEvents.error(
                    component = TEST_PAYMENT_METHOD_TYPE,
                    event = ErrorEvent.API_NATIVE_REDIRECT,
                )
                analyticsManager.assertHasEventEquals(expectedEvent)
            }
    }

    private fun createComponent(
        action: RedirectAction = redirectAction(),
    ): RedirectComponent {
        return RedirectComponent(
            action = action,
            analyticsManager = analyticsManager,
            redirectHandler = redirectHandler,
            nativeRedirectService = nativeRedirectService,
            clientKey = TEST_CLIENT_KEY,
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
        )
    }

    private fun redirectAction(
        type: String = TEST_ACTION_TYPE,
        paymentMethodType: String? = null,
        paymentData: String? = null,
        nativeRedirectData: String? = null,
        url: String? = null,
    ) = RedirectAction(
        type = type,
        paymentMethodType = paymentMethodType,
        paymentData = paymentData,
        nativeRedirectData = nativeRedirectData,
        method = null,
        url = url,
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfgh"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private const val TEST_ACTION_TYPE = "TEST_ACTION_TYPE"
        private const val TEST_URL = "https://test.adyen.com/redirect"
    }
}

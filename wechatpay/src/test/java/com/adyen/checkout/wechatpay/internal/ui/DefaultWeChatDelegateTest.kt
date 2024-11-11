/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/8/2022.
 */

package com.adyen.checkout.wechatpay.internal.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.action.WeChatPaySdkData
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.wechatpay.internal.util.WeChatRequestGenerator
import com.adyen.checkout.wechatpay.weChatPayAction
import com.tencent.mm.opensdk.modelpay.PayResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultWeChatDelegateTest(
    @Mock private val iwxApi: IWXAPI,
    @Mock private val weChatRequestGenerator: WeChatRequestGenerator<*>
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var paymentDataRepository: PaymentDataRepository
    private lateinit var delegate: DefaultWeChatDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        delegate = createDelegate()
    }

    @Test
    fun `when handling intent, then delegate it to the IWXAPI`() {
        delegate.handleIntent(Intent())

        verify(iwxApi).handleIntent(any(), any())
    }

    @Test
    fun `when receiving a response from IWXAPI, then details should be emitted`() = runTest {
        val payResponse = PayResp().apply {
            prepayId = "somePrepayId"
            returnKey = "someReturnKey"
            extData = "someExtData"
        }

        val action = SdkAction(
            sdkData = WeChatPaySdkData(),
            paymentData = TEST_PAYMENT_DATA,
        )

        delegate.detailsFlow.test {
            delegate.handleAction(action, Activity())
            delegate.onResponse(payResponse)

            val expected = JSONObject().apply {
                put("resultCode", 0)
            }

            with(awaitItem()) {
                assertEquals(expected.toString(), details.toString())
                assertEquals(TEST_PAYMENT_DATA, paymentData)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when handling action, then we chat pay is initiated`() {
        val action = SdkAction(
            sdkData = WeChatPaySdkData(
                appid = "appid",
                noncestr = "noncestr",
                packageValue = "packageValue",
                partnerid = "partnerid",
                prepayid = "prepayid",
                sign = "sign",
                timestamp = "timestamp",
            ),
            paymentData = "paymentData",
        )

        delegate.handleAction(action, Activity())

        verify(iwxApi).registerApp("appid")
        verify(iwxApi).sendReq(anyOrNull())
    }

    @Test
    fun `when handling action and sdkData is null, then an error is propagated`() = runTest {
        val action = SdkAction<WeChatPaySdkData>(sdkData = null)

        delegate.exceptionFlow.test {
            delegate.handleAction(action, Activity())

            assertTrue(awaitItem() is ComponentException)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when handling action and sending a request fails, then an error is propagated`() = runTest {
        whenever(iwxApi.sendReq(any())) doReturn false
        val action = SdkAction(sdkData = WeChatPaySdkData())

        delegate.exceptionFlow.test {
            delegate.handleAction(action, Activity())

            assertTrue(awaitItem() is ComponentException)

            cancelAndIgnoreRemainingEvents()
        }
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
                sdkData = WeChatPaySdkData(),
            )

            delegate.handleAction(action, Activity())

            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @Test
        fun `when handleAction is called and sending a request to WeChat fails, then an error is tracked`() {
            whenever(iwxApi.sendReq(any())) doReturn false
            val action = SdkAction(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                type = TEST_ACTION_TYPE,
                paymentData = TEST_PAYMENT_DATA,
                sdkData = WeChatPaySdkData(),
            )

            delegate.handleAction(action, Activity())

            val expectedEvent = GenericEvents.error(
                component = TEST_PAYMENT_METHOD_TYPE,
                event = ErrorEvent.THIRD_PARTY,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }
    }

    @Test
    fun `when initializing and action is set, then state is restored`() = runTest {
        val savedStateHandle = SavedStateHandle().apply {
            set(
                DefaultWeChatDelegate.ACTION_KEY,
                SdkAction(paymentMethodType = "test", paymentData = "paymentData", sdkData = WeChatPaySdkData()),
            )
        }
        delegate = createDelegate(savedStateHandle)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertEquals("paymentData", paymentDataRepository.paymentData)
    }

    @Test
    fun `when details are emitted, then state is cleared`() = runTest {
        whenever(iwxApi.sendReq(anyOrNull())) doReturn true
        val savedStateHandle = SavedStateHandle()
        delegate = createDelegate(savedStateHandle)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.handleAction(
            SdkAction(paymentMethodType = "test", paymentData = "paymentData", sdkData = WeChatPaySdkData()),
            Activity(),
        )

        delegate.onResponse(PayResp().apply { errCode = 1 })

        assertNull(savedStateHandle[DefaultWeChatDelegate.ACTION_KEY])
    }

    @Test
    fun `when an error is emitted, then state is cleared`() = runTest {
        val savedStateHandle = SavedStateHandle()
        delegate = createDelegate(savedStateHandle)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.handleAction(
            SdkAction(paymentMethodType = "test", paymentData = null, sdkData = WeChatPaySdkData()),
            Activity(),
        )

        assertNull(savedStateHandle[DefaultWeChatDelegate.ACTION_KEY])
    }

    private fun createDelegate(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ): DefaultWeChatDelegate {
        val configuration = CheckoutConfiguration(
            Environment.TEST,
            TEST_CLIENT_KEY,
        ) {
            weChatPayAction()
        }

        return DefaultWeChatDelegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = savedStateHandle,
            componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null),
            iwxApi = iwxApi,
            payRequestGenerator = weChatRequestGenerator,
            paymentDataRepository = paymentDataRepository,
            analyticsManager = analyticsManager,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private const val TEST_ACTION_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private const val TEST_PAYMENT_DATA = "TEST_PAYMENT_DATA"
    }
}

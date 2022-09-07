/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/8/2022.
 */

package com.adyen.checkout.wechatpay

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.components.model.payments.response.SdkAction
import com.adyen.checkout.components.model.payments.response.WeChatPaySdkData
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.core.exception.ComponentException
import com.tencent.mm.opensdk.modelpay.PayResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultWeChatDelegateTest(
    @Mock private val iwxApi: IWXAPI,
    @Mock private val weChatRequestGenerator: WeChatRequestGenerator<*>
) {

    private lateinit var paymentDataRepository: PaymentDataRepository
    private lateinit var delegate: DefaultWeChatDelegate

    @BeforeEach
    fun beforeEach() {
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        delegate = DefaultWeChatDelegate(
            iwxApi = iwxApi,
            payRequestGenerator = weChatRequestGenerator,
            paymentDataRepository = paymentDataRepository,
        )
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
            paymentData = "paymentData"
        )

        delegate.detailsFlow.test {
            delegate.handleAction(action, Activity())
            delegate.onResponse(payResponse)

            val expected = JSONObject().apply {
                put("resultCode", 0)
            }

            with(awaitItem()) {
                assertEquals(expected.toString(), details.toString())
                assertEquals("paymentData", paymentData)
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
            paymentData = "paymentData"
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
}

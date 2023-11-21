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
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.action.TwintSdkData
import com.adyen.checkout.components.core.action.WeChatPaySdkData
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class DefaultTwintDelegateTest {

    private lateinit var delegate: DefaultTwintDelegate

    @BeforeEach
    fun beforeEach() {
        val configuration = CheckoutConfiguration(Environment.TEST, "")

        delegate = DefaultTwintDelegate(
            observerRepository = ActionObserverRepository(),
            componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null),
            paymentDataRepository = PaymentDataRepository(SavedStateHandle()),
        )
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
        val detailsFlow = delegate.detailsFlow.test(testScheduler)
        val exceptionFlow = delegate.exceptionFlow.test(testScheduler)
        delegate.handleAction(SdkAction(paymentData = "test", sdkData = TwintSdkData("token")), Activity())

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

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"

        @JvmStatic
        fun handleActionSource() = listOf(
            arguments(AwaitAction(), "Unsupported action"),
            arguments(
                SdkAction(paymentData = "something", sdkData = WeChatPaySdkData()),
                "SDK Data is null or of wrong type",
            ),
            arguments(SdkAction<WeChatPaySdkData>(paymentData = "something"), "SDK Data is null or of wrong type"),
            arguments(SdkAction<TwintSdkData>(paymentData = null), "Payment data is null"),
            arguments(
                SdkAction<TwintSdkData>(paymentData = "something", sdkData = null),
                "SDK Data is null or of wrong type",
            ),
            // Success case: we cannot instantiate the Twint SDK (because it uses activity), but if this happens we
            // successfully handed the action over to Twint.
            arguments(
                SdkAction(paymentData = "something", sdkData = TwintSdkData("token")),
                "Twint not initialised before payment.",
            ),
        )

        @JvmStatic
        fun handleTwintResult() = listOf(
            arguments(
                TwintPayResult.TW_B_SUCCESS,
                TwintTestResult.Success(ActionComponentData("test", JSONObject())),
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

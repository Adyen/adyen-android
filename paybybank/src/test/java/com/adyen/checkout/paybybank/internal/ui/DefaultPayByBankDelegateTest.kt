/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/12/2022.
 */

package com.adyen.checkout.paybybank.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.Issuer
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel
import com.adyen.checkout.paybybank.PayByBankComponentState
import com.adyen.checkout.paybybank.PayByBankConfiguration
import com.adyen.checkout.paybybank.internal.ui.model.PayByBankOutputData
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultPayByBankDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
    @Mock private val submitHandler: SubmitHandler<PayByBankComponentState>,
) {

    private lateinit var delegate: DefaultPayByBankDelegate

    @BeforeEach
    fun beforeEach() {
        delegate = createPayByBankDelegate(
            issuers = listOf(
                Issuer(id = "issuer-id", name = "issuer-name")
            )
        )
        AdyenLogger.setLogLevel(Logger.NONE)
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `selectedIssuer is null, then output should be null`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData { selectedIssuer = null }

                with(expectMostRecentItem()) {
                    assertNull(selectedIssuer)
                }
            }
        }

        @Test
        fun `selectedIssuer is null, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData { selectedIssuer = null }

                with(expectMostRecentItem()) {
                    assertNull(selectedIssuer)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `selectedIssuer is valid, then output should be valid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    selectedIssuer = IssuerModel(id = "id", name = "test", environment = Environment.TEST)
                }

                with(expectMostRecentItem()) {
                    assertEquals("test", selectedIssuer?.name)
                    assertEquals("id", selectedIssuer?.id)
                    assertTrue(isValid)
                }
            }
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {

        @Test
        fun `output is invalid, then component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.updateComponentState(PayByBankOutputData(null, emptyList()))
                with(expectMostRecentItem()) {
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `output is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                val issuer = IssuerModel(id = "issuer-id", name = "issuer-name", environment = Environment.TEST)
                delegate.updateComponentState(
                    PayByBankOutputData(
                        issuer,
                        listOf(issuer)
                    )
                )
                with(expectMostRecentItem()) {
                    assertEquals("issuer-id", data.paymentMethod?.issuer)
                    assertEquals(TEST_ORDER, data.order)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }
            }
        }

        @Test
        fun `when issuers is empty, then component state should be valid`() = runTest {
            val configuration = getPayByBankConfigurationBuilder().build()
            delegate = createPayByBankDelegate(
                issuers = emptyList(),
                configuration = configuration,
            )
            delegate.componentStateFlow.test {
                with(expectMostRecentItem()) {
                    assertNull(data.paymentMethod?.issuer)
                    assertEquals(TEST_ORDER, data.order)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.paybybank.internal.ui.DefaultPayByBankDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            val configuration = getPayByBankConfigurationBuilder()
                .apply {
                    configurationValue?.let {
                        setAmount(it)
                    }
                }
                .build()
            delegate = createPayByBankDelegate(
                issuers = listOf(Issuer(id = "issuer-id", name = "issuer-name")),
                configuration = configuration,
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                val issuer = IssuerModel(id = "issuer-id", name = "issuer-name", environment = Environment.TEST)
                delegate.updateComponentState(
                    PayByBankOutputData(
                        issuer,
                        listOf(issuer)
                    )
                )
                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
        }
    }

    @Test
    fun `when issuers is empty in paymentMethod then viewFlow should emit null`() = runTest {
        delegate = createPayByBankDelegate(
            issuers = emptyList()
        )
        delegate.viewFlow.test {
            assertEquals(null, expectMostRecentItem())
        }
    }

    @Test
    fun `when issuers is not empty in paymentMethod then viewFlow should emit PayByBankComponentViewType`() = runTest {
        delegate = createPayByBankDelegate(
            issuers = listOf(
                Issuer(id = "issuer-id", name = "issuer-name")
            )
        )
        delegate.viewFlow.test {
            assertEquals(PayByBankComponentViewType, expectMostRecentItem())
        }
    }

    @Test
    fun `when delegate is initialized then analytics event is sent`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        verify(analyticsRepository).sendAnalyticsEvent()
    }

    @Nested
    inner class SubmitHandlerTest {

        @Test
        fun `when delegate is initialized then submit handler event is initialized`() = runTest {
            val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
            delegate.initialize(coroutineScope)
            verify(submitHandler).initialize(coroutineScope, delegate.componentStateFlow)
        }

        @Test
        fun `when delegate setInteractionBlocked is called then submit handler setInteractionBlocked is called`() =
            runTest {
                delegate.setInteractionBlocked(true)
                verify(submitHandler).setInteractionBlocked(true)
            }

        @Test
        fun `when delegate onSubmit is called then submit handler onSubmit is called`() = runTest {
            delegate.componentStateFlow.test {
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                delegate.onSubmit()
                verify(submitHandler).onSubmit(expectMostRecentItem())
            }
        }

        @Test
        fun `when no issuers in paymentMethod and delegate is initialized then submit handler onSubmit is called`() =
            runTest {
                delegate = createPayByBankDelegate(issuers = emptyList())
                delegate.componentStateFlow.test {
                    delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                    verify(submitHandler).onSubmit(expectMostRecentItem())
                }
            }
    }

    private fun getPayByBankConfigurationBuilder(): PayByBankConfiguration.Builder {
        return PayByBankConfiguration.Builder(
            Locale.US,
            Environment.TEST,
            TEST_CLIENT_KEY
        )
    }

    private fun createPayByBankDelegate(
        issuers: List<Issuer>,
        order: Order? = TEST_ORDER,
        configuration: PayByBankConfiguration = getPayByBankConfigurationBuilder().build(),
    ): DefaultPayByBankDelegate {
        return DefaultPayByBankDelegate(
            observerRepository = PaymentObserverRepository(),
            componentParams = GenericComponentParamsMapper(null, null).mapToParams(configuration, null),
            paymentMethod = PaymentMethod(
                issuers = issuers
            ),
            order = order,
            analyticsRepository = analyticsRepository,
            submitHandler = submitHandler,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, expectedComponentStateValue
            arguments(Amount("EUR", 100), Amount("EUR", 100)),
            arguments(Amount("USD", 0), Amount("USD", 0)),
            arguments(Amount.EMPTY, null),
            arguments(null, null),
        )
    }
}

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/12/2022.
 */

package com.adyen.checkout.paybybank

import app.cash.turbine.test
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.base.GenericComponentParamsMapper
import com.adyen.checkout.components.model.paymentmethods.Issuer
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.issuerlist.IssuerModel
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
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultPayByBankDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository
) {

    private lateinit var delegate: DefaultPayByBankDelegate

    private val configuration = PayByBankConfiguration.Builder(
        Locale.US,
        Environment.TEST,
        TEST_CLIENT_KEY
    ).build()

    @BeforeEach
    fun beforeEach() {
        delegate = DefaultPayByBankDelegate(
            observerRepository = PaymentObserverRepository(),
            componentParams = GenericComponentParamsMapper(null).mapToParams(configuration),
            paymentMethod = PaymentMethod(),
            analyticsRepository = analyticsRepository,
            submitHandler = SubmitHandler()
        )
        Logger.setLogcatLevel(Logger.NONE)
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
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }
            }
        }
    }

    @Test
    fun `when issuers is empty in paymentMethod then viewFlow should emit null`() = runTest {
        delegate = DefaultPayByBankDelegate(
            observerRepository = PaymentObserverRepository(),
            componentParams = GenericComponentParamsMapper(null).mapToParams(configuration),
            paymentMethod = PaymentMethod(
                issuers = emptyList()
            ),
            analyticsRepository = analyticsRepository,
            submitHandler = SubmitHandler()
        )
        delegate.viewFlow.test {
            assertEquals(null, expectMostRecentItem())
        }
    }


    @Test
    fun `when issuers is not empty in paymentMethod then viewFlow should emit PayByBankComponentViewType`() = runTest {
        delegate = DefaultPayByBankDelegate(
            observerRepository = PaymentObserverRepository(),
            componentParams = GenericComponentParamsMapper(null).mapToParams(configuration),
            paymentMethod = PaymentMethod(
                issuers = listOf(
                    Issuer(id = "issuer-id", name = "issuer-name")
                )
            ),
            analyticsRepository = analyticsRepository,
            submitHandler = SubmitHandler()
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


    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}

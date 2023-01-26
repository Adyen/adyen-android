/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 17/8/2022.
 */

package com.adyen.checkout.issuerlist

import app.cash.turbine.test
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.SubmitHandlerOld
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.issuerlist.utils.TestIssuerListConfiguration
import com.adyen.checkout.issuerlist.utils.TestIssuerPaymentMethod
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
internal class DefaultIssuerListDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
) {

    private lateinit var delegate: DefaultIssuerListDelegate<*>

    @BeforeEach
    fun beforeEach() {
        delegate = createIssuerListDelegate()
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
                delegate.updateComponentState(IssuerListOutputData(null))
                with(expectMostRecentItem()) {
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `output is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    IssuerListOutputData(
                        IssuerModel(
                            id = "issuer-id",
                            name = "issuer-name",
                            environment = Environment.TEST
                        )
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
    fun `when configuration viewType is RECYCLER_VIEW then viewFlow should emit RECYCLER_VIEW`() = runTest {
        val configuration: IssuerListConfiguration = TestIssuerListConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .setViewType(IssuerListViewType.RECYCLER_VIEW)
            .build()

        delegate = DefaultIssuerListDelegate(
            observerRepository = PaymentObserverRepository(),
            componentParams = IssuerListComponentParamsMapper(null).mapToParams(configuration),
            paymentMethod = PaymentMethod(),
            analyticsRepository = analyticsRepository,
            submitHandler = SubmitHandlerOld()
        ) { TestIssuerPaymentMethod() }

        delegate.viewFlow.test {
            assertEquals(IssuerListComponentViewType.RecyclerView, expectMostRecentItem())
        }
    }

    @Test
    fun `when configuration viewType is SPINNER_VIEW then viewFlow should emit SPINNER_VIEW`() = runTest {
        val configuration: IssuerListConfiguration = TestIssuerListConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .setViewType(IssuerListViewType.SPINNER_VIEW)
            .build()

        delegate = DefaultIssuerListDelegate(
            observerRepository = PaymentObserverRepository(),
            componentParams = IssuerListComponentParamsMapper(null).mapToParams(configuration),
            paymentMethod = PaymentMethod(),
            analyticsRepository = analyticsRepository,
            submitHandler = SubmitHandlerOld()
        ) { TestIssuerPaymentMethod() }
        delegate.viewFlow.test {
            assertEquals(IssuerListComponentViewType.SpinnerView, expectMostRecentItem())
        }
    }

    @Test
    fun `when delegate is initialized then analytics event is sent`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        verify(analyticsRepository).sendAnalyticsEvent()
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createIssuerListDelegate(
                configuration = getDefaultTestIssuerListConfigurationBuilder()
                    .setViewType(IssuerListViewType.SPINNER_VIEW)
                    .setSubmitButtonVisible(false)
                    .build()
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createIssuerListDelegate(
                configuration = getDefaultTestIssuerListConfigurationBuilder()
                    .setViewType(IssuerListViewType.SPINNER_VIEW)
                    .setSubmitButtonVisible(true)
                    .build()
            )

            assertTrue(delegate.shouldShowSubmitButton())
        }
    }

    private fun createIssuerListDelegate(
        configuration: TestIssuerListConfiguration = getDefaultTestIssuerListConfigurationBuilder().build()
    ) = DefaultIssuerListDelegate(
        observerRepository = PaymentObserverRepository(),
        componentParams = IssuerListComponentParamsMapper(null).mapToParams(configuration),
        paymentMethod = PaymentMethod(),
        analyticsRepository = analyticsRepository,
        submitHandler = SubmitHandlerOld()
    ) { TestIssuerPaymentMethod() }

    private fun getDefaultTestIssuerListConfigurationBuilder() = TestIssuerListConfiguration.Builder(
        Locale.US,
        Environment.TEST,
        TEST_CLIENT_KEY_1
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}

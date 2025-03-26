/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/7/2022.
 */

package com.adyen.checkout.mbway.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.ui.model.state.DelegateStateManager
import com.adyen.checkout.components.core.internal.ui.model.transformer.DefaultTransformerRegistry
import com.adyen.checkout.core.Environment
import com.adyen.checkout.mbway.MBWayComponentState
import com.adyen.checkout.mbway.MBWayConfiguration
import com.adyen.checkout.mbway.getMBWayConfiguration
import com.adyen.checkout.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.mbway.internal.ui.model.MBWayFieldId
import com.adyen.checkout.mbway.mbWay
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultMBWayDelegateTest(
    @Mock private val submitHandler: SubmitHandler<MBWayComponentState>,
    @Mock private val stateManager: DelegateStateManager<MBWayDelegateState, MBWayFieldId>,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultMBWayDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        delegate = createMBWayDelegate()
    }

    @Test
    fun `when state is updated, then component state should be created`() = runTest {
        val delegateState = createMBWayDelegateState(
            countryCodeValue = CountryModel("NL", "Netherlands", "+31"),
            countryCodeValidation = Validation.Valid,
            localPhoneNumberValue = "2345678901",
            localPhoneNumberValidation = Validation.Valid,
        )
        initializeDelegate(CoroutineScope(UnconfinedTestDispatcher()), delegateState)

        delegate.componentStateFlow.test {
            with(awaitItem()) {
                assertEquals("+312345678901", data.paymentMethod?.telephoneNumber)
                assertTrue(isInputValid)
                assertTrue(isValid)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when state is updated, then view state should be created`() = runTest {
        val delegateState = createMBWayDelegateState(
            countries = listOf(
                CountryModel("PT", "Portugal", "+351"),
                CountryModel("NL", "Netherlands", "+31"),
            ),
            countryCodeValue = CountryModel("NL", "Netherlands", "+31"),
            localPhoneNumberValue = "+312345678901",
        )
        initializeDelegate(CoroutineScope(UnconfinedTestDispatcher()), delegateState)

        delegate.viewStateFlow.test {
            with(awaitItem()) {
                assertEquals(delegateState.countries, countries)
                assertEquals(delegateState.countryCodeFieldState.value, countryCodeFieldState.value)
                assertEquals(delegateState.localPhoneNumberFieldState.value, phoneNumberFieldState.value)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @ParameterizedTest
    @MethodSource("com.adyen.checkout.mbway.internal.ui.DefaultMBWayDelegateTest#amountSource")
    fun `when state is valid then amount is propagated in component state if set`(
        configurationValue: Amount?,
        expectedComponentStateValue: Amount?,
    ) = runTest {
        if (configurationValue != null) {
            val configuration = createCheckoutConfiguration(configurationValue)
            delegate = createMBWayDelegate(configuration = configuration)
        }
        initializeDelegate(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.componentStateFlow.test {
            delegate.onFieldValueChanged(MBWayFieldId.LOCAL_PHONE_NUMBER, "234567890")
            assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
        }
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createMBWayDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createMBWayDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(true)
                },
            )

            assertTrue(delegate.shouldShowSubmitButton())
        }
    }

    @Nested
    inner class SubmitHandlerTest {

        @Test
        fun `when delegate is initialized then submit handler event is initialized`() = runTest {
            val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
            initializeDelegate(coroutineScope)
            verify(submitHandler).initialize(coroutineScope, delegate.componentStateFlow)
        }

        @Test
        fun `when delegate setInteractionBlocked is called then submit handler setInteractionBlocked is called`() =
            runTest {
                delegate.setInteractionBlocked(true)
                verify(submitHandler).setInteractionBlocked(true)
            }

        @Test
        fun `when delegate onSubmit is called and state is valid, then submit handler onSubmit is called`() = runTest {
            initializeDelegate(CoroutineScope(UnconfinedTestDispatcher()))
            whenever(stateManager.isValid) doReturn true

            delegate.componentStateFlow.test {
                delegate.onSubmit()
                verify(submitHandler).onSubmit(expectMostRecentItem())
                verify(stateManager, never()).highlightAllFieldValidationErrors()
            }
        }

        @Test
        fun `when delegate onSubmit is called and state is invalid, then validation errors are highlighted`() =
            runTest {
                initializeDelegate(CoroutineScope(UnconfinedTestDispatcher()))
                whenever(stateManager.isValid) doReturn false

                delegate.componentStateFlow.test {
                    delegate.onSubmit()
                    verify(submitHandler, never()).onSubmit(expectMostRecentItem())
                    verify(stateManager).highlightAllFieldValidationErrors()
                }
            }
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when delegate is initialized then analytics manager is initialized`() {
            initializeDelegate(CoroutineScope(UnconfinedTestDispatcher()))

            analyticsManager.assertIsInitialized()
        }

        @Test
        fun `when delegate is initialized, then render event is tracked`() {
            initializeDelegate(CoroutineScope(UnconfinedTestDispatcher()))

            val expectedEvent = GenericEvents.rendered(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when submitFlow emits an event, then submit event is tracked`() = runTest {
            val submitFlow = flow<MBWayComponentState> { emit(mock()) }
            whenever(submitHandler.submitFlow) doReturn submitFlow
            val delegate = createMBWayDelegate()

            delegate.submitFlow.collectLatest {
                val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
                analyticsManager.assertLastEventEquals(expectedEvent)
            }
        }

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            analyticsManager.setCheckoutAttemptId(TEST_CHECKOUT_ATTEMPT_ID)
            val delegateState = createMBWayDelegateState(
                countryCodeValidation = Validation.Valid,
                localPhoneNumberValidation = Validation.Valid,
            )

            initializeDelegate(CoroutineScope(UnconfinedTestDispatcher()), delegateState)

            delegate.componentStateFlow.test {
                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.onCleared()

            analyticsManager.assertIsCleared()
        }
    }

    @Test
    fun `when field value changes, then field is updated in the state`() {
        initializeDelegate(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.onFieldValueChanged(MBWayFieldId.LOCAL_PHONE_NUMBER, value = "123456789")

        verify(stateManager).updateFieldValue(
            fieldId = eq(MBWayFieldId.LOCAL_PHONE_NUMBER),
            value = eq("123456789"),
        )
    }

    @Test
    fun `when field focus changes, then field is updated in the state`() {
        initializeDelegate(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.onFieldFocusChanged(MBWayFieldId.LOCAL_PHONE_NUMBER, hasFocus = true)

        verify(stateManager).updateFieldFocus(
            fieldId = eq(MBWayFieldId.LOCAL_PHONE_NUMBER),
            hasFocus = eq(true),
        )
    }

    private fun initializeDelegate(
        coroutineScope: CoroutineScope,
        delegateState: MBWayDelegateState = createMBWayDelegateState()
    ) {
        whenever(stateManager.state) doReturn MutableStateFlow(delegateState)
        delegate.initialize(coroutineScope)
    }

    private fun createMBWayDelegateState(
        countries: List<CountryModel> = emptyList(),
        countryCodeValue: CountryModel = CountryModel("", "", ""),
        countryCodeValidation: Validation = Validation.Valid,
        localPhoneNumberValue: String = "",
        localPhoneNumberValidation: Validation = Validation.Valid,
    ) = MBWayDelegateState(
        countries = countries,
        countryCodeFieldState = ComponentFieldDelegateState(
            value = countryCodeValue,
            validation = countryCodeValidation,
        ),
        localPhoneNumberFieldState = ComponentFieldDelegateState(
            value = localPhoneNumberValue,
            validation = localPhoneNumberValidation,
        ),
    )

    private fun createMBWayDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
    ) = DefaultMBWayDelegate(
        observerRepository = PaymentObserverRepository(),
        paymentMethod = PaymentMethod(type = TEST_PAYMENT_METHOD_TYPE),
        order = TEST_ORDER,
        componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
            componentConfiguration = configuration.getMBWayConfiguration(),
        ),
        analyticsManager = analyticsManager,
        submitHandler = submitHandler,
        transformerRegistry = DefaultTransformerRegistry(),
        stateManager = stateManager,
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: MBWayConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        mbWay(configuration)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, expectedComponentStateValue
            arguments(Amount("EUR", 100), Amount("EUR", 100)),
            arguments(Amount("USD", 0), Amount("USD", 0)),
            arguments(null, null),
            arguments(null, null),
        )
    }
}

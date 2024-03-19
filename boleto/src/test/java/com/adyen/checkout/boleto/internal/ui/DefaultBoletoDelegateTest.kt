/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.boleto.BoletoComponentState
import com.adyen.checkout.boleto.BoletoConfiguration
import com.adyen.checkout.boleto.boleto
import com.adyen.checkout.boleto.internal.ui.model.BoletoComponentParamsMapper
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.ui.core.internal.test.TestAddressRepository
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class DefaultBoletoDelegateTest(
    @Mock private val submitHandler: SubmitHandler<BoletoComponentState>,
    @Mock private val analyticsManager: AnalyticsManager,
) {

    private lateinit var delegate: DefaultBoletoDelegate

    private lateinit var addressRepository: TestAddressRepository

    @BeforeEach
    fun beforeEach() {
        addressRepository = TestAddressRepository()
        delegate = createBoletoDelegate()
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `input data is valid, then output must be valid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = "Etman"
                socialSecurityNumber = "568.617.525-09"
                address = createAddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test.com"
            }

            assertTrue(outputTestFlow.latestValue.isValid)
            outputTestFlow.cancel()
        }

        @Test
        fun `all inputs are empty, then output should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {}

            assertFalse(outputTestFlow.latestValue.isValid)
            outputTestFlow.cancel()
        }

        @Test
        fun `first name is empty and other inputs are valid, then output should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = " "
                lastName = "Etman"
                socialSecurityNumber = "568.617.525-09"
                address = createAddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test.com"
            }

            assertFalse(outputTestFlow.latestValue.isValid)
            outputTestFlow.cancel()
        }

        @Test
        fun `last name is empty and other inputs are valid, then output should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = " "
                socialSecurityNumber = "568.617.525-09"
                address = createAddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test.com"
            }

            assertFalse(outputTestFlow.latestValue.isValid)
            outputTestFlow.cancel()
        }

        @Test
        fun `social security number is empty and other inputs are valid, then output should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = "Etman"
                socialSecurityNumber = " "
                address = createAddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test.com"
            }

            assertFalse(outputTestFlow.latestValue.isValid)
            outputTestFlow.cancel()
        }

        @Test
        fun `social security number is invalid and other inputs are valid, then output should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = "Etman"
                socialSecurityNumber = "123.456.789-0"
                address = createAddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test.com"
            }

            assertFalse(outputTestFlow.latestValue.isValid)
            outputTestFlow.cancel()
        }

        @Test
        fun `address is empty and other inputs are valid, then output should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = "Etman"
                socialSecurityNumber = "568.617.525-09"
                address = AddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test.com"
            }

            assertFalse(outputTestFlow.latestValue.isValid)
            outputTestFlow.cancel()
        }

        @Test
        fun `email is empty and isEmailCopySelected equals true, then output should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = "Etman"
                socialSecurityNumber = "568.617.525-09"
                address = AddressInputModel()
                isSendEmailSelected = true
                shopperEmail = " "
            }

            assertFalse(outputTestFlow.latestValue.isValid)
            outputTestFlow.cancel()
        }

        @Test
        fun `email is invalid and isEmailCopySelected equals true, then output should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = "Etman"
                socialSecurityNumber = "568.617.525-09"
                address = AddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test"
            }

            assertFalse(outputTestFlow.latestValue.isValid)
            outputTestFlow.cancel()
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {

        @Test
        fun `output is valid, then component state should be valid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = "Etman"
                socialSecurityNumber = "568.617.525-09"
                address = createAddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test.com"
            }

            with(componentStateTestFlow.latestValue) {
                assertTrue(isInputValid)
                assertTrue(isValid)
            }
        }

        @Test
        fun `output is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateInputData {}

            with(componentStateTestFlow.latestValue) {
                assertFalse(isInputValid)
                assertFalse(isValid)
            }
        }

        @Test
        fun `first name is empty and other inputs are valid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = " "
                lastName = "Etman"
                socialSecurityNumber = "568.617.525-09"
                address = createAddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test.com"
            }

            with(componentStateTestFlow.latestValue) {
                assertFalse(isInputValid)
                assertFalse(isValid)
            }
        }

        @Test
        fun `last name is empty and other inputs are valid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = " "
                socialSecurityNumber = "568.617.525-09"
                address = createAddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test.com"
            }

            with(componentStateTestFlow.latestValue) {
                assertFalse(isInputValid)
                assertFalse(isValid)
            }
        }

        @Test
        fun `social security number is empty and other inputs are valid, then component state should be invalid`() =
            runTest {
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

                delegate.updateInputData {
                    firstName = "Atef"
                    lastName = "Etman"
                    socialSecurityNumber = " "
                    address = createAddressInputModel()
                    isSendEmailSelected = true
                    shopperEmail = "atef@test.com"
                }

                with(componentStateTestFlow.latestValue) {
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }
            }

        @Test
        fun `social security number is invalid input and other inputs are valid, then component state should be invalid`() =
            runTest {
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

                delegate.updateInputData {
                    firstName = "Atef"
                    lastName = "Etman"
                    socialSecurityNumber = "123.456.789-0"
                    address = createAddressInputModel()
                    isSendEmailSelected = true
                    shopperEmail = "atef@test.com"
                }

                with(componentStateTestFlow.latestValue) {
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }
            }

        @Test
        fun `social security number is invalid pattern and other inputs are valid, then component state should be invalid`() =
            runTest {
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

                delegate.updateInputData {
                    firstName = "Atef"
                    lastName = "Etman"
                    socialSecurityNumber = "56861752509"
                    address = createAddressInputModel()
                    isSendEmailSelected = true
                    shopperEmail = "atef@test.com"
                }

                with(componentStateTestFlow.latestValue) {
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }
            }

        @Test
        fun `address is empty and other inputs are valid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = "Etman"
                socialSecurityNumber = "568.617.525-09"
                address = AddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test.com"
            }

            with(componentStateTestFlow.latestValue) {
                assertFalse(isInputValid)
                assertFalse(isValid)
            }
        }

        @Test
        fun `email is empty and isEmailCopySelected equals true, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = "Etman"
                socialSecurityNumber = "568.617.525-09"
                address = AddressInputModel()
                isSendEmailSelected = true
                shopperEmail = " "
            }

            with(componentStateTestFlow.latestValue) {
                assertFalse(isInputValid)
                assertFalse(isValid)
            }
        }

        @Test
        fun `email is invalid and isEmailCopySelected equals true, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateInputData {
                firstName = "Atef"
                lastName = "Etman"
                socialSecurityNumber = "568.617.525-09"
                address = AddressInputModel()
                isSendEmailSelected = true
                shopperEmail = "atef@test"
            }

            with(componentStateTestFlow.latestValue) {
                assertFalse(isInputValid)
                assertFalse(isValid)
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.boleto.internal.ui.DefaultBoletoDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = createCheckoutConfiguration(configurationValue)
                delegate = createBoletoDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    firstName = "Test"
                }
                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
        }
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
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when delegate is initialized then analytics manager is initialized`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            verify(analyticsManager).initialize(eq(delegate), any())
        }

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            whenever(analyticsManager.getCheckoutAttemptId()) doReturn TEST_CHECKOUT_ATTEMPT_ID

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    firstName = "Test"
                }

                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.onCleared()
            verify(analyticsManager).clear(eq(delegate))
        }
    }

    @Suppress("LongParameterList")
    private fun createBoletoDelegate(
        submitHandler: SubmitHandler<BoletoComponentState> = this.submitHandler,
        analyticsManager: AnalyticsManager = this.analyticsManager,
        paymentMethod: PaymentMethod = PaymentMethod(),
        addressRepository: TestAddressRepository = this.addressRepository,
        order: Order? = TEST_ORDER,
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
    ) = DefaultBoletoDelegate(
        submitHandler = submitHandler,
        analyticsManager = analyticsManager,
        observerRepository = PaymentObserverRepository(),
        paymentMethod = paymentMethod,
        order = order,
        componentParams = BoletoComponentParamsMapper(CommonComponentParamsMapper())
            .mapToParams(configuration, Locale.US, null, null),
        addressRepository = addressRepository,
    )

    @Suppress("LongParameterList")
    private fun createAddressInputModel(
        postalCode: String = "12345678",
        street: String = "Rua Funcionarios",
        stateOrProvince: String = "SP",
        houseNumberOrName: String = "952",
        apartmentSuite: String = "",
        city: String = "São Paulo",
        country: String = BRAZIL_COUNTRY_CODE
    ) = AddressInputModel(
        postalCode = postalCode,
        street = street,
        stateOrProvince = stateOrProvince,
        houseNumberOrName = houseNumberOrName,
        apartmentSuite = apartmentSuite,
        city = city,
        country = country,
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: BoletoConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        boleto(configuration)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val BRAZIL_COUNTRY_CODE = "BR"
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"

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

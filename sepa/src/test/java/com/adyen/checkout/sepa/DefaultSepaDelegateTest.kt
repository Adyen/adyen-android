package com.adyen.checkout.sepa

import app.cash.turbine.test
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import com.adyen.checkout.core.api.Environment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultSepaDelegateTest {

    private lateinit var delegate: DefaultSepaDelegate

    @BeforeEach
    fun before() {
        delegate = DefaultSepaDelegate(
            paymentMethod = PaymentMethod(),
            configuration = SepaConfiguration.Builder(
                Locale.US,
                Environment.TEST,
                TEST_CLIENT_KEY
            ).build(),
        )
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `everything is good, then output data should be propagated`() = runTest {
            delegate.outputDataFlow.test {
                skipItems(1)
                delegate.updateInputData {
                    name = "name"
                    iban = "NL02ABNA0123456789"
                }

                with(awaitItem()) {
                    assertEquals("name", ownerNameField.value)
                    assertEquals("NL02ABNA0123456789", ibanNumberField.value)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `when creating component state is successful, then the state is propagated`() = runTest {
        delegate.componentStateFlow.test {
            skipItems(1)
            delegate.updateComponentState(SepaOutputData("name", "NL02ABNA0123456789"))

            with(awaitItem()) {
                assertTrue(data.paymentMethod is SepaPaymentMethod)
                assertTrue(isInputValid)
                assertTrue(isReady)
                assertEquals("name", data.paymentMethod?.ownerName)
                assertEquals("NL02ABNA0123456789", data.paymentMethod?.iban)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}

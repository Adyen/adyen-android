package com.adyen.checkout.sepa

import app.cash.turbine.test
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultSepaDelegateTest {

    val delegate = DefaultSepaDelegate(PaymentMethod())

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `everything is good, then output data should be propagated`() = runTest {
            delegate.outputDataFlow.test {
                skipItems(1)
                delegate.onInputDataChanged(SepaInputData(name = "name", iban = "NL02ABNA0123456789"))
                val sepaOutputData = awaitItem()

                assertEquals("name", sepaOutputData?.ownerNameField?.value)
                assertEquals("NL02ABNA0123456789", sepaOutputData?.ibanNumberField?.value)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `when creating component state is successful, then the state is propagated`() = runTest {
        delegate.componentStateFlow.test {
            skipItems(1)
            delegate.createComponentState(SepaOutputData("name", "NL02ABNA0123456789"))

            val componentState = awaitItem()
            assertTrue(componentState!!.data.paymentMethod is SepaPaymentMethod)
            assertTrue(componentState.isInputValid)
            assertTrue(componentState.isReady)
            assertEquals("name", componentState.data.paymentMethod?.ownerName)
            assertEquals("NL02ABNA0123456789", componentState.data.paymentMethod?.iban)

            cancelAndIgnoreRemainingEvents()
        }
    }
}

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 20/9/2022.
 */

package com.adyen.checkout.onlinebankingcore

import android.content.Context
import app.cash.turbine.test
import com.adyen.checkout.components.base.GenericComponentParamsMapper
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.api.Environment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultOnlineBankingDelegateTest {

    private lateinit var delegate: DefaultOnlineBankingDelegate<OnlineBankingCZPaymentMethod>

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var pdfOpener: PdfOpener

    @BeforeEach
    fun setup() {
        val configuration = TestOnlineBankingConfiguration.Builder(
            Locale.US,
            Environment.TEST,
            TEST_CLIENT_KEY
        ).build()
        delegate = DefaultOnlineBankingDelegate(
            observerRepository = PaymentObserverRepository(),
            pdfOpener = pdfOpener,
            paymentMethod = PaymentMethod(),
            componentParams = GenericComponentParamsMapper(null).mapToParams(configuration),
            termsAndConditionsUrl = TEST_URL,
            paymentMethodFactory = { OnlineBankingCZPaymentMethod() }
        )
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

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
                delegate.updateInputData { selectedIssuer = OnlineBankingModel(id = "id", name = "test") }

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
                val output = OnlineBankingOutputData(null)

                delegate.updateComponentState(output)

                with(expectMostRecentItem()) {
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `output is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                val model = OnlineBankingModel(id = "issuer-id", name = "issuer-name")
                val output = OnlineBankingOutputData(model)

                delegate.updateComponentState(output)

                with(expectMostRecentItem()) {
                    assertEquals("issuer-id", data.paymentMethod?.issuer)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }
            }
        }
    }

    @Nested
    @DisplayName("when opening terms and conditions and it")
    inner class TermsAndConditionsTest {
        @Test
        fun `successfully opens`() {
            val url = TEST_URL

            delegate.openTermsAndConditions(context)

            verify(pdfOpener).open(context, url)
        }

        @Test
        fun `failed to open pdf and throws an exception`() {
            val url = TEST_URL
            whenever(pdfOpener.open(context, url)) doThrow IllegalStateException("failed")

            delegate.openTermsAndConditions(context)

            assertThrows<IllegalStateException> { pdfOpener.open(context, url) }
        }
    }

    companion object {
        private const val TEST_URL = "any-url"
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}

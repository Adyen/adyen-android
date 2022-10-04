/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 17/8/2022.
 */

package com.adyen.checkout.issuerlist

import app.cash.turbine.test
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.issuerlist.utils.TestIssuerPaymentMethod
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
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultIssuerListDelegateTest(
    @Mock private val configuration: IssuerListConfiguration,
) {

    private lateinit var delegate: DefaultIssuerListDelegate<*>

    @BeforeEach
    fun beforeEach() {
        whenever(configuration.viewType) doReturn IssuerListViewType.RECYCLER_VIEW
        delegate = DefaultIssuerListDelegate(configuration, PaymentMethod()) { TestIssuerPaymentMethod() }
        Logger.setLogcatLevel(Logger.NONE)
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `selectedIssuer is null, then output should be null`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(IssuerListInputData(null))
                with(requireNotNull(expectMostRecentItem())) {
                    assertNull(selectedIssuer)
                }
            }
        }

        @Test
        fun `selectedIssuer is null, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(IssuerListInputData(null))
                with(requireNotNull(expectMostRecentItem())) {
                    assertNull(selectedIssuer)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `selectedIssuer is valid, then output should be valid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(IssuerListInputData(IssuerModel(id = "id", name = "test")))
                with(requireNotNull(expectMostRecentItem())) {
                    assertEquals("test", selectedIssuer?.name)
                    assertEquals("id", selectedIssuer?.id)
                    assertTrue(isValid)
                }
            }
        }

        @Test
        fun `selectedIssuer is null, then component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(IssuerListInputData())
                with(requireNotNull(expectMostRecentItem())) {
                    assertEquals("", data.paymentMethod?.issuer)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `selectIssuer is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(
                    IssuerListInputData(selectedIssuer = IssuerModel(id = "issuer-id", name = "issuer-name"))
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertEquals("issuer-id", data.paymentMethod?.issuer)
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
                delegate.createComponentState(IssuerListOutputData(null))
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `output is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.createComponentState(IssuerListOutputData(IssuerModel(id = "issuer-id", name = "issuer-name")))
                with(requireNotNull(expectMostRecentItem())) {
                    assertEquals("issuer-id", data.paymentMethod?.issuer)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }
            }
        }
    }

    @Test
    fun `when configuration viewType is RECYCLER_VIEW then viewFlow should emit RECYCLER_VIEW`() = runTest {
        whenever(configuration.viewType) doReturn IssuerListViewType.RECYCLER_VIEW
        delegate.viewFlow.test {
            assertEquals(IssuerListComponentViewType.RECYCLER_VIEW, expectMostRecentItem())
        }
    }

    @Test
    fun `when configuration viewType is SPINNER_VIEW then viewFlow should emit SPINNER_VIEW`() = runTest {
        whenever(configuration.viewType) doReturn IssuerListViewType.SPINNER_VIEW
        delegate = DefaultIssuerListDelegate(configuration, PaymentMethod()) { TestIssuerPaymentMethod() }
        delegate.viewFlow.test {
            assertEquals(IssuerListComponentViewType.SPINNER_VIEW, expectMostRecentItem())
        }
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}

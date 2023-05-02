/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2022.
 */

package com.adyen.checkout.issuerlist.internal

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.issuerlist.TestIssuerComponentState
import com.adyen.checkout.issuerlist.internal.ui.IssuerListComponentViewType
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate
import com.adyen.checkout.issuerlist.utils.TestIssuerListComponent
import com.adyen.checkout.issuerlist.utils.TestIssuerPaymentMethod
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.checkout.ui.core.internal.test.TestComponentViewType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class IssuerListComponentTest(
    @Mock private val issuerListDelegate: IssuerListDelegate<TestIssuerPaymentMethod, TestIssuerComponentState>,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
    @Mock private val componentEventHandler: ComponentEventHandler<TestIssuerComponentState>,
) {

    // We created TestIssuerListComponent to be able to run our tests, because IssuerListComponent is an abstract class
    // and all of the components that extend it are in other modules not accessible from this one
    private lateinit var component: TestIssuerListComponent

    @BeforeEach
    fun before() {
        whenever(issuerListDelegate.viewFlow) doReturn MutableStateFlow(IssuerListComponentViewType.SpinnerView)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        component = TestIssuerListComponent(
            issuerListDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
        AdyenLogger.setLogLevel(Logger.NONE)
    }

    @Test
    fun `when component is created then delegates are initialized`() {
        verify(issuerListDelegate).initialize(component.viewModelScope)
        verify(genericActionDelegate).initialize(component.viewModelScope)
        verify(componentEventHandler).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared then delegates are cleared`() {
        component.invokeOnCleared()

        verify(issuerListDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when observe is called then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<TestIssuerComponentState>) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(issuerListDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(component.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegates is called`() {
        component.removeObserver()

        verify(issuerListDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized then view flow should match issuer list delegate view flow`() = runTest {
        component.viewFlow.test {
            assertEquals(IssuerListComponentViewType.SpinnerView, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `when issuer list delegate view flow emits a value then component view flow should match that value`() =
        runTest {
            val issuerListDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
            whenever(issuerListDelegate.viewFlow) doReturn issuerListDelegateViewFlow
            component = TestIssuerListComponent(
                issuerListDelegate,
                genericActionDelegate,
                actionHandlingComponent,
                componentEventHandler,
            )

            component.viewFlow.test {
                assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())

                issuerListDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
                assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

                expectNoEvents()
            }
        }

    @Test
    fun `when action delegate view flow emits a value then component view flow should match that value`() = runTest {
        val actionDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn actionDelegateViewFlow
        component = TestIssuerListComponent(
            issuerListDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )

        component.viewFlow.test {
            // this value should match the value of the main delegate and not the action delegate
            // and in practice the initial value of the action delegate view flow is always null so it should be ignored
            assertEquals(IssuerListComponentViewType.SpinnerView, awaitItem())

            actionDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }

    @Test
    fun `when isConfirmationRequired, then delegate is called`() {
        component.isConfirmationRequired()
        verify(issuerListDelegate).isConfirmationRequired()
    }

    @Test
    fun `when submit is called and active delegate is the payment delegate, then delegate onSubmit is called`() {
        whenever(component.delegate).thenReturn(issuerListDelegate)
        component.submit()
        verify(issuerListDelegate).onSubmit()
    }

    @Test
    fun `when submit is called and active delegate is the action delegate, then delegate onSubmit is not called`() {
        whenever(component.delegate).thenReturn(genericActionDelegate)
        component.submit()
        verify(issuerListDelegate, never()).onSubmit()
    }
}

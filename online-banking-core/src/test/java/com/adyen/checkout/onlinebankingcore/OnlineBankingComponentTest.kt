/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2022.
 */

package com.adyen.checkout.onlinebankingcore

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.test.TestComponentViewType
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.onlinebankingcore.utils.TestOnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.utils.TestOnlineBankingPaymentMethod
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class OnlineBankingComponentTest(
    @Mock private val onlineBankingDelegate: OnlineBankingDelegate<TestOnlineBankingPaymentMethod>,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
) {

    // We created TestOnlineBankingComponent to be able to run our tests, because OnlineBankingComponent is an abstract
    // class and all of the components that extend it are in other modules not accessible from this one
    private lateinit var component: TestOnlineBankingComponent

    @BeforeEach
    fun before() {
        whenever(onlineBankingDelegate.viewFlow) doReturn MutableStateFlow(OnlineBankingComponentViewType)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        component = TestOnlineBankingComponent(
            onlineBankingDelegate,
            genericActionDelegate,
            actionHandlingComponent,
        )
        Logger.setLogcatLevel(Logger.NONE)
    }

    @Test
    fun `when component is created then delegates are initialized`() {
        verify(onlineBankingDelegate).initialize(component.viewModelScope)
        verify(genericActionDelegate).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared then delegates are cleared`() {
        component.invokeOnCleared()

        verify(onlineBankingDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
    }

    @Test
    fun `when observe is called then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<PaymentComponentState<TestOnlineBankingPaymentMethod>>) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(onlineBankingDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(component.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegates is called`() {
        component.removeObserver()

        verify(onlineBankingDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized then view flow should match online banking delegate view flow`() = runTest {
        component.viewFlow.test {
            assertEquals(OnlineBankingComponentViewType, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `when online banking delegate view flow emits a value then component view flow should match that value`() =
        runTest {
            val onlineBankingDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
            whenever(onlineBankingDelegate.viewFlow) doReturn onlineBankingDelegateViewFlow
            component =
                TestOnlineBankingComponent(onlineBankingDelegate, genericActionDelegate, actionHandlingComponent)

            component.viewFlow.test {
                assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())

                onlineBankingDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
                assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

                expectNoEvents()
            }
        }

    @Test
    fun `when action delegate view flow emits a value then component view flow should match that value`() = runTest {
        val actionDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn actionDelegateViewFlow
        component = TestOnlineBankingComponent(onlineBankingDelegate, genericActionDelegate, actionHandlingComponent)

        component.viewFlow.test {
            // this value should match the value of the main delegate and not the action delegate
            // and in practice the initial value of the action delegate view flow is always null so it should be ignored
            assertEquals(OnlineBankingComponentViewType, awaitItem())

            actionDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }
}

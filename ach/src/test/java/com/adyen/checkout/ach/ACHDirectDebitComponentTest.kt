/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 31/1/2023.
 */

package com.adyen.checkout.ach

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.ach.internal.ui.ACHDirectDebitComponentViewType
import com.adyen.checkout.ach.internal.ui.DefaultACHDirectDebitDelegate
import com.adyen.checkout.ach.internal.ui.StoredACHDirectDebitDelegate
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.checkout.ui.core.internal.ui.TestComponentViewType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
internal class ACHDirectDebitComponentTest(
    @Mock private val defaultAchDelegate: DefaultACHDirectDebitDelegate,
    @Mock private val storedAchDelegate: StoredACHDirectDebitDelegate,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
    @Mock private val componentEventHandler: ComponentEventHandler<ACHDirectDebitComponentState>,
) {

    private lateinit var defaultAchComponent: ACHDirectDebitComponent
    private lateinit var storedAchComponent: ACHDirectDebitComponent

    @BeforeEach
    fun before() {
        whenever(defaultAchDelegate.viewFlow) doReturn MutableStateFlow(ACHDirectDebitComponentViewType)
        whenever(storedAchDelegate.viewFlow) doReturn MutableStateFlow(null)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        defaultAchComponent = ACHDirectDebitComponent(
            defaultAchDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler
        )

        storedAchComponent = ACHDirectDebitComponent(
            storedAchDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler
        )
    }

    @Test
    fun `when default component is created then delegates are initialized`() {
        verify(defaultAchDelegate).initialize(defaultAchComponent.viewModelScope)
        verify(genericActionDelegate).initialize(defaultAchComponent.viewModelScope)
        verify(componentEventHandler).initialize(defaultAchComponent.viewModelScope)
    }

    @Test
    fun `when stored component is created then delegates are initialized`() {
        verify(storedAchDelegate).initialize(storedAchComponent.viewModelScope)
        verify(genericActionDelegate).initialize(storedAchComponent.viewModelScope)
        verify(componentEventHandler).initialize(storedAchComponent.viewModelScope)
    }

    @Test
    fun `when default component is cleared then delegates are cleared`() {
        defaultAchComponent.invokeOnCleared()

        verify(defaultAchDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when stored component is cleared then delegates are cleared`() {
        storedAchComponent.invokeOnCleared()

        verify(storedAchDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when observe is called then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<ACHDirectDebitComponentState>) -> Unit = {}

        defaultAchComponent.observe(lifecycleOwner, callback)

        verify(defaultAchDelegate).observe(lifecycleOwner, defaultAchComponent.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(defaultAchComponent.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegates is called`() {
        defaultAchComponent.removeObserver()

        verify(defaultAchDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when default component is initialized then view flow should match default ach delegate view flow`() = runTest {
        val event = defaultAchComponent.viewFlow.first()
        assertEquals(ACHDirectDebitComponentViewType, event)
    }

    @Test
    fun `when stored component is initialized then view flow should match stored ach delegate view flow`() = runTest {
        val event = storedAchComponent.viewFlow.first()
        assertEquals(null, event)
    }

    @Test
    fun `when default ach delegate view flow emits a value then component view flow should match that value`() =
        runTest {
            val achDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
            whenever(defaultAchDelegate.viewFlow) doReturn achDelegateViewFlow
            defaultAchComponent =
                ACHDirectDebitComponent(
                    defaultAchDelegate,
                    genericActionDelegate,
                    actionHandlingComponent,
                    componentEventHandler
                )
            assertEquals(TestComponentViewType.VIEW_TYPE_1, defaultAchComponent.viewFlow.first())
            achDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, defaultAchComponent.viewFlow.first())
        }

    @Test
    fun `when action delegate view flow emits a value then component view flow should match that value`() = runTest {
        val achDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn achDelegateViewFlow
        defaultAchComponent =
            ACHDirectDebitComponent(
                defaultAchDelegate,
                genericActionDelegate,
                actionHandlingComponent,
                componentEventHandler
            )

        // this value should match the value of the main delegate and not the action delegate
        // and in practice the initial value of the action delegate view flow is always null so it should be ignored
        assertEquals(ACHDirectDebitComponentViewType, defaultAchComponent.viewFlow.first())
        achDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
        assertEquals(TestComponentViewType.VIEW_TYPE_2, defaultAchComponent.viewFlow.first())
    }

    @Test
    fun `when default component isConfirmationRequired is called, then delegate is called`() {
        defaultAchComponent.isConfirmationRequired()
        verify(defaultAchDelegate).isConfirmationRequired()
    }

    @Test
    fun `when stored component isConfirmationRequired is called, then it should return false`() {
        assertEquals(false, storedAchComponent.isConfirmationRequired())
    }

    @Test
    fun `when default component setInteractionBlocked is called, then delegate is called`() {
        whenever(defaultAchComponent.delegate).thenReturn(defaultAchDelegate)
        defaultAchComponent.setInteractionBlocked(true)
        verify(defaultAchDelegate).setInteractionBlocked(true)
    }

    @Test
    fun `when submit is called and active delegate is the payment delegate, then delegate onSubmit is called`() {
        whenever(defaultAchComponent.delegate).thenReturn(defaultAchDelegate)
        defaultAchComponent.submit()
        verify(defaultAchDelegate).onSubmit()
    }

    @Test
    fun `when submit is called and active delegate is the action delegate, then delegate onSubmit is not called`() {
        whenever(defaultAchComponent.delegate).thenReturn(genericActionDelegate)
        defaultAchComponent.submit()
        verify(defaultAchDelegate, never()).onSubmit()
    }
}

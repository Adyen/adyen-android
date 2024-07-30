package com.adyen.checkout.twint

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.twint.internal.ui.DefaultTwintDelegate
import com.adyen.checkout.twint.internal.ui.TwintComponentViewType
import com.adyen.checkout.twint.internal.ui.TwintDelegate
import com.adyen.checkout.ui.core.internal.ui.TestComponentViewType
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

@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class, LoggingExtension::class)
internal class TwintComponentTest(
    @Mock private val twintDelegate: TwintDelegate,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
    @Mock private val componentEventHandler: ComponentEventHandler<TwintComponentState>,
) {

    private lateinit var component: TwintComponent

    @BeforeEach
    fun before() {
        whenever(twintDelegate.viewFlow) doReturn MutableStateFlow(TwintComponentViewType)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        component = TwintComponent(
            twintDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
    }

    @Test
    fun `when component is created, then delegates are initialized`() {
        verify(twintDelegate).initialize(component.viewModelScope)
        verify(genericActionDelegate).initialize(component.viewModelScope)
        verify(componentEventHandler).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared, then delegates are cleared`() {
        component.invokeOnCleared()

        verify(twintDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when observe is called, then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<TwintComponentState>) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(twintDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(component.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called, then removeObserver in delegates is called`() {
        component.removeObserver()

        verify(twintDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized, then view flow should match twint delegate view flow`() = runTest {
        val testViewFlow = component.viewFlow.test(testScheduler)
        assertEquals(TwintComponentViewType, testViewFlow.latestValue)
    }

    @Test
    fun `when cash app pay delegate view flow emits a value, then component view flow should match that value`() =
        runTest {
            val delegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
            whenever(twintDelegate.viewFlow) doReturn delegateViewFlow
            component = TwintComponent(
                twintDelegate,
                genericActionDelegate,
                actionHandlingComponent,
                componentEventHandler,
            )

            val testViewFlow = component.viewFlow.test(testScheduler)

            assertEquals(TestComponentViewType.VIEW_TYPE_1, testViewFlow.latestValue)

            delegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)

            assertEquals(TestComponentViewType.VIEW_TYPE_2, testViewFlow.latestValue)
        }

    @Test
    fun `when action delegate view flow emits a value, then component view flow should match that value`() = runTest {
        val actionDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn actionDelegateViewFlow
        component = TwintComponent(
            twintDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )

        val testViewFlow = component.viewFlow.test(testScheduler)
        // this value should match the value of the main delegate and not the action delegate
        // and in practice the initial value of the action delegate view flow is always null so it should be ignored
        assertEquals(TwintComponentViewType, testViewFlow.latestValue)

        actionDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)

        assertEquals(TestComponentViewType.VIEW_TYPE_2, testViewFlow.latestValue)
    }

    @Test
    fun `when isConfirmationRequired and delegate is default, then delegate is called`() {
        val delegate = mock<DefaultTwintDelegate>()
        whenever(delegate.viewFlow) doReturn MutableStateFlow(TwintComponentViewType)
        component = TwintComponent(
            delegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )

        component.isConfirmationRequired()

        verify(delegate).isConfirmationRequired()
    }

    @Test
    fun `when submit is called and active delegate is the payment delegate, then delegate onSubmit is called`() {
        val delegate = mock<DefaultTwintDelegate>()
        whenever(delegate.viewFlow) doReturn MutableStateFlow(TwintComponentViewType)
        component = TwintComponent(
            delegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
        whenever(component.delegate).thenReturn(delegate)

        component.submit()

        verify(delegate).onSubmit()
    }

    @Test
    fun `when submit is called and active delegate is the action delegate, then delegate onSubmit is not called`() {
        val delegate = mock<DefaultTwintDelegate>()
        whenever(delegate.viewFlow) doReturn MutableStateFlow(TwintComponentViewType)
        component = TwintComponent(
            delegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
        whenever(component.delegate).thenReturn(genericActionDelegate)

        component.submit()

        verify(delegate, never()).onSubmit()
    }
}

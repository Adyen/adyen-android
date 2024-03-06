package com.adyen.checkout.action.core.internal

import android.app.Activity
import android.content.Intent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.action.AwaitAction
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class DefaultActionHandlingComponentTest(
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val paymentDelegate: PaymentComponentDelegate<*>,
) {

    private lateinit var actionHandlingComponent: DefaultActionHandlingComponent

    @BeforeEach
    fun setup() {
        actionHandlingComponent = DefaultActionHandlingComponent(
            genericActionDelegate = genericActionDelegate,
            paymentDelegate = paymentDelegate,
        )
    }

    @Test
    fun `when getting active delegate before handling an action, then the payment delegate should be returned`() {
        val activeDelegate = actionHandlingComponent.activeDelegate

        assertEquals(paymentDelegate, activeDelegate)
    }

    @Test
    fun `when getting active delegate after handling an action, then the generic action delegate should be returned`() {
        val mockActionDelegate = mock<ActionDelegate>()
        whenever(genericActionDelegate.delegate) doReturn mockActionDelegate
        actionHandlingComponent.handleAction(AwaitAction(), Activity())

        val activeDelegate = actionHandlingComponent.activeDelegate

        assertEquals(mockActionDelegate, activeDelegate)
    }

    @Test
    fun `when handleAction is called then handleAction in delegate is called`() {
        val action = AwaitAction()
        val activity = Activity()

        actionHandlingComponent.handleAction(action, activity)

        verify(genericActionDelegate).handleAction(action, activity)
    }

    @Test
    fun `when handleIntent is called then handleIntent in delegate is called`() {
        val intent = Intent()

        actionHandlingComponent.handleIntent(intent)

        verify(genericActionDelegate).handleIntent(intent)
    }

    @Test
    fun `when setOnRedirectListener is called then setOnRedirectListener in delegate is called`() {
        val listener = { }

        actionHandlingComponent.setOnRedirectListener(listener)

        verify(genericActionDelegate).setOnRedirectListener(listener)
    }
}

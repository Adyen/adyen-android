/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/7/2025.
 */

package com.adyen.checkout.core.action.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.TestAction
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.model.generateCommonComponentParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
internal class ActionComponentProviderTest {

    private val component = TestActionComponent()

    private val factory = generateFactory(
        actionComponent = component,
    )

    @BeforeEach
    fun setUp() {
        ActionComponentProvider.clear()
    }

    @Test
    fun `when register is called concurrently from multiple threads, then provider should not lose any data`() {
        val threadCount = 10
        val registrationsPerThread = 100
        val totalRegistrations = threadCount * registrationsPerThread
        val executor = Executors.newFixedThreadPool(threadCount)

        // Submit all registration tasks to the thread pool
        for (i in 0 until totalRegistrations) {
            executor.submit {
                ActionComponentProvider.register("actionType_$i", factory)
            }
        }

        // Wait for all tasks to complete
        executor.shutdown()
        // Giving it a generous timeout to finish all tasks
        val completed = executor.awaitTermination(5, TimeUnit.SECONDS)
        assertTrue("Executor tasks did not complete in time.", completed)
        assertEquals(totalRegistrations, ActionComponentProvider.getFactoriesCount())
    }

    @Test
    fun `when register is called with an existing actionType, then the factory is overwritten`() =
        runTest {
            val secondaryComponent = TestActionComponent()
            val secondaryFactory = generateFactory(
                actionComponent = secondaryComponent,
            )

            ActionComponentProvider.register("actionType", factory)
            ActionComponentProvider.register("actionType", secondaryFactory)

            val actualComponent = ActionComponentProvider.get(
                action = TestAction(type = "actionType"),
                coroutineScope = this,
                analyticsManager = TestAnalyticsManager(),
                checkoutConfiguration = generateCheckoutConfiguration(),
                savedStateHandle = SavedStateHandle(),
                commonComponentParams = generateCommonComponentParams(),
            )
            assertEquals(1, ActionComponentProvider.getFactoriesCount())
            assertEquals(secondaryComponent, actualComponent)
            assertNotSame(component, actualComponent)
        }

    @Test
    fun `when register is called for different actionTypes, then all factories are stored`() {
        ActionComponentProvider.register("actionType_one", factory)
        ActionComponentProvider.register("actionType_two", factory)

        assertEquals(2, ActionComponentProvider.getFactoriesCount())
    }

    @Test
    fun `when get is called for a registered factory, then the correct component is returned`() =
        runTest {
            ActionComponentProvider.register("actionType", factory)

            val actualComponent = ActionComponentProvider.get(
                action = TestAction(type = "actionType"),
                coroutineScope = this,
                analyticsManager = TestAnalyticsManager(),
                checkoutConfiguration = generateCheckoutConfiguration(),
                savedStateHandle = SavedStateHandle(),
                commonComponentParams = generateCommonComponentParams(),
            )
            assertEquals(1, ActionComponentProvider.getFactoriesCount())
            Assert.assertSame(component, actualComponent)
        }

    @Test
    fun `when get is called for an unregistered factory, then an error is thrown`() = runTest {
        assertThrows<IllegalStateException> {
            ActionComponentProvider.get(
                action = TestAction(type = "unregistered_actionType"),
                coroutineScope = this,
                analyticsManager = TestAnalyticsManager(),
                checkoutConfiguration = generateCheckoutConfiguration(),
                savedStateHandle = SavedStateHandle(),
                commonComponentParams = generateCommonComponentParams(),
            )
        }
    }

    @Test
    fun `when clear is called, then all factories are removed`() {
        ActionComponentProvider.register("actionType_one", factory)
        ActionComponentProvider.register("actionType_two", factory)
        assertEquals(2, ActionComponentProvider.getFactoriesCount())

        ActionComponentProvider.clear()

        assertEquals(0, ActionComponentProvider.getFactoriesCount())
    }

    private fun generateFactory(actionComponent: ActionComponent) =
        object : ActionFactory<ActionComponent> {
            override fun create(
                action: Action,
                coroutineScope: CoroutineScope,
                analyticsManager: AnalyticsManager,
                checkoutConfiguration: CheckoutConfiguration,
                savedStateHandle: SavedStateHandle,
                commonComponentParams: CommonComponentParams,
            ) = actionComponent
        }

    private fun generateCheckoutConfiguration() = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test_key_12345",
    )
}

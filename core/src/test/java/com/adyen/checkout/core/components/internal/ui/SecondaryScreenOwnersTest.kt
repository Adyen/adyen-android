/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.TestFlow
import com.adyen.checkout.core.common.test
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class SecondaryScreenOwnersTest {

    private val child = TestSecondaryScreenComponent()
    private val component = TestSecondaryScreenComponent(childScreenComponents = listOf(child))
    private val owners = SecondaryScreenOwners(component)

    @Test
    fun `when the component opens a screen then the component renders it`() = runTest {
        // GIVEN
        val navigation = owners.navigation.test(testScheduler)

        // WHEN
        component.navigation.emit(SecondaryNavigationEvent.Open(COMPONENT_SCREEN))

        // THEN
        val screen = navigation.backStack().single()
        assertEquals(COMPONENT_SCREEN, screen.key)
        assertSame(component, owners[screen])
    }

    @Test
    fun `when a child component opens a screen on top then the child component renders it`() = runTest {
        // GIVEN
        val navigation = owners.navigation.test(testScheduler)
        component.navigation.emit(SecondaryNavigationEvent.Open(COMPONENT_SCREEN))

        // WHEN
        child.navigation.emit(SecondaryNavigationEvent.Open(CHILD_SCREEN))

        // THEN
        val (componentScreen, childScreen) = navigation.backStack()
        assertEquals(CHILD_SCREEN, childScreen.key)
        assertSame(child, owners[childScreen])
        assertSame(component, owners[componentScreen])
    }

    @Test
    fun `when a child component closes its screen then the screen below it is on top again`() = runTest {
        // GIVEN
        val navigation = owners.navigation.test(testScheduler)
        component.navigation.emit(SecondaryNavigationEvent.Open(COMPONENT_SCREEN))
        child.navigation.emit(SecondaryNavigationEvent.Open(CHILD_SCREEN))

        // WHEN
        child.navigation.emit(SecondaryNavigationEvent.Close)

        // THEN
        val screen = navigation.backStack().single()
        assertEquals(COMPONENT_SCREEN, screen.key)
        assertSame(component, owners[screen])
    }

    /**
     * Applies the navigation in the order it was sent, the way the secondary screen host does.
     */
    private fun TestFlow<IndexedValue<SecondaryNavigationEvent>>.backStack(): List<SecondaryScreenEntry> =
        values.fold(emptyList()) { backStack, event -> backStack.navigate(event) }

    private class TestSecondaryScreenComponent(
        override val childScreenComponents: List<SecondaryScreenComponent> = emptyList(),
    ) : SecondaryScreenComponent {

        override val navigation = MutableSharedFlow<SecondaryNavigationEvent>(extraBufferCapacity = 1)

        @Composable
        override fun SecondaryContent(identifier: String, modifier: Modifier) = Unit
    }

    companion object {
        private const val COMPONENT_SCREEN = "COMPONENT_SCREEN"
        private const val CHILD_SCREEN = "CHILD_SCREEN"
    }
}

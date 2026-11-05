/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.dropin.internal.helper.BackStackPersister
import com.adyen.checkout.dropin.internal.helper.InMemoryBackStackPersister
import com.adyen.checkout.test.LoggingExtension
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(LoggingExtension::class)
internal class DropInNavigatorTest {

    private lateinit var persister: BackStackPersister
    private lateinit var navigator: DropInNavigator

    @BeforeEach
    fun setUp() {
        persister = InMemoryBackStackPersister()
        navigator = DropInNavigator(persister)
    }

    @Test
    fun `when initialized then back stack is empty`() {
        assertEquals(emptyList<NavKey>(), navigator.backStack)
    }

    @Test
    fun `when navigating to a key then it is added to the back stack`() {
        val key = TestNavKey("test")
        navigator.navigateTo(key)
        assertEquals(listOf(key), navigator.backStack)
    }

    @Test
    fun `when clearing and navigating to a key then back stack is cleared and key is added`() {
        val initialKey = TestNavKey("initial")
        navigator.navigateTo(initialKey)

        val newKey = TestNavKey("new")
        navigator.clearAndNavigateTo(newKey)

        assertEquals(listOf(newKey), navigator.backStack)
    }

    @Test
    fun `when going back then last key is removed`() {
        val key1 = TestNavKey("key1")
        val key2 = TestNavKey("key2")
        navigator.navigateTo(key1)
        navigator.navigateTo(key2)

        navigator.back()

        assertEquals(listOf(key1), navigator.backStack)
    }

    @Test
    fun `when going back from the only key then the back stack is empty`() {
        navigator.navigateTo(TestNavKey("test"))

        navigator.back()

        assertEquals(emptyList<NavKey>(), navigator.backStack)
    }

    @Test
    fun `when going back from an empty back stack then it stays empty`() {
        navigator.back()

        assertEquals(emptyList<NavKey>(), navigator.backStack)
    }

    @Test
    fun `when clearing then the back stack is empty`() {
        navigator.navigateTo(TestNavKey("first"))
        navigator.navigateTo(TestNavKey("second"))

        navigator.clear()

        assertEquals(emptyList<NavKey>(), navigator.backStack)
    }

    @Test
    fun `when clearing then the empty back stack is restored`() {
        navigator.navigateTo(TestNavKey("test"))

        navigator.clear()

        assertEquals(emptyList<NavKey>(), DropInNavigator(persister).backStack)
    }

    @Test
    fun `when the current key is the only one then nothing is after it`() {
        navigator.navigateTo(TestNavKey("only"))

        assertEquals(true, navigator.isEmptyAfterCurrent())
    }

    @Test
    fun `when a key is stacked on another then something is after it`() {
        navigator.navigateTo(TestNavKey("first"))
        navigator.navigateTo(TestNavKey("second"))

        assertEquals(false, navigator.isEmptyAfterCurrent())
    }

    @Test
    fun `when a key is stacked on another then that one is below the current key`() {
        val first = TestNavKey("first")
        navigator.navigateTo(first)
        navigator.navigateTo(TestNavKey("second"))

        assertEquals(first, navigator.keyBelowCurrent())
    }

    @Test
    fun `when the current key is the only one then nothing is below it`() {
        navigator.navigateTo(TestNavKey("only"))

        assertNull(navigator.keyBelowCurrent())
    }

    @Test
    fun `when restoring from saved state then back stack is restored`() {
        val key = TestNavKey("test")
        navigator.navigateTo(key)

        val restoredNavigator = DropInNavigator(persister)

        assertEquals(listOf(key), restoredNavigator.backStack)
    }

    @Serializable
    private data class TestNavKey(val value: String) : NavKey
}

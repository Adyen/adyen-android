/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(LoggingExtension::class)
internal class DropInNavigatorTest {

    private lateinit var navigator: DropInNavigator

    @BeforeEach
    fun setUp() {
        navigator = DropInNavigator()
    }

    @Test
    fun `when initialized then back stack contains only empty key`() {
        assertEquals(listOf(EmptyNavKey), navigator.backStack)
    }

    @Test
    fun `when navigating to a key then it is added to the back stack`() {
        val key = TestNavKey("test")
        navigator.navigateTo(key)
        assertEquals(listOf(EmptyNavKey, key), navigator.backStack)
    }

    @Test
    fun `when clearing and navigating to a key then back stack is cleared and key is added`() {
        val initialKey = TestNavKey("initial")
        navigator.navigateTo(initialKey)

        val newKey = TestNavKey("new")
        navigator.clearAndNavigateTo(newKey)

        assertEquals(listOf(EmptyNavKey, newKey), navigator.backStack)
    }

    @Test
    fun `when going back then last key is removed`() {
        val key = TestNavKey("test")
        navigator.navigateTo(key)

        navigator.back()

        assertEquals(listOf(EmptyNavKey), navigator.backStack)
    }

    @Test
    fun `when going back to empty key then finish flow emits true`() = runTest {
        val key = TestNavKey("test")
        navigator.navigateTo(key)

        val finishFlow = navigator.finishFlow.test(testScheduler)

        navigator.back()

        assertEquals(true, finishFlow.latestValue)
    }

    @Test
    fun `when going back with multiple keys then finish flow does not emit`() = runTest {
        val key1 = TestNavKey("key1")
        val key2 = TestNavKey("key2")
        navigator.navigateTo(key1)
        navigator.navigateTo(key2)

        val finishFlow = navigator.finishFlow.test(testScheduler)

        navigator.back()

        assertEquals(1, finishFlow.values.size)
    }

    private data class TestNavKey(val value: String) : NavKey
}

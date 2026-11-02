/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/10/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SecondaryScreenHostTest {

    @Test
    fun `when the back stack is empty and a screen is opened then it is pushed onto the back stack`() {
        // GIVEN
        val backStack = emptyList<String>()

        // WHEN
        val result = SecondaryNavigationEvent.Open(FIRST).applyTo(backStack)

        // THEN
        assertEquals(listOf(FIRST), result)
    }

    @Test
    fun `when a screen is opened then it is pushed onto the back stack`() {
        // GIVEN
        val backStack = listOf(FIRST)

        // WHEN
        val result = SecondaryNavigationEvent.Open(SECOND).applyTo(backStack)

        // THEN
        assertEquals(listOf(FIRST, SECOND), result)
    }

    @Test
    fun `when the screen on top is opened again then the back stack does not change`() {
        // GIVEN
        // A double tap on the row that opens a screen sends two opens before the screen covers the row.
        val backStack = listOf(FIRST, SECOND)

        // WHEN
        val result = SecondaryNavigationEvent.Open(SECOND).applyTo(backStack)

        // THEN
        assertEquals(listOf(FIRST, SECOND), result)
    }

    @Test
    fun `when a screen below the top is opened again then it is pushed onto the back stack`() {
        // GIVEN
        val backStack = listOf(FIRST, SECOND)

        // WHEN
        val result = SecondaryNavigationEvent.Open(FIRST).applyTo(backStack)

        // THEN
        assertEquals(listOf(FIRST, SECOND, FIRST), result)
    }

    @Test
    fun `when the screen on top is closed then it is popped`() {
        // GIVEN
        val backStack = listOf(FIRST, SECOND)

        // WHEN
        val result = SecondaryNavigationEvent.Close(SECOND).applyTo(backStack)

        // THEN
        assertEquals(listOf(FIRST), result)
    }

    @Test
    fun `when a screen that is not on top is closed then the back stack does not change`() {
        // GIVEN
        // A double tap on a picker entry sends a second close after the picker was already popped.
        val backStack = listOf(FIRST)

        // WHEN
        val result = SecondaryNavigationEvent.Close(SECOND).applyTo(backStack)

        // THEN
        assertEquals(listOf(FIRST), result)
    }

    @Test
    fun `when the back stack is empty and a screen is closed then it stays empty`() {
        // GIVEN
        val backStack = emptyList<String>()

        // WHEN
        val result = SecondaryNavigationEvent.Close(FIRST).applyTo(backStack)

        // THEN
        assertEquals(emptyList<String>(), result)
    }

    companion object {
        private const val FIRST = "FIRST"
        private const val SECOND = "SECOND"
    }
}

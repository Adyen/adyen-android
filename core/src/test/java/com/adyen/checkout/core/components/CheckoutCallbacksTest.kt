/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/4/2026.
 */

package com.adyen.checkout.core.components

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class CheckoutCallbacksTest {

    @Test
    fun `when set is empty then getAdditionalCallback returns null`() {
        val callbacks: Set<CheckoutAdditionalCallback> = emptySet()

        val result = callbacks.getAdditionalCallback<TestCallbackA>()

        assertNull(result)
    }

    @Test
    fun `when set contains a callback of the requested type then getAdditionalCallback returns it`() {
        val callback = TestCallbackAImpl()
        val callbacks: Set<CheckoutAdditionalCallback> = setOf(callback)

        val result = callbacks.getAdditionalCallback<TestCallbackA>()

        assertSame(callback, result)
    }

    @Test
    fun `when set contains a callback of a different type then getAdditionalCallback returns null`() {
        val callback = TestCallbackAImpl()
        val callbacks: Set<CheckoutAdditionalCallback> = setOf(callback)

        val result = callbacks.getAdditionalCallback<TestCallbackB>()

        assertNull(result)
    }

    @Test
    fun `when set contains callbacks of multiple types then each is retrievable by its type`() {
        val callbackA = TestCallbackAImpl()
        val callbackB = TestCallbackBImpl()
        val callbacks: Set<CheckoutAdditionalCallback> = setOf(callbackA, callbackB)

        assertSame(callbackA, callbacks.getAdditionalCallback<TestCallbackA>())
        assertSame(callbackB, callbacks.getAdditionalCallback<TestCallbackB>())
    }

    @Test
    fun `when a callback implements multiple marker interfaces then it is retrievable by each`() {
        val callback = TestCallbackABImpl()
        val callbacks: Set<CheckoutAdditionalCallback> = setOf(callback)

        assertSame(callback, callbacks.getAdditionalCallback<TestCallbackA>())
        assertSame(callback, callbacks.getAdditionalCallback<TestCallbackB>())
    }

    @Test
    fun `when multiple callbacks of the same type are present then the first one is returned`() {
        val first = TestCallbackAImpl()
        val second = TestCallbackAImpl()
        // Use LinkedHashSet to guarantee iteration order for the test.
        val callbacks: Set<CheckoutAdditionalCallback> = linkedSetOf(first, second)

        val result = callbacks.getAdditionalCallback<TestCallbackA>()

        assertSame(first, result)
    }

    private interface TestCallbackA : CheckoutAdditionalCallback

    private interface TestCallbackB : CheckoutAdditionalCallback

    private class TestCallbackAImpl : TestCallbackA

    private class TestCallbackBImpl : TestCallbackB

    private class TestCallbackABImpl : TestCallbackA, TestCallbackB
}

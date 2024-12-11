/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/7/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.fail
import org.mockito.internal.matchers.apachecommons.ReflectionEquals

@Suppress("TooManyFunctions")
class TestAnalyticsManager : AnalyticsManager {

    private var isInitialized = false
    private var isCleared = false
    private var checkoutAttemptId: String = CHECKOUT_ATTEMPT_ID_NOT_FETCHED
    private val events: MutableList<AnalyticsEvent> = mutableListOf()

    override fun initialize(owner: Any, coroutineScope: CoroutineScope) {
        isInitialized = true
    }

    fun assertIsInitialized() {
        assertTrue(isInitialized)
    }

    override fun trackEvent(event: AnalyticsEvent) {
        events.add(event)
    }

    fun assertHasEventEquals(expected: AnalyticsEvent) {
        assertTrue(events.any { event -> areEventsEqual(expected, event) })
    }

    fun assertLastEventEquals(expected: AnalyticsEvent) {
        if (events.isEmpty()) fail("The events list is empty")
        assertTrue(areEventsEqual(expected, events.last()))
    }

    fun assertLastEventNotEquals(expected: AnalyticsEvent) {
        if (events.isEmpty()) return
        assertFalse(areEventsEqual(expected, events.last()))
    }

    private fun areEventsEqual(expected: AnalyticsEvent, actual: AnalyticsEvent): Boolean {
        val re = ReflectionEquals(
            expected,
            // Exclude these fields as they are generated at runtime
            "id",
            "timestamp",
            // Exclude message field as it is not required
            "message",
        )
        return re.matches(actual)
    }

    override fun getCheckoutAttemptId(): String = checkoutAttemptId

    fun setCheckoutAttemptId(checkoutAttemptId: String) {
        this.checkoutAttemptId = checkoutAttemptId
    }

    override fun clear(owner: Any) {
        isCleared = true
    }

    fun assertIsCleared() {
        assertTrue(isCleared)
    }

    companion object {
        const val CHECKOUT_ATTEMPT_ID_NOT_FETCHED = "not-fetched"
    }
}

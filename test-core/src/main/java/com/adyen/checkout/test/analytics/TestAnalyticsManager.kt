/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/3/2024.
 */
package com.adyen.checkout.test.analytics

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.core.internal.analytics.AnalyticsManager
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.internal.matchers.apachecommons.ReflectionEquals

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP, RestrictTo.Scope.TESTS)
class TestAnalyticsManager : AnalyticsManager {

    private var isInitialized = false
    private var isCleared = false
    private var checkoutAttemptId: String? = null
    private var lastEvent: AnalyticsEvent? = null
    override fun initialize(owner: Any, coroutineScope: CoroutineScope) {
        isInitialized = true
    }

    fun assertIsInitialized() {
        assertTrue(isInitialized)
    }

    override fun trackEvent(event: AnalyticsEvent) {
        lastEvent = event
    }

    fun assertLastEventEquals(expected: AnalyticsEvent?) {
        // Exclude these field as they are generated at runtime
        val re = ReflectionEquals(
            expected,
            "id",
            "timestamp",
        )
        assertTrue(re.matches(lastEvent))
    }

    override fun getCheckoutAttemptId() = checkoutAttemptId

    fun setCheckoutAttemptId(checkoutAttemptId: String?) {
        this.checkoutAttemptId = checkoutAttemptId
    }

    override fun clear(owner: Any) {
        isCleared = true
    }

    fun assertIsCleared() {
        assertTrue(isCleared)
    }
}

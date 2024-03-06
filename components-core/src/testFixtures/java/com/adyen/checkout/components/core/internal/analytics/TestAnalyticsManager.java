/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/3/2024.
 */

package com.adyen.checkout.components.core.internal.analytics;

import static org.junit.jupiter.api.Assertions.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import kotlinx.coroutines.CoroutineScope;

public class TestAnalyticsManager implements AnalyticsManager {

    private boolean isInitialized = false;

    private boolean isCleared = false;

    private String checkoutAttemptId = null;

    private AnalyticsEvent lastEvent = null;

    @Override
    public void initialize(@NonNull Object owner, @NonNull CoroutineScope coroutineScope) {
        isInitialized = true;
    }

    public void assertIsInitialized() {
        assertTrue(isInitialized);
    }

    @Override
    public void trackEvent(@NonNull AnalyticsEvent event) {
        lastEvent = event;
    }

    public void assertLastEventEquals(AnalyticsEvent expected) {
        ReflectionEquals re = new ReflectionEquals(
            expected,
            // Exclude these field as they are generated at runtime
            "id",
            "timestamp"
        );
        assertTrue(re.matches(lastEvent));
    }

    @Nullable
    @Override
    public String getCheckoutAttemptId() {
        return checkoutAttemptId;
    }

    public void setCheckoutAttemptId(String checkoutAttemptId) {
        this.checkoutAttemptId = checkoutAttemptId;
    }

    @Override
    public void clear(@NonNull Object owner) {
        isCleared = true;
    }

    public void assertIsCleared() {
        assertTrue(isCleared);
    }
}

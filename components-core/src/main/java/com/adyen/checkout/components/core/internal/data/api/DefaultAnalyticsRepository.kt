/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/11/2022.
 */

package com.adyen.checkout.components.core.internal.data.api

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSource
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.core.internal.util.runSuspendCatching
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultAnalyticsRepository(
    private val packageName: String,
    private val locale: Locale,
    private val source: AnalyticsSource,
    private val analyticsService: AnalyticsService,
    private val analyticsMapper: AnalyticsMapper,
    private val clientKey: String,
) : AnalyticsRepository {

    @VisibleForTesting
    internal var state: State = State.Uninitialized
        private set

    override suspend fun setupAnalytics() {
        if (state != State.Uninitialized) return
        state = State.InProgress
        Logger.v(TAG, "Setting up analytics")

        runSuspendCatching {
            val analyticsSetupRequest = analyticsMapper.getAnalyticsSetupRequest(packageName, locale, source)
            analyticsService.setupAnalytics(analyticsSetupRequest, clientKey)
            state = State.Ready
            Logger.v(TAG, "Analytics setup call successful")
        }.onFailure { e ->
            state = State.Failed
            Logger.e(TAG, "Failed to send analytics setup call", e)
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }

    @VisibleForTesting
    internal sealed class State {
        object Uninitialized : State()
        object InProgress : State()
        object Ready : State()
        object Failed : State()
    }
}

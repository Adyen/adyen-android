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
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel.ALL
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel.NONE
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.core.internal.util.runSuspendCatching

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultAnalyticsRepository(
    private val analyticsRepositoryData: AnalyticsRepositoryData,
    private val analyticsService: AnalyticsService,
    private val analyticsMapper: AnalyticsMapper,
) : AnalyticsRepository {

    @VisibleForTesting
    internal var state: State = State.Uninitialized
        private set

    private var checkoutAttemptId: String? = null
    override fun getCheckoutAttemptId(): String? = checkoutAttemptId

    override suspend fun setupAnalytics() {
        if (!canSendAnalytics(requiredLevel = ALL)) return
        if (state != State.Uninitialized) return
        state = State.InProgress
        Logger.v(TAG, "Setting up analytics")

        runSuspendCatching {
            val analyticsSetupRequest = with(analyticsRepositoryData) {
                analyticsMapper.getAnalyticsSetupRequest(
                    packageName = packageName,
                    locale = locale,
                    source = source,
                    amount = amount,
                    screenWidth = screenWidth.toLong(),
                    paymentMethods = paymentMethods,
                )
            }
            val response = analyticsService.setupAnalytics(analyticsSetupRequest, analyticsRepositoryData.clientKey)
            checkoutAttemptId = response.checkoutAttemptId
            state = State.Ready
            Logger.v(TAG, "Analytics setup call successful")
        }.onFailure { e ->
            state = State.Failed
            Logger.e(TAG, "Failed to send analytics setup call", e)
        }
    }

    private fun canSendAnalytics(requiredLevel: AnalyticsParamsLevel): Boolean {
        require(requiredLevel != NONE) { "Analytics are not allowed with level NONE" }
        return !analyticsRepositoryData.level.hasHigherPriorityThan(requiredLevel)
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

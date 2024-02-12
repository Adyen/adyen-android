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
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
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
        if (!canSendAnalytics(requiredLevel = ALL)) {
            checkoutAttemptId = CHECKOUT_ATTEMPT_ID_FOR_DISABLED_ANALYTICS
            return
        }
        if (state != State.Uninitialized) return
        state = State.InProgress
        adyenLog(AdyenLogLevel.VERBOSE) { "Setting up analytics" }

//        runSuspendCatching {
//            val analyticsSetupRequest = with(analyticsRepositoryData) {
//                analyticsMapper.getAnalyticsSetupRequest(
//                    packageName = packageName,
//                    locale = locale,
//                    source = source,
//                    amount = amount,
//                    screenWidth = screenWidth.toLong(),
//                    paymentMethods = paymentMethods,
//                    sessionId = sessionId,
//                )
//            }
//            val response = analyticsService.setupAnalytics(analyticsSetupRequest, analyticsRepositoryData.clientKey)
//            checkoutAttemptId = response.checkoutAttemptId
//            state = State.Ready
//            adyenLog(AdyenLogLevel.VERBOSE) { "Analytics setup call successful" }
//        }.onFailure { e ->
//            state = State.Failed
//            adyenLog(AdyenLogLevel.ERROR) {
//                "Failed to send analytics setup call - ${e::class.simpleName}: ${e.message}"
//            }
//        }
    }

    private fun canSendAnalytics(requiredLevel: AnalyticsParamsLevel): Boolean {
        require(requiredLevel != NONE) { "Analytics are not allowed with level NONE" }
        return true
    }

    companion object {

        @VisibleForTesting
        internal const val CHECKOUT_ATTEMPT_ID_FOR_DISABLED_ANALYTICS = "do-not-track"
    }

    @VisibleForTesting
    internal sealed class State {
        object Uninitialized : State()
        object InProgress : State()
        object Ready : State()
        object Failed : State()
    }
}

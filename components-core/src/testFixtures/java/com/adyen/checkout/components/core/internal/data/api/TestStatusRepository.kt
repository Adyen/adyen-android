/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/7/2024.
 */

package com.adyen.checkout.components.core.internal.data.api

import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class TestStatusRepository : StatusRepository {

    var pollingResults: List<Result<StatusResponse>> = emptyList()
    private var timesOnPollCalled: Int = 0

    override fun poll(paymentData: String, maxPollingDuration: Long): Flow<Result<StatusResponse>> {
        timesOnPollCalled++
        return pollingResults.asFlow()
    }

    override fun refreshStatus(paymentData: String) = Unit

    fun assertPollingStarted() {
        assert(timesOnPollCalled > 0)
    }

    fun assertPollingNotStarted() {
        assert(timesOnPollCalled <= 0)
    }
}

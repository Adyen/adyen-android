/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/8/2022.
 */

package com.adyen.checkout.components.core.internal.test

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.data.api.StatusRepository
import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/**
 * Test implementation of [StatusRepository]. This class should never be used in not test code as it does not actuall
 * poll any status!
 */
// TODO move to test fixtures once it becomes supported on Android
@RestrictTo(RestrictTo.Scope.TESTS)
class TestStatusRepository : StatusRepository {

    var pollingResults: List<Result<StatusResponse>> = emptyList()
    var timesOnPollCalled: Int = 0

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

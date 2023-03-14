/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 31/1/2023.
 */
package com.adyen.checkout.test.extensions

import androidx.annotation.RestrictTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.coroutines.CoroutineContext

/**
 * Collects the values of a flow into a list.
 *
 * Should only be used in tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RestrictTo(RestrictTo.Scope.TESTS)
class TestFlow<T> internal constructor(flow: Flow<T>, testScheduler: TestCoroutineScheduler) : CoroutineScope {

    private val coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext = UnconfinedTestDispatcher(testScheduler) + coroutineJob

    private val _values = mutableListOf<T>()
    val values: List<T> = _values

    val latestValue: T get() = values.last()

    init {
        flow
            .onEach { _values.add(it) }
            .launchIn(this)
    }
}

/**
 * Extension method to create a [TestFlow].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RestrictTo(RestrictTo.Scope.TESTS)
fun <T> Flow<T>.test(testScheduler: TestCoroutineScheduler): TestFlow<T> {
    return TestFlow(this, testScheduler)
}

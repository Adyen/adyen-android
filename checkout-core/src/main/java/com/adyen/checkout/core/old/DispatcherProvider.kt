/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.core.old

import androidx.annotation.RestrictTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

@Suppress("NotDispatcherProvider")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object DispatcherProvider {

    private var mainFactory = { Dispatchers.Main }
    val Main: MainCoroutineDispatcher get() = mainFactory()

    private var defaultFactory = { Dispatchers.Default }
    val Default: CoroutineDispatcher get() = defaultFactory()

    private var ioFactory = { Dispatchers.IO }
    val IO: CoroutineDispatcher get() = ioFactory()

    fun overrideMain(dispatcher: MainCoroutineDispatcher) {
        mainFactory = { dispatcher }
    }

    fun overrideIO(dispatcher: CoroutineDispatcher) {
        ioFactory = { dispatcher }
    }

    fun overrideDefault(dispatcher: CoroutineDispatcher) {
        defaultFactory = { dispatcher }
    }

    fun resetMain() {
        mainFactory = { Dispatchers.Main }
    }

    fun resetIO() {
        ioFactory = { Dispatchers.IO }
    }

    fun resetDefault() {
        defaultFactory = { Dispatchers.Default }
    }

    fun resetAll() {
        resetMain()
        resetIO()
        resetDefault()
    }
}

/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/10/2024.
 */

package com.adyen.checkout.core

import androidx.annotation.RestrictTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object DispatcherProvider {

    @Suppress("ktlint:standard:property-naming")
    var Main: CoroutineDispatcher = Dispatchers.Main
        private set

    @Suppress("ktlint:standard:property-naming")
    var IO: CoroutineDispatcher = Dispatchers.IO
        private set

    @Suppress("ktlint:standard:property-naming")
    var Default: CoroutineDispatcher = Dispatchers.Default
        private set

    fun setMain(dispatcher: CoroutineDispatcher) {
        this.Main = dispatcher
    }

    fun setIO(dispatcher: CoroutineDispatcher) {
        this.IO = dispatcher
    }

    fun setDefault(dispatcher: CoroutineDispatcher) {
        this.Default = dispatcher
    }

    fun resetMain() {
        Main = Dispatchers.Main
    }

    fun resetIO() {
        IO = Dispatchers.IO
    }

    fun resetDefault() {
        Default = Dispatchers.Default
    }

    fun resetAll() {
        resetMain()
        resetIO()
        resetDefault()
    }
}
